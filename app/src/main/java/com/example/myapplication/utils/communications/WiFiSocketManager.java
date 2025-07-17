package com.example.myapplication.utils.communications;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.*;
import java.net.*;

public class WiFiSocketManager {
    private static WiFiSocketManager instance;

    // TCP
    private Socket tcpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;
    private Thread tcpListenThread;
    private boolean tcpListening;
    private final Object tcpLock = new Object();

    // UDP
    private DatagramSocket udpSocket;
    private Thread udpListenThread;
    private boolean udpListening;
    private final Object udpLock = new Object();

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private WiFiSocketManager() {}

    public interface TCPMessageListener {
        void onMessageReceived(String message);
    }

    public interface UDPMessageListener {
        void onUdpMessageReceived(String message, InetAddress senderAddress, int senderPort);
    }

    private TCPMessageListener tcpMessageListener;
    private UDPMessageListener udpMessageListener;

    public void setTCPMessageListener(TCPMessageListener listener) {
        this.tcpMessageListener = listener;
    }


    public static synchronized WiFiSocketManager getInstance() {
        if (instance == null) {
            instance = new WiFiSocketManager();
        }
        return instance;
    }

    public interface Callback {
        void onSuccess(String response);
        void onError(Exception e);
    }

    // Connect once, keep socket open
    public void connectTCP(String ip, int port, Callback callback) {
        new Thread(() -> {
            try {
                tcpSocket = new Socket(ip, port);
                tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
                tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

                startTCPListening();  // 🔥 Start listening immediately after connection

                callback.onSuccess("Connected to TCP");
            } catch (IOException e) {
                callback.onError(e);
            }
        }).start();
    }

    private void startTCPListening() {
        tcpListening = true;
        tcpListenThread = new Thread(() -> {
            try {
                String line;
                while (tcpListening && (line = tcpIn.readLine()) != null) {
                    if (tcpMessageListener != null) {
                        tcpMessageListener.onMessageReceived(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        tcpListenThread.start();
    }

    public void stopTCPListening() {
        tcpListening = false;
        if (tcpListenThread != null && tcpListenThread.isAlive()) {
            tcpListenThread.interrupt();
        }
    }

    public void disconnectTCP() {
        stopTCPListening();
        try {
            if (tcpSocket != null) tcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send data on existing TCP socket, receive response
    public void sendTCP(String message, Callback callback) {
        new Thread(() -> {
            synchronized (tcpLock) {
                try {
                    if (tcpOut != null) {
                        tcpOut.println(message);
                        tcpOut.flush();
                        callback.onSuccess("Sent: " + message);
                    } else {
                        callback.onError(new Exception("TCP output stream is null"));
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        }).start();
    }

    public boolean isTCPConnected() {
        return tcpSocket != null && tcpSocket.isConnected() && !tcpSocket.isClosed();
    }


    // ----- UDP Methods -----


    public void setUDPMessageListener(UDPMessageListener listener) {
        this.udpMessageListener = listener;
    }


    // Create UDP socket once and keep open
    public void createUDP(int localPort, Callback callback) {
        new Thread(() -> {
            try {
                udpSocket = new DatagramSocket(localPort);
                postSuccess(callback, "UDP socket created");
            } catch (SocketException e) {
                postError(callback, e);
            }
        }).start();
    }

    // Send UDP message and wait for response on existing UDP socket
    public void sendUDP(String message, String targetIp, int targetPort, Callback callback) {
        new Thread(() -> {
            try {
                if (udpSocket == null || udpSocket.isClosed()) {
                    udpSocket = new DatagramSocket(); // system chooses available port
                }

                byte[] data = message.getBytes();
                InetAddress address = InetAddress.getByName(targetIp);
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, targetPort);
                udpSocket.send(sendPacket);

                callback.onSuccess("UDP Message Sent");

            } catch (IOException e) {
                callback.onError(e);
            }
        }).start();
    }

    public void startUDPListening(int localPort, Callback callback ) {
        if (udpListening) return;
        udpListenThread = new Thread(() -> {
            synchronized (udpLock) {
                try {
                    if (udpSocket != null && !udpSocket.isClosed()) {
                        udpSocket.close(); // Prevent multiple bindings
                    }
                    udpSocket = new DatagramSocket(localPort);
                    udpListening = true;
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    while (udpListening) {
                        udpSocket.receive(packet);
                        Log.d("inform", "UDP Listening ...");
                        String received = new String(packet.getData(), 0, packet.getLength());
                        callback.onSuccess(received);
                    }

                } catch (IOException e) {
                    callback.onError(e);
                }
            }
        });
        udpListenThread.start();
    }

    public void stopUDPListening() {
        udpListening = false;
        if (udpListenThread != null && udpListenThread.isAlive()) {
            udpListenThread.interrupt();
        }
    }

    public void disconnectUDP() {
        stopUDPListening();
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }

    // Helper methods to post results on main thread
    private void postSuccess(Callback callback, String response) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(response));
        }
    }

    private void postError(Callback callback, Exception e) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(e));
        }
    }
}
