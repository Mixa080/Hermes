package com.example.aiapp.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aiapp.R;
import com.example.aiapp.model.ChatHistory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder> {
    private List<ChatHistory> chatHistories = new ArrayList<>();
    private OnChatSelectedListener listener;
    private OnChatDeletedListener deleteListener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public interface OnChatSelectedListener {
        void onChatSelected(ChatHistory chatHistory);
    }

    public interface OnChatDeletedListener {
        void onChatDeleted(ChatHistory chatHistory);
    }

    public ChatHistoryAdapter(OnChatSelectedListener listener) {
        this.listener = listener;
    }

    public void setOnChatDeletedListener(OnChatDeletedListener listener) {
        this.deleteListener = listener;
    }

    public void setChatHistories(List<ChatHistory> chatHistories) {
        this.chatHistories = chatHistories;
        notifyDataSetChanged();
    }

    public void addChatHistory(ChatHistory chatHistory) {
        chatHistories.add(0, chatHistory);
        notifyItemInserted(0);
    }

    public void removeChatHistory(ChatHistory chatHistory) {
        int position = chatHistories.indexOf(chatHistory);
        if (position != -1) {
            chatHistories.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ChatHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_history, parent, false);
        return new ChatHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHistoryViewHolder holder, int position) {
        ChatHistory chatHistory = chatHistories.get(position);
        holder.bind(chatHistory);
    }

    @Override
    public int getItemCount() {
        return chatHistories.size();
    }

    class ChatHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView dateText;
        private final ImageButton deleteButton;

        ChatHistoryViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.chatTitle);
            dateText = itemView.findViewById(R.id.chatDate);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onChatSelected(chatHistories.get(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ChatHistory chatHistory = chatHistories.get(position);
                    showDeleteConfirmationDialog(chatHistory);
                }
            });
        }

        private void showDeleteConfirmationDialog(ChatHistory chatHistory) {
            new AlertDialog.Builder(itemView.getContext())
                .setTitle("Видалення чату")
                .setMessage("Ви впевнені, що хочете видалити цей чат?")
                .setPositiveButton("Так", (dialog, which) -> {
                    if (deleteListener != null) {
                        deleteListener.onChatDeleted(chatHistory);
                    }
                })
                .setNegativeButton("Ні", null)
                .show();
        }

        void bind(ChatHistory chatHistory) {
            titleText.setText(chatHistory.getTitle());
            dateText.setText(dateFormat.format(chatHistory.getDate()));
        }
    }
} 