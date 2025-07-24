package com.example.myapplication.utils.communications;


import static com.example.myapplication.utils.communications.PacketParser.parseESPData;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.db.entity.ESPReceiveData;
import com.example.myapplication.utils.communications.PacketParser.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class WiFiSocketManager {
    private static WiFiSocketManager instance;

    // TCP
    private Socket tcpSocket;
    private OutputStream tcpOutputStream;
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

    private final Gson gson = new Gson();

    private WiFiSocketManager() {}

    public interface TCPMessageListener {
        void onMessageReceived(byte[] data);
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
        void onSuccess(byte[] data);
        void onError(Exception e);
    }

    // Connect once, keep socket open
    public void connectTCP(String ip, int port, Callback callback) {
        new Thread(() -> {
            try {
                tcpSocket = new Socket();
                tcpSocket.connect(new InetSocketAddress(ip, port), 10000);
                tcpOutputStream = tcpSocket.getOutputStream();
                tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                startTCPListening();  // 🔥 Start listening immediately after connection
                callback.onSuccess(new byte[10]);
            } catch (Exception e) {
                Log.e("SocketError", "I/O error during connection", e);
                callback.onError(e);
            }
        }).start();
    }

    private void startTCPListening() {
        tcpListening = true;
        tcpListenThread = new Thread(() -> {
            try {
                InputStream inputStream = tcpSocket.getInputStream();
                byte[] buffer = new byte[20]; // max packet size
                int bytesRead;
//                while (tcpListening && (line = tcpIn.readLine()) != null) {
//                    if (tcpMessageListener != null) {
//                        tcpMessageListener.onMessageReceived(line);
//                    }
//                }
                while (tcpListening && (bytesRead = inputStream.read(buffer)) != -1) {
                    byte[] data = Arrays.copyOf(buffer, bytesRead);
                    if (tcpMessageListener != null) {
                        tcpMessageListener.onMessageReceived(data);
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
    public void sendTCP(byte[] data, Callback callback) {
        new Thread(() -> {
            synchronized (tcpLock) {
                try {
                    if (tcpOutputStream != null) {
                        tcpOutputStream.write(data);
                        tcpOutputStream.flush();
                        callback.onSuccess(data);
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
    public void sendUDP(byte[] data, String targetIp, int targetPort, Callback callback) {
        new Thread(() -> {
            try {
                if (udpSocket == null || udpSocket.isClosed()) {
                    udpSocket = new DatagramSocket(); // system chooses available port
                }

                InetAddress address = InetAddress.getByName(targetIp);
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, targetPort);
                udpSocket.send(sendPacket);

                callback.onSuccess(data);

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
                        byte[] received = Arrays.copyOf(packet.getData(), packet.getLength());

                        // Parse the byte array into ESPData
                        Log.d("inform", "UDP Listening ...");
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
            mainHandler.post(() -> callback.onSuccess(new byte[10]));
        }
    }

    private void postError(Callback callback, Exception e) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(e));
        }
    }

}
