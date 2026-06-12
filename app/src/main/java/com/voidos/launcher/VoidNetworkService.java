package com.voidos.launcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class VoidNetworkService extends Service {
    private static final int TCP_PORT = 8888;
    private static final int UDP_DISCOVERY_PORT = 8889;
    private static final String SHARED_MESH_KEY = "VoidOSNetworkKey";
    private static final String CHANNEL_ID = "VoidOS_Core_Daemon";

    private ServerSocket serverSocket;
    private DatagramSocket udpSocket;
    private boolean isDaemonRunning = true;

    @Override
    public void onCreate() {
        super.onCreate();
        initForegroundNotificationSilence();
        startCoreMeshDaemon();
    }

    private void initForegroundNotificationSilence() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "VoidOS Network Kernel", 
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("VoidOS Core Active")
                    .setContentText("Mesh Network Daemon running 24x7 securely.")
                    .setSmallIcon(android.R.drawable.ic_menu_share)
                    .build();
        }
        startForeground(1337, notification);
    }

    private void startCoreMeshDaemon() {
        // TCP Thread: Continuous Encrypted Inbound Stream Receiver
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(TCP_PORT);
                while (isDaemonRunning) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> handleIncomingTcpConnection(socket)).start();
                }
            } catch (Exception e) { /* Suppressed Kernel Trace */ }
        }).start();

        // UDP Thread: Continuous Node Discovery Mesh Scanner
        new Thread(() -> {
            try {
                udpSocket = new DatagramSocket(UDP_DISCOVERY_PORT);
                byte[] buffer = new byte[1024];
                while (isDaemonRunning) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    handleIncomingUdpDiscovery(packet);
                }
            } catch (Exception e) { /* Suppressed Kernel Trace */ }
        }).start();
    }

    private void handleIncomingTcpConnection(Socket socket) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String incomingPayload = input.readLine();
            if (!TextUtils.isEmpty(incomingPayload)) {
                String clearText = decryptPayload(incomingPayload.trim());
                
                String sender = "Peer";
                String cleanMsg = clearText;
                if (clearText.contains("]: ")) {
                    sender = clearText.substring(0, clearText.indexOf("]: ") + 1);
                    cleanMsg = clearText.substring(clearText.indexOf("]: ") + 3);
                }

                // Directly commit log sequence to background SQLite vault
                HomeActivity.instance.saveMessageToLocalDb(sender, cleanMsg);
                
                // If UI is active, update terminal pipe immediately
                final String finalSender = sender;
                final String finalMsg = cleanMsg;
                if (HomeActivity.instance != null) {
                    HomeActivity.instance.runOnUiThread(() -> 
                        HomeActivity.instance.pushUiLog(finalSender + ": " + finalMsg, "#00FFFF"));
                }
            }
            input.close();
            socket.close();
        } catch (Exception e) { /* Trace dropped */ }
    }

    private void handleIncomingUdpDiscovery(DatagramPacket packet) {
        try {
            String message = new String(packet.getData(), 0, packet.getLength()).trim();
            String peerIP = packet.getAddress().getHostAddress();
            
            if (message.startsWith("VOID_PING:")) {
                String peerID = message.replace("VOID_PING:", "");
                if (HomeActivity.instance != null && !peerID.contains(HomeActivity.instance.getVoidSecureID())) {
                    HomeActivity.instance.setDiscoveredPeerIP(peerIP);
                    HomeActivity.instance.runOnUiThread(() -> 
                        HomeActivity.instance.pushUiLog("✨ Auto-Discovered Node: " + peerID + " at " + peerIP, "#FF00FF"));
                }
            }
        } catch (Exception e) { /* Trace dropped */ }
    }

    private String decryptPayload(String cipherText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SHARED_MESH_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.decode(cipherText, Base64.DEFAULT);
            return new String(cipher.doFinal(decodedBytes));
        } catch (Exception e) {
            return "[DECRYPTION_ERROR]";
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Forces Android kernel to recreate service if killed by low memory
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDaemonRunning = false;
        try {
            if (serverSocket != null) serverSocket.close();
            if (udpSocket != null) udpSocket.close();
        } catch (Exception e) { /* System cleanup shutdown */ }
    }
}
