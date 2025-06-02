package com.example.aiapp.model;

public class ImageMessage extends Message {
    private String imageUrl;
    private String prompt;

    public ImageMessage(String imageUrl, String prompt, boolean isUser) {
        super(prompt, isUser);
        this.imageUrl = imageUrl;
        this.prompt = prompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPrompt() {
        return prompt;
    }

    @Override
    public boolean isImage() {
        return true;
    }
} 