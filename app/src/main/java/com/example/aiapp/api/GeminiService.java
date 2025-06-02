package com.example.aiapp.api;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class GeminiService {
    private static final String TAG = "GeminiService";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String API_KEY = "AIzaSyDoYMqW4F47-8wc4SI97hyAz1ixd0itb7I";
    private final OkHttpClient client = new OkHttpClient();

    public interface ChatCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void sendMessage(String message, ChatCallback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();

            JSONObject textPart = new JSONObject();
            textPart.put("text", message);
            parts.put(textPart);

            content.put("parts", parts);
            contents.put(content);

            jsonBody.put("contents", contents);

        } catch (JSONException e) {
            callback.onError("Error creating request: " + e.getMessage());
            return;
        }

        String url = API_URL + "?key=" + API_KEY;

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                jsonBody.toString()
        );

        Request request = new Request.Builder()
                .url(url)
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

                    if (!response.isSuccessful()) {
                        callback.onError("API Error: " + response.code() + " - " + responseBody);
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (!jsonResponse.has("candidates") || jsonResponse.getJSONArray("candidates").length() == 0) {
                        callback.onError("Invalid API response: No candidates returned");
                        return;
                    }

                    String messageContent = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    callback.onResponse(messageContent.trim());

                } catch (JSONException e) {
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            }
        });
    }
}