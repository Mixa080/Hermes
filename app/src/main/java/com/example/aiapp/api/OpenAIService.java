package com.example.aiapp.api;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;
// не викор.
public class OpenAIService {
    private static final String API_URL = "https://api.aimlapi.com/v1/chat/completions";
    private static final String API_KEY = "7439b35485a244b3a02b7ba08703965a";
    private final OkHttpClient client = new OkHttpClient();

    public interface ChatCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void sendMessage(String message, ChatCallback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            JSONArray messages = new JSONArray();
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", "user");
            messageObj.put("content", message);
            messages.put(messageObj);
            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.7);
        } catch (JSONException e) {
            callback.onError("Error creating request: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"),
            jsonBody.toString()
        );

        Request request = new Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    if (!response.isSuccessful()) {
                        String errorMessage;
                        if (jsonResponse.has("error")) {
                            String apiError = jsonResponse.getJSONObject("error").getString("message");
                            errorMessage = apiError;
                        } else {
                            errorMessage = "API Error: " + response.code();
                        }
                        Log.e("OpenAIService", "API Error: " + responseBody);
                        callback.onError(errorMessage);
                        return;
                    }

                    if (!jsonResponse.has("choices") || jsonResponse.getJSONArray("choices").length() == 0) {
                        callback.onError("Invalid API response: No choices returned");
                        return;
                    }

                    String messageContent = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                    callback.onResponse(messageContent.trim());
                } catch (JSONException e) {
                    Log.e("GroqService", "Response parsing error: " + e.getMessage());
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            }
        });
    }
} 