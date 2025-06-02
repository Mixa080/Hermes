package com.example.aiapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.aiapp.model.User;
import com.example.aiapp.model.Message;
import com.example.aiapp.model.ChatHistory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "aiapp.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_CHATS = "chats";
    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_AI_SETTINGS = "ai_settings";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CHAT_ID = "chat_id";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_IMAGE_URL = "image_url";
    private static final String COLUMN_MODEL = "model";
    private static final String COLUMN_TEMPERATURE = "temperature";
    private static final String COLUMN_MAX_TOKENS = "max_tokens";
    private static final String COLUMN_SYSTEM_PROMPT = "system_prompt";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD_HASH + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_CHATS_TABLE = "CREATE TABLE " + TABLE_CHATS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_CHATS_TABLE);

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CHAT_ID + " INTEGER,"
                + COLUMN_SENDER + " TEXT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_IMAGE_URL + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_CHAT_ID + ") REFERENCES " + TABLE_CHATS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);

        String CREATE_AI_SETTINGS_TABLE = "CREATE TABLE " + TABLE_AI_SETTINGS + "("
                + COLUMN_CHAT_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_MODEL + " TEXT,"
                + COLUMN_TEMPERATURE + " REAL,"
                + COLUMN_MAX_TOKENS + " INTEGER,"
                + COLUMN_SYSTEM_PROMPT + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_CHAT_ID + ") REFERENCES " + TABLE_CHATS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_AI_SETTINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_AI_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            
            onCreate(db);
        }
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, null, null, null, null, null, null);
            if (cursor != null && cursor.getColumnIndex(columnName) != -1) {
                return true;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking column existence: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public long registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD_HASH, hashPassword(password));

        try {
            return db.insertOrThrow(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error registering user: " + e.getMessage());
            return -1;
        }
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String passwordHash = hashPassword(password);

        String[] columns = {COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PASSWORD_HASH, COLUMN_CREATED_AT};
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD_HASH + " = ?";
        String[] selectionArgs = {email, passwordHash};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
            );
        }
        cursor.close();
        return user;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("DatabaseHelper", "Error hashing password: " + e.getMessage());
            return null;
        }
    }

    public long createChat(int userId, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        long chatId = db.insert(TABLE_CHATS, null, values);
        Log.d("DatabaseHelper", "Created chat with ID: " + chatId + " for user: " + userId);
        return chatId;
    }

    public List<ChatHistory> getAllChats(int userId) {
        List<ChatHistory> chatList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        Log.d("DatabaseHelper", "Loading chats for user: " + userId);
        
        Cursor cursor = db.query(TABLE_CHATS, null, selection, selectionArgs, null, null, COLUMN_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                int chatUserId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                Log.d("DatabaseHelper", "Found chat - ID: " + id + ", UserID: " + chatUserId + ", Title: " + title);
                
                ChatHistory chat = new ChatHistory(title);
                chat.setId(String.valueOf(id));
                chatList.add(chat);
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "No chats found for user: " + userId);
        }
        cursor.close();
        return chatList;
    }

    public long createMessage(long chatId, String sender, String content, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_ID, chatId);
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_IMAGE_URL, imageUrl);
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        return db.insert(TABLE_MESSAGES, null, values);
    }

    public List<Message> getChatMessages(long chatId) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_SENDER, COLUMN_CONTENT, COLUMN_IMAGE_URL, COLUMN_CREATED_AT};
        String selection = COLUMN_CHAT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(chatId)};
        String orderBy = COLUMN_CREATED_AT + " ASC";

        Cursor cursor = db.query(TABLE_MESSAGES, columns, selection, selectionArgs, null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                String sender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    messages.add(new com.example.aiapp.model.ImageMessage(imageUrl, content, sender.equals("user")));
                } else {
                    messages.add(new Message(content, sender.equals("user")));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }

    public void deleteChat(long chatId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, COLUMN_CHAT_ID + " = ?", new String[]{String.valueOf(chatId)});
        db.delete(TABLE_CHATS, COLUMN_ID + " = ?", new String[]{String.valueOf(chatId)});
    }

    public void saveAISettings(long chatId, String model, float temperature, int maxTokens, String systemPrompt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_ID, chatId);
        values.put(COLUMN_MODEL, model);
        values.put(COLUMN_TEMPERATURE, temperature);
        values.put(COLUMN_MAX_TOKENS, maxTokens);
        values.put(COLUMN_SYSTEM_PROMPT, systemPrompt);
        db.insertWithOnConflict(TABLE_AI_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
} 