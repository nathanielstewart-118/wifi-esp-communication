package com.prtech.spiapp.utils;

import android.os.Handler;
import android.os.Looper;
import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class TCPHelper {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenThread;

    public void connect(String ip, int port, Consumer<String> onMessageReceived, Runnable onConnected, Consumer<String> onError) {
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                new Handler(Looper.getMainLooper()).post(onConnected);

                listenThread = new Thread(() -> {
                    String message;
                    try {
                        while ((message = reader.readLine()) != null) {
                            String finalMessage = message;
                            new Handler(Looper.getMainLooper()).post(() -> onMessageReceived.accept(finalMessage));
                        }
                    } catch (IOException e) {
                        onError.accept("Disconnected: " + e.getMessage());
                    }
                });
                listenThread.start();
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> onError.accept("Connection error: " + e.getMessage()));
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public void disconnect() {
        try {
            if (listenThread != null) listenThread.interrupt();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
