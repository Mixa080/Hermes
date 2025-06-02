package com.example.aiapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aiapp.adapter.MessageAdapter;
import com.example.aiapp.adapter.ChatHistoryAdapter;
import com.example.aiapp.api.GeminiService;
import com.example.aiapp.model.Message;
import com.example.aiapp.model.ChatHistory;
import com.example.aiapp.speech.VoiceRecognizer;
import com.google.android.material.button.MaterialButton;
import com.example.aiapp.database.DatabaseHelper;
import android.util.Log;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.example.aiapp.model.ChatMode;
import com.example.aiapp.viewmodel.ChatModeViewModel;
import com.example.aiapp.api.ImageGenerationService;
import com.example.aiapp.model.ImageMessage;
import com.example.aiapp.model.User;
import android.content.Intent;
import java.util.List;
import java.util.ArrayList;
import com.example.aiapp.util.TranslationUtil;

public class MainActivity extends AppCompatActivity implements ChatHistoryAdapter.OnChatSelectedListener {
    private static final String TAG = "MainActivity";
    private MessageAdapter messageAdapter;
    private ChatHistoryAdapter chatHistoryAdapter;
    private EditText messageInput;
    private GeminiService geminiService;
    private boolean isProcessing = false;
    private RecyclerView messagesRecyclerView;
    private RecyclerView chatHistoryRecyclerView;
    private DrawerLayout drawerLayout;
    private ChatHistory currentChat;
    private VoiceRecognizer voiceRecognizer;
    private ImageButton voiceButton;
    private MaterialButton newChatButton;
    private ImageButton sendButton;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private ChatModeViewModel chatModeViewModel;
    private ImageButton btnSend;
    private ImageButton btnMic;
    private TextView modeChangeAnimation;
    private MaterialButton btnCasual;
    private MaterialButton btnProfessional;
    private MaterialButton btnAcademic;
    private ImageGenerationService imageGenerationService;
    private static final String PEXELS_API_KEY = "YOUR_PEXELS_API_KEY";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startRecording();
            } else {
                Toast.makeText(this, R.string.error_microphone_permission, Toast.LENGTH_LONG).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        try {
            initializeViews();
            setupLogoutButton();
            validateViewsInitialized();

            dbHelper = new DatabaseHelper(getApplicationContext());
            
            currentUserId = getIntent().getIntExtra("USER_ID", -1);
            if (currentUserId == -1) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            setupMessagesList();
            setupChatHistoryList();
            loadOrCreateChat();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing: " + e.getMessage());
            return;
        }

        geminiService = new GeminiService();
        voiceRecognizer = new VoiceRecognizer(this);
        imageGenerationService = new ImageGenerationService();

        setupClickListeners();
        setupChatModeViewModel();
    }
    
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        messagesRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.btnSend);
        voiceButton = findViewById(R.id.btnMic);
        newChatButton = findViewById(R.id.newChatButton);
        chatHistoryRecyclerView = findViewById(R.id.chatHistoryRecyclerView);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        modeChangeAnimation = findViewById(R.id.modeChangeAnimation);
        btnCasual = findViewById(R.id.btnCasual);
        btnProfessional = findViewById(R.id.btnProfessional);
        btnAcademic = findViewById(R.id.btnAcademic);
    }
    
    private void setupLogoutButton() {
        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
    
    private void validateViewsInitialized() {
        if (messagesRecyclerView == null || messageInput == null || sendButton == null || 
            voiceButton == null || newChatButton == null || chatHistoryRecyclerView == null ||
            btnSend == null || btnMic == null || modeChangeAnimation == null ||
            btnCasual == null || btnProfessional == null || btnAcademic == null) {
            throw new IllegalStateException("Some views were not found in the layout");
        }
    }
    
    private void setupMessagesList() {
        messageAdapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }
    
    private void setupChatHistoryList() {
        chatHistoryAdapter = new ChatHistoryAdapter(this);
        chatHistoryAdapter.setOnChatDeletedListener(chatHistory -> {
            dbHelper.deleteChat(Long.parseLong(chatHistory.getId()));
            chatHistoryAdapter.removeChatHistory(chatHistory);
            
            if (currentChat != null && currentChat.getId().equals(chatHistory.getId())) {
                createNewChat();
            }
        });
        chatHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatHistoryRecyclerView.setAdapter(chatHistoryAdapter);
    }
    
    private void loadOrCreateChat() {
        List<ChatHistory> chatHistories = dbHelper.getAllChats(currentUserId);
        
        if (chatHistories != null && !chatHistories.isEmpty()) {
            chatHistoryAdapter.setChatHistories(chatHistories);
            currentChat = chatHistories.get(0);
            loadChatMessages(currentChat.getId());
        } else {
            long chatId = dbHelper.createChat(currentUserId, "New Chat");
            currentChat = new ChatHistory("New Chat");
            currentChat.setId(String.valueOf(chatId));
            chatHistoryAdapter.addChatHistory(currentChat);
        }
    }
    
    private void setupClickListeners() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.open();
                }
            });
        }
        
        if (newChatButton != null) {
            newChatButton.setOnClickListener(v -> {
                createNewChat();
                if (drawerLayout != null) {
                    drawerLayout.close();
                }
            });
        }
        
        if (sendButton != null) {
            sendButton.setOnClickListener(v -> sendMessage());
        }
        
        if (voiceButton != null) {
            voiceButton.setOnClickListener(v -> checkMicrophonePermission());
        }
        
        if (messageInput != null) {
            messageInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            });
        }

        if (btnCasual != null) {
            btnCasual.setOnClickListener(v -> {
                if (chatModeViewModel != null) {
                    chatModeViewModel.setMode(ChatMode.CASUAL);
                }
            });
        }

        if (btnProfessional != null) {
            btnProfessional.setOnClickListener(v -> {
                if (chatModeViewModel != null) {
                    chatModeViewModel.setMode(ChatMode.PROFESSIONAL);
                }
            });
        }

        if (btnAcademic != null) {
            btnAcademic.setOnClickListener(v -> {
                if (chatModeViewModel != null) {
                    chatModeViewModel.setMode(ChatMode.ACADEMIC);
                }
            });
        }

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                sendMessage();
            });
        }

        if (btnMic != null) {
            btnMic.setOnClickListener(v -> {
                checkMicrophonePermission();
            });
        }

        ImageButton btnGenerateImage = findViewById(R.id.btnGenerateImage);
        if (btnGenerateImage != null) {
            btnGenerateImage.setOnClickListener(v -> {
                String prompt = messageInput.getText().toString().trim();
                if (!prompt.isEmpty()) {
                    generateImage(prompt);
                } else {
                    Toast.makeText(this, "Введіть опис зображення", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void setupChatModeViewModel() {
        chatModeViewModel = new ViewModelProvider(this).get(ChatModeViewModel.class);
        observeViewModel();
    }

    private void loadChatMessages(String chatId) {
        List<Message> messages = dbHelper.getChatMessages(Long.parseLong(chatId));
        if (messages != null) {
            messageAdapter = new MessageAdapter();
            for (Message message : messages) {
                messageAdapter.addMessage(message);
            }
            messagesRecyclerView.setAdapter(messageAdapter);
            scrollToBottom();
        }
    }

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startRecording() {
        voiceRecognizer.startRecording();
        voiceRecognizer.stopRecordingAndTranscribe(new VoiceRecognizer.SpeechCallback() {
            @Override
            public void onTranscriptionResult(String text) {
                messageInput.setText(text);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewChat() {
        long chatId = dbHelper.createChat(currentUserId, "New Chat");
        currentChat = new ChatHistory("New Chat");
        currentChat.setId(String.valueOf(chatId));
        chatHistoryAdapter.addChatHistory(currentChat);
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setAdapter(messageAdapter);
        
        dbHelper.saveAISettings(chatId, "gpt-4", 0.7f, 2048, "You are a helpful AI assistant.");
    }

    @Override
    public void onChatSelected(ChatHistory chatHistory) {
        currentChat = chatHistory;
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setAdapter(messageAdapter);
        
        List<Message> messages = dbHelper.getChatMessages(Long.parseLong(chatHistory.getId()));
        for (Message message : messages) {
            messageAdapter.addMessage(message);
        }
        
        drawerLayout.close();
        scrollToBottom();
    }

    private void sendMessage() {
        if (isProcessing) {
            Toast.makeText(this, "Please wait for the previous message to process", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        Message userMessage = new Message(messageText, true);
        messageAdapter.addMessage(userMessage);
        currentChat.addMessage(userMessage);
        
        long messageId = dbHelper.createMessage(Long.parseLong(currentChat.getId()), "user", messageText, null);
        
        messageInput.setText("");
        scrollToBottom();

        isProcessing = true;

        geminiService.sendMessage(messageText, new GeminiService.ChatCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    Message aiMessage = new Message(response, false);
                    messageAdapter.addMessage(aiMessage);
                    currentChat.addMessage(aiMessage);
                    
                    long aiMessageId = dbHelper.createMessage(Long.parseLong(currentChat.getId()), "ai", response, null);
                    
                    isProcessing = false;
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    isProcessing = false;
                });
            }
        });
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceRecognizer != null) {
            voiceRecognizer.shutdown();
        }
    }

    private void observeViewModel() {
        chatModeViewModel.getModeTitle().observe(this, title -> {
            if (title != null) {
                showModeChangeAnimation(title);
            }
        });

        chatModeViewModel.getCurrentMode().observe(this, mode -> {
            if (mode != null) {
                updateTheme(mode);
            }
        });
    }

    private void showModeChangeAnimation(String title) {
        modeChangeAnimation.setText(title);
        modeChangeAnimation.setTextColor(getResources().getColor(android.R.color.white));
        modeChangeAnimation.setVisibility(View.VISIBLE);
        modeChangeAnimation.setAlpha(0f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(modeChangeAnimation, "alpha", 0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(modeChangeAnimation, "alpha", 1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.setStartDelay(1000);

        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeOut.start();
            }
        });

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                modeChangeAnimation.setVisibility(View.GONE);
            }
        });

        fadeIn.start();
    }

    private void updateTheme(ChatMode mode) {
        findViewById(R.id.appBarLayout).setBackgroundColor(getResources().getColor(R.color.primary));
        messagesRecyclerView.setBackgroundColor(getResources().getColor(R.color.background_dark));
    }

    private void startVoiceInput() {
    }

    private void generateImage(String prompt) {
        String promptEn = TranslationUtil.translateToEnglish(prompt);

        Message userMessage = new Message("Згенерувати зображення: " + prompt, true);
        messageAdapter.addMessage(userMessage);
        currentChat.addMessage(userMessage);
        messageInput.setText("");
        scrollToBottom();

        Toast.makeText(this, "Генерація зображення...", Toast.LENGTH_SHORT).show();

        imageGenerationService.generateImage(promptEn, new ImageGenerationService.ImageGenerationCallback() {
            @Override
            public void onImageGenerated(String imageUrl) {
                runOnUiThread(() -> {
                    ImageMessage imageMessage = new ImageMessage(imageUrl, prompt, false);
                    messageAdapter.addMessage(imageMessage);
                    currentChat.addMessage(imageMessage);
                    long imageMsgId = dbHelper.createMessage(Long.parseLong(currentChat.getId()), "ai", prompt, imageUrl);
                    Log.d(TAG, "Saved image message to database with ID: " + imageMsgId);
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Помилка: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}