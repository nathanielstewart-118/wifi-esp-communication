package com.example.myapplication.utils;

import android.util.Log;

import com.example.myapplication.interfaces.LogCallback;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.HttpURLConnection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;




public class LogHelper {

    public static void sendLog(
            final String urlString,
            final String method,
            final String jsonBody,
            final String bearerToken
    ) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString.trim());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method.toUpperCase());
                conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Type", "multipart/form-data");
                conn.setDoOutput(true);

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = now.format(formatter);
                Gson gson = new Gson();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("datetime", formattedDateTime);
                jsonObject.addProperty("message", jsonBody);

//                InetAddress address = InetAddress.getByName("https://s1385328.eu-nbg-2.betterstackdata.com");
//                Log.d("LogHelper", "Resolved IP: " + address.getHostAddress());

                if (jsonBody != null && !jsonBody.isEmpty()) {
                    OutputStream os = conn.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
                    writer.write(gson.toJson(jsonObject));
                    writer.flush();
                    writer.close();
                    os.close();
                }

                int responseCode = conn.getResponseCode();
                Log.d("LogHelper", "Response Code: " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Log.d("LogHelper", "Response Body: " + response.toString());

            } catch (IOException e) {
                Log.e("LogHelper", "Exception: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    public static void sendLogWithCallback(final String urlString,
                                    final String method,
                                    final String jsonBody,
                                    final String bearerToken,
                                    LogCallback callback ) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString.trim());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method.toUpperCase());
                conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Type", "multipart/form-data");
                conn.setDoOutput(true);

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = now.format(formatter);
                Gson gson = new Gson();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("datetime", formattedDateTime);
                jsonObject.addProperty("message", jsonBody);

//                InetAddress address = InetAddress.getByName("https://s1385328.eu-nbg-2.betterstackdata.com");
//                Log.d("LogHelper", "Resolved IP: " + address.getHostAddress());

                if (jsonBody != null && !jsonBody.isEmpty()) {
                    OutputStream os = conn.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
                    writer.write(gson.toJson(jsonObject));
                    writer.flush();
                    writer.close();
                    os.close();
                }

                int responseCode = conn.getResponseCode();
                Log.d("LogHelper", "Response Code: " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                if (responseCode == 202) {
                    callback.onSuccess();
                }
                else callback.onFailure(new Exception());
            } catch (Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        }).start();
    }
}

