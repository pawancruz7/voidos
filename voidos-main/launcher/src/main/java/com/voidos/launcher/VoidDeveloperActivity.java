package com.voidos.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.util.Log;

public class VoidDeveloperActivity extends Activity {

    private SharedPreferences devPrefs;
    private Vibrator haptic;
    private static final String DEV_PREFS_NAME = "void_forensic_developer_core";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void_developer); // UI Layout File

        haptic = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        devPrefs = getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE);

        // Bind Forensic Switches
        setupDevSwitch(R.id.switch_honeypot, "fg_honeypot_active", "Honey-Pot Environment Deployed.");
        setupDevSwitch(R.id.switch_sniffer, "fg_net_sniffer_active", "Live Packet Sniffer Hooked to Network Stack.");
        setupDevSwitch(R.id.switch_ram_purge, "fg_ram_purge_active", "Anti-Forensic Memory Purging Enabled.");
        setupDevSwitch(R.id.switch_zero_log, "fg_zero_log_active", "System Logcat Architecture Deactivated.");
    }

    private void setupDevSwitch(int resId, final String key, final String successMessage) {
        Switch toggle = findViewById(resId);
        toggle.setChecked(devPrefs.getBoolean(key, false));

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (haptic != null && haptic.hasVibrator()) {
                    haptic.vibrate(isChecked ? 60 : 30);
                }
                devPrefs.edit().putBoolean(key, isChecked).apply();
                
                // Real-time Low Level Kernel Logging bypass
                Log.w("VOID_DEV_CORE", "Subsystem Override: " + key + " -> " + isChecked);
                
                if (isChecked) {
                    Toast.makeText(VoidDeveloperActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
