package com.example.aiapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.aiapp.model.ChatMode;
import android.content.Context;
import android.graphics.Color;

public class ChatModeViewModel extends ViewModel {
    private final MutableLiveData<ChatMode> currentMode = new MutableLiveData<>(ChatMode.CASUAL);
    private final MutableLiveData<String> modeTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showModeChangeAnimation = new MutableLiveData<>(false);

    public LiveData<ChatMode> getCurrentMode() {
        return currentMode;
    }

    public LiveData<String> getModeTitle() {
        return modeTitle;
    }

    public LiveData<Boolean> getShowModeChangeAnimation() {
        return showModeChangeAnimation;
    }

    public void setMode(ChatMode mode) {
        currentMode.setValue(mode);
        modeTitle.setValue(mode.getTitle());
        showModeChangeAnimation.setValue(true);
        
        // Скидаємо анімацію через 2 секунди
        new android.os.Handler().postDelayed(() -> 
            showModeChangeAnimation.setValue(false), 2000);
    }

    public int getPrimaryColor() {
        ChatMode mode = currentMode.getValue();
        if (mode == null) return Color.parseColor("#FFC107"); // Default casual color
        
        switch (mode) {
            case CASUAL:
                return Color.parseColor("#FFC107");
            case PROFESSIONAL:
                return Color.parseColor("#2196F3");
            case ACADEMIC:
                return Color.parseColor("#3F51B5");
            default:
                return Color.parseColor("#FFC107");
        }
    }

    public int getSecondaryColor() {
        ChatMode mode = currentMode.getValue();
        if (mode == null) return Color.parseColor("#FFA000"); // Default casual color
        
        switch (mode) {
            case CASUAL:
                return Color.parseColor("#FFA000");
            case PROFESSIONAL:
                return Color.parseColor("#1976D2");
            case ACADEMIC:
                return Color.parseColor("#303F9F");
            default:
                return Color.parseColor("#FFA000");
        }
    }

    public int getBackgroundColor() {
        ChatMode mode = currentMode.getValue();
        if (mode == null) return Color.parseColor("#FFF8E1"); // Default casual color
        
        switch (mode) {
            case CASUAL:
                return Color.parseColor("#FFF8E1");
            case PROFESSIONAL:
                return Color.parseColor("#E3F2FD");
            case ACADEMIC:
                return Color.parseColor("#E8EAF6");
            default:
                return Color.parseColor("#FFF8E1");
        }
    }

    public int getTextColor() {
        ChatMode mode = currentMode.getValue();
        if (mode == null) return Color.parseColor("#212121"); // Default text color
        
        switch (mode) {
            case CASUAL:
                return Color.parseColor("#212121");
            case PROFESSIONAL:
                return Color.parseColor("#1565C0");
            case ACADEMIC:
                return Color.parseColor("#1A237E");
            default:
                return Color.parseColor("#212121");
        }
    }
} 