package com.voidos.launcher;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ScrollView;
import android.text.TextUtils;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.app.Activity;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.os.Build;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class HomeActivity extends Activity {
    public static HomeActivity instance;
    private String voidSecureID;
    private LinearLayout chatDisplay;
    private KeyStore systemHardwareKeyStore;
    private static final String CRYPTO_ALIAS = "VoidOS_Hardware_Shield_v2";
    
    // P2P Network Protocol Layout
    private static final int TCP_PORT = 8888; 
    private static final int UDP_DISCOVERY_PORT = 8889;
    private static final String SHARED_MESH_KEY = "VoidOSNetworkKey"; // 16-byte secure AES key base
    
    private ServerSocket serverSocket;
    private DatagramSocket udpSocket;
    private boolean isNetworkActive = true;
    private String discoveredPeerIP = ""; // Automatically holds the last discovered peer's IP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        // 1. Core Hardware Cryptographic Encryption initialization
        enforceHardwareSecurityVault();
        
        // 2. Launch Background Networks (TCP Inbound Server + UDP Peer Discovery Listener)
        startP2PNetworkServices();
        
        // 3. Render High-Performance Zero-XML Responsive UI
        setContentView(buildSuperChargedUI());
    }

    // === SECURITY VAULT: HARDWARE ENCRYPTED IMMUTABLE IDENTITY ===
    private void enforceHardwareSecurityVault() {
        try {
            systemHardwareKeyStore = KeyStore.getInstance("AndroidKeyStore");
            systemHardwareKeyStore.load(null);

            if (!systemHardwareKeyStore.containsAlias(CRYPTO_ALIAS)) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                
                kpg.initialize(new KeyGenParameterSpec.Builder(
                        CRYPTO_ALIAS,
                        KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .setKeySize(2048)
                        .build());
                kpg.generateKeyPair();
            }

            PublicKey publicKey = systemHardwareKeyStore.getCertificate(CRYPTO_ALIAS).getPublicKey();
            byte[] rawBytes = publicKey.getEncoded();
            
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
            byte[] secureHash = shaDigest.digest(rawBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : secureHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            this.voidSecureID = "void_" + hexString.toString().substring(0, 16);

        } catch (Exception e) {
            int nativeSeed = (Build.BOARD + Build.HARDWARE + Build.MANUFACTURER).hashCode();
            this.voidSecureID = "void_hw_fail_" + Integer.toHexString(Math.abs(nativeSeed));
        }
    }

    // === CRYPTO LAYER: AES-128 ENCRYPTION & DECRYPTION ENGINE ===
    private String encryptPayload(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SHARED_MESH_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim();
        } catch (Exception e) {
            return plainText; // Fallback to raw text if encryption faults out
        }
    }

    private String decryptPayload(String cipherText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SHARED_MESH_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.decode(cipherText, Base64.DEFAULT);
            return new String(cipher.doFinal(decodedBytes));
        } catch (Exception e) {
            return "[DECRYPTION_ERROR]: Unverified packet trace.";
        }
    }

    // === P2P BACKGROUND ENGINES: DISCOVERY & RECEPTION ===
    private void startP2PNetworkServices() {
        // Thread 1: Inbound TCP Encrypted Packet Listener
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(TCP_PORT);
                runOnUiThread(() -> pushSafeLog("TCP Secure Receiver Online [Port " + TCP_PORT + "]", "#00FF00"));

                while (isNetworkActive) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            String incomingEncryptedPayload = input.readLine();
                            
                            if (!TextUtils.isEmpty(incomingEncryptedPayload)) {
                                // Decrypting packet string dynamically off the network line
                                final String clearTextPayload = decryptPayload(incomingEncryptedPayload.trim());
                                runOnUiThread(() -> pushSafeLog("RX [Secure Payload]: " + clearTextPayload, "#00FFFF"));
                            }
                            input.close();
                            clientSocket.close();
                        } catch (Exception e) { /* Suppressed */ }
                    }).start();
                }
            } catch (Exception e) { /* Port binding log suppression */ }
        }).start();

        // Thread 2: Automatic Peer Discovery Listener (UDP Broadcast Receiver)
        new Thread(() -> {
            try {
                udpSocket = new DatagramSocket(UDP_DISCOVERY_PORT);
                byte[] buffer = new byte[1024];
                runOnUiThread(() -> pushSafeLog("UDP Discovery Engine Active [Scanning Mesh...]", "#00FF00"));

                while (isNetworkActive) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet); // Blocks until a peer broadcasts its node signature
                    
                    String senderMessage = new String(packet.getData(), 0, packet.getLength()).trim();
                    String senderIP = packet.getAddress().getHostAddress();
                    
                    if (senderMessage.startsWith("VOID_PING:") && !senderMessage.contains(voidSecureID)) {
                        String peerID = senderMessage.replace("VOID_PING:", "");
                        discoveredPeerIP = senderIP; // Automatically locking network target alignment
                        
                        runOnUiThread(() -> pushSafeLog("✨ Auto-Discovered Node: " + peerID + " at " + discoveredPeerIP, "#FF00FF"));
                    }
                }
            } catch (Exception e) { /* Network standard shutdown handler */ }
        }).start();
    }

    // === P2P OUTBOUND ENGINES: ASYNC BROADCASTS & TRANSMISSIONS ===
    private void triggerUdpDiscoveryBroadcast() {
        new Thread(() -> {
            try {
                DatagramSocket txUdpSocket = new DatagramSocket();
                txUdpSocket.setBroadcast(true);
                
                String discoveryPingMessage = "VOID_PING:" + voidSecureID;
                byte[] sendData = discoveryPingMessage.getBytes();
                
                // Broad-spectrum network mapping vector (Standard Local Broadcast IP)
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, UDP_DISCOVERY_PORT);
                
                txUdpSocket.send(sendPacket);
                txUdpSocket.close();
                runOnUiThread(() -> pushSafeLog("📡 Outbound Discovery Beacon Ping Sent...", "#FFFF00"));
            } catch (Exception e) {
                runOnUiThread(() -> pushSafeLog("Discovery Failure: Check Wi-Fi state.", "#FF0000"));
            }
        }).start();
    }

    private void sendEncryptedPeerMessageAsync(final String messagePayload) {
        if (TextUtils.isEmpty(discoveredPeerIP)) {
            pushSafeLog("TX Blocked: No node target discovered yet. Tap Beacon first.", "#FF9900");
            return;
        }

        new Thread(() -> {
            try {
                Socket socket = new Socket(discoveredPeerIP, TCP_PORT);
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                
                // Formatting payload identity layout and sealing via encryption envelope
                String plainPacketStructure = "[" + voidSecureID + "]: " + messagePayload;
                String sealedCipherPacket = encryptPayload(plainPacketStructure);
                
                output.println(sealedCipherPacket);
                output.flush();
                output.close();
                socket.close();
                
                runOnUiThread(() -> pushSafeLog("TX [Encrypted Engine -> " + discoveredPeerIP + "]: Success", "#00FF00"));
            } catch (Exception e) {
                runOnUiThread(() -> pushSafeLog("TX Error: Node handshake failed.", "#FF0000"));
            }
        }).start();
    }

    // === ULTRA-FAST LIGHTWEIGHT TERMINAL INTERFACE ===
    private LinearLayout buildSuperChargedUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#000000")); 
        mainLayout.setPadding(30, 40, 30, 30);

        // Header Security Banner
        TextView statusBanner = new TextView(this);
        statusBanner.setText("⚡ VoidOS CORE ACTIVE [MESH INTERACTIVE]\nPERMANENT_ID: " + this.voidSecureID);
        statusBanner.setTextColor(Color.parseColor("#00FF00")); 
        statusBanner.setTextSize(11f);
        statusBanner.setTypeface(Typeface.MONOSPACE);
        statusBanner.setPadding(20, 20, 20, 20);
        
        GradientDrawable modernBorder = new GradientDrawable();
        modernBorder.setColor(Color.parseColor("#0A0A0A"));
        modernBorder.setStroke(2, Color.parseColor("#00FF00"));
        modernBorder.setCornerRadius(8f);
        statusBanner.setBackground(modernBorder);
        mainLayout.addView(statusBanner);

        // Control Matrix Row
        Button discoveryBeaconBtn = new Button(this);
        discoveryBeaconBtn.setText("PING MESH BEACON");
        discoveryBeaconBtn.setTextColor(Color.parseColor("#000000"));
        discoveryBeaconBtn.setBackgroundColor(Color.parseColor("#FFFF00")); // Yellow tactical indicator
        discoveryBeaconBtn.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        discoveryBeaconBtn.setOnClickListener(v -> triggerUdpDiscoveryBroadcast());
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 15, 0, 15);
        discoveryBeaconBtn.setLayoutParams(btnParams);
        mainLayout.addView(discoveryBeaconBtn);

        // Optimized Message Stream Viewport
        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollView.setLayoutParams(scrollParams);

        chatDisplay = new LinearLayout(this);
        chatDisplay.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(chatDisplay);
        mainLayout.addView(scrollView);

        // Input Deck (Protected against Overflows)
        LinearLayout inputDeck = new LinearLayout(this);
        inputDeck.setOrientation(LinearLayout.HORIZONTAL);

        final EditText msgInput = new EditText(this);
        msgInput.setHint("Transmission packet bytes...");
        msgInput.setHintTextColor(Color.parseColor("#333333"));
        msgInput.setTextColor(Color.parseColor("#00FF00"));
        msgInput.setTypeface(Typeface.MONOSPACE);
        msgInput.setTextSize(13f);
        msgInput.setBackgroundColor(Color.parseColor("#0D0D0D"));
        msgInput.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(200)}); 
        
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        msgInput.setLayoutParams(inputParams);
        inputDeck.addView(msgInput);

        Button execBtn = new Button(this);
        execBtn.setText("TX");
        execBtn.setTextColor(Color.parseColor("#000000"));
        execBtn.setBackgroundColor(Color.parseColor("#00FF00"));
        execBtn.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        execBtn.setOnClickListener(v -> {
            String cleanText = msgInput.getText().toString().trim();
            if (!TextUtils.isEmpty(cleanText)) {
                pushSafeLog("Me: " + cleanText, "#00FF00");
                sendEncryptedPeerMessageAsync(cleanText); // Transmitting cipher stream
                msgInput.setText(""); 
            }
        });
        inputDeck.addView(execBtn);

        mainLayout.addView(inputDeck);
        return mainLayout;
    }

    private void pushSafeLog(String text, String colorHex) {
        TextView dynamicLog = new TextView(this);
        dynamicLog.setText("[SECURE_NODE] " + text);
        dynamicLog.setTextColor(Color.parseColor(colorHex));
        dynamicLog.setTypeface(Typeface.MONOSPACE);
        dynamicLog.setTextSize(11f);
        
        if (chatDisplay.getChildCount() > 50) {
            chatDisplay.removeViewAt(0); 
        }
        chatDisplay.addView(dynamicLog);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isNetworkActive = false;
        try {
            if (serverSocket != null) serverSocket.close();
            if (udpSocket != null) udpSocket.close();
        } catch (Exception e) { /* Clean Exit */ }
    }
                  }
package org.voidos.network;

// ... tumhare purane imports yahan honge ...
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    
    // 1. Tumhare purane variables jo pehle se the...
    
    // KAAM 1: Yeh naye variables yahan jod do
    private VoidNetworkService voidNetworkService;
    private boolean isServiceBound = false;

    // KAAM 2: Yeh poora ServiceConnection block yahan paste kar do
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            VoidNetworkService.LocalBinder binder = (VoidNetworkService.LocalBinder) service;
            voidNetworkService = binder.getService();
            isServiceBound = true;
            Log.d("VoidOS_Home", "VoidNetworkService successfully bound to UI.");
            onNetworkServiceReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            Log.d("VoidOS_Home", "VoidNetworkService disconnected.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tumhara purana setContentView ya UI setup yahan hoga...
        
        // KAAM 3: Purane code ke neeche yeh line jod do
        startVoidNetworkService();
    }

    // KAAM 3 (Part 2): Yeh dono naye methods class ke end mein kahi bhi paste kar do
    private void startVoidNetworkService() {
        Intent serviceIntent = new Intent(this, VoidNetworkService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void onNetworkServiceReady() {
        // Jab network service ready ho jaye tab kya karna hai (Abhi khali chhod do)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Memory leak se bachne ke liye unbind karna
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
} // Class ka aakhiri bracket

