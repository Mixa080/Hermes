package com.example.aiapp.api;

import android.util.Log;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Callback;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import okhttp3.Call;

public class ImageGenerationService {
    private static final String TAG = "ImageGenerationService";
    private static final String POLLINATIONS_API_URL = "https://image.pollinations.ai/prompt/";
    private final OkHttpClient client;

    public interface ImageGenerationCallback {
        void onImageGenerated(String imageUrl);
        void onError(String error);
    }

    public ImageGenerationService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void generateImage(String prompt, ImageGenerationCallback callback) {
        try {
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString());
            long seed = System.currentTimeMillis();
            String url = POLLINATIONS_API_URL + encodedPrompt + "?nologo=true&seed=" + seed;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Log.d(TAG, "Sending request to Pollinations API: " + url);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error getting image: " + e.getMessage());
                    callback.onError("Failed to get image: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Error response from Pollinations: " + response.code());
                        callback.onError("Failed to get image: " + response.code());
                        return;
                    }

                    Log.d(TAG, "Image generated successfully: " + url);
                    callback.onImageGenerated(url);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing request: " + e.getMessage());
            callback.onError("Failed to prepare request: " + e.getMessage());
        }
    }

    public void translateAndGenerateImage(String prompt, ImageGenerationCallback callback) {
        OkHttpClient translateClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("q", prompt)
                .add("source", "auto")
                .add("target", "en")
                .build();

        Request translateRequest = new Request.Builder()
                .url("https://libretranslate.de/translate")
                .post(body)
                .addHeader("accept", "application/json")
                .build();

        translateClient.newCall(translateRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Translation failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Translation failed: " + response.code());
                    return;
                }
                String responseBody = response.body().string();
                try {
                    String translatedText = new JSONObject(responseBody).getString("translatedText");

                    generateImage(translatedText, callback);
                } catch (org.json.JSONException e) {
                    callback.onError("Failed to parse translation response: " + e.getMessage());
                }
            }
        });
    }
}