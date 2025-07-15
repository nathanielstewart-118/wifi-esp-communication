package com.example.myapplication.utils;

import android.os.Handler;
import android.os.Looper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Consumer;

public class UDPHelper {

    private DatagramSocket socket;
    private Thread listenThread;

    /**
     * Send UDP message to target IP and port.
     */
    public void sendUdpMessage(String ip, int port, String message) {
        new Thread(() -> {
            try {
                InetAddress address = InetAddress.getByName(ip);
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                DatagramSocket sendSocket = new DatagramSocket();
                sendSocket.send(packet);
                sendSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Start listening for incoming UDP messages on given port.
     */
    public void startListening(int port, Consumer<String> onMessageReceived) {
        listenThread = new Thread(() -> {
            try {
                socket = new DatagramSocket(port);
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (!Thread.currentThread().isInterrupted()) {
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    new Handler(Looper.getMainLooper()).post(() -> onMessageReceived.accept(message));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    /**
     * Stop listening for UDP messages.
     */
    public void stopListening() {
        if (listenThread != null) {
            listenThread.interrupt();
            listenThread = null;
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
