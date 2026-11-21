package com.example.galleryapp;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {
    public static Map<String, String> readConfig(Context context) {
        Map<String, String> config = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("config.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    config.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    public static String getLicenseKey(Context context) {
        Map<String, String> config = readConfig(context);
        return config.getOrDefault("LICENSE_KEY", "");
    }
}
