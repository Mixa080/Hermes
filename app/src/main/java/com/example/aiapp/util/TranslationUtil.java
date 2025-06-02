package com.example.aiapp.util;

import android.os.StrictMode;
import okhttp3.*;
import org.json.JSONArray;

public class TranslationUtil {
    public static String translateToEnglish(String text) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            OkHttpClient client = new OkHttpClient();
            HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("translate.googleapis.com")
                .addPathSegment("translate_a")
                .addPathSegment("single")
                .addQueryParameter("client", "gtx")
                .addQueryParameter("sl", "auto")
                .addQueryParameter("tl", "en")
                .addQueryParameter("dt", "t")
                .addQueryParameter("q", text)
                .build();

            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                JSONArray arr = new JSONArray(json);
                JSONArray arr2 = arr.getJSONArray(0).getJSONArray(0);
                return arr2.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
} 