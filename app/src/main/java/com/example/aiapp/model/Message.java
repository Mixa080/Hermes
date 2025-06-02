package com.example.aiapp.model;

public class Message {
    private String id;
    private String text;
    private boolean isUser;

    public Message(String text, boolean isUser) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.text = text;
        this.isUser = isUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    public boolean isImage() {
        return false;
    }
} 