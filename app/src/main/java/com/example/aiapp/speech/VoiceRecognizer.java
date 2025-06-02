package com.example.aiapp.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceRecognizer {
    private static final String TAG = "VoiceRecognizer";
    
    private final Context context;
    private final Handler mainHandler;
    private final SpeechRecognizer speechRecognizer;
    private boolean isRecording = false;

    public interface SpeechCallback {
        void onTranscriptionResult(String text);
        void onError(String error);
    }

    public VoiceRecognizer(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            throw new RuntimeException("Speech recognition is not available on this device");
        }
        
        this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
    }

    public void startRecording() {
        if (isRecording) {
            return;
        }

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        isRecording = true;
        speechRecognizer.startListening(recognizerIntent);
    }

    public void stopRecordingAndTranscribe(SpeechCallback callback) {
        if (!isRecording) {
            return;
        }

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d(TAG, "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech");
            }

            @Override
            public void onRmsChanged(float v) {
                //
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                //
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
                isRecording = false;
            }

            @Override
            public void onError(int error) {
                isRecording = false;
                String errorMessage;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorMessage = "Ошибка записи аудио";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorMessage = "Ошибка клиента";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorMessage = "Недостаточно прав";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorMessage = "Ошибка сети";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorMessage = "Таймаут сети";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorMessage = "Мова не розпізнана";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorMessage = "Розпізнавач зайнятий";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        errorMessage = "Ошибка сервера";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorMessage = "Час очікування мовлення закінчився";
                        break;
                    default:
                        errorMessage = "Невідома помилка";
                        break;
                }
                mainHandler.post(() -> callback.onError(errorMessage));
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    Log.d(TAG, "Recognized text: " + text);
                    mainHandler.post(() -> callback.onTranscriptionResult(text));
                } else {
                    mainHandler.post(() -> callback.onError("Не вдалося розпізнати мову"));
                }
                isRecording = false;
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    public void shutdown() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }
} 