package com.example.aiapp.model;

public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String createdAt;

    public User(int id, String username, String email, String passwordHash, String createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getCreatedAt() { return createdAt; }


    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
} 