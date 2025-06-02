package com.example.aiapp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatHistory {
    private String id;
    private String title;
    private Date date;
    private List<Message> messages;

    public ChatHistory(String title) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title;
        this.date = new Date();
        this.messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
        if (title == null || title.isEmpty()) {
            this.title = message.getText().length() > 30
                ? message.getText().substring(0, 27) + "..."
                : message.getText();
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }
} 