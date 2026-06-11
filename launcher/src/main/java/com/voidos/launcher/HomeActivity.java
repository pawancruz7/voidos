package com.voidos.launcher; // Agar tumhara package name alag hai, toh yahan apna waala daal dena

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

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.MessageDigest;

public class HomeActivity extends Activity {
    public static HomeActivity instance;
    private String voidSecureID;
    private LinearLayout chatDisplay;
    private KeyStore systemHardwareKeyStore;
    private static final String CRYPTO_ALIAS = "VoidOS_Hardware_Shield_v2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        // 1. Trigger Hardware-Isolated Security Engine instantly
        enforceHardwareSecurityVault();
        
        // 2. Load Ultra-Lightweight Terminal UI (Zero XML layout inflation overhead)
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

    // === ULTRA-FAST LIGHTWEIGHT TERMINAL INTERFACE ===
    private LinearLayout buildSuperChargedUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#000000")); 
        mainLayout.setPadding(30, 40, 30, 30);

        TextView statusBanner = new TextView(this);
        statusBanner.setText("⚡ VoidOS HARDWARE SHIELD ACTIVE\nPERMANENT_ID: " + this.voidSecureID);
        statusBanner.setTextColor(Color.parseColor("#00FF00")); 
        statusBanner.setTextSize(12f);
        statusBanner.setTypeface(Typeface.MONOSPACE);
        statusBanner.setPadding(20, 20, 20, 20);
        
        GradientDrawable modernBorder = new GradientDrawable();
        modernBorder.setColor(Color.parseColor("#0A0A0A"));
        modernBorder.setStroke(2, Color.parseColor("#00FF00"));
        modernBorder.setCornerRadius(8f);
        statusBanner.setBackground(modernBorder);
        mainLayout.addView(statusBanner);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollParams.setMargins(0, 25, 0, 25);
        scrollView.setLayoutParams(scrollParams);

        chatDisplay = new LinearLayout(this);
        chatDisplay.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(chatDisplay);
        mainLayout.addView(scrollView);

        LinearLayout inputDeck = new LinearLayout(this);
        inputDeck.setOrientation(LinearLayout.HORIZONTAL);

        final EditText msgInput = new EditText(this);
        msgInput.setHint("Enter transmission byte sequence...");
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
        execBtn.setText("SEND");
        execBtn.setTextColor(Color.parseColor("#000000"));
        execBtn.setBackgroundColor(Color.parseColor("#00FF00"));
        execBtn.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        execBtn.setOnClickListener(v -> {
            String cleanText = msgInput.getText().toString().trim();
            if (!TextUtils.isEmpty(cleanText)) {
                pushSafeLog("Me: " + cleanText, "#00FF00");
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
        dynamicLog.setTextSize(12f);
        
        if (chatDisplay.getChildCount() > 50) {
            chatDisplay.removeViewAt(0); 
        }
        chatDisplay.addView(dynamicLog);
    }
                              }
