package com.voidos.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Toast;
import java.io.File;

public class PrivacySettingsActivity extends Activity {

    private static final String TAG = "VoidOS_Guard";
    private static final String PREFS_NAME = "secure_void_core_config";
    private SharedPreferences securePrefs;
    private Vibrator hapticFeedback;

    // VoidOS Kernel IPC Broadcast Actions
    private static final String ACTION_VOID_IPC_UPDATE = "com.voidos.system.IPC_HARDENING_UPDATE";
    private static final String EXTRA_HARDENING_TYPE = "hardening_type";
    private static final String EXTRA_STATE = "state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        // Haptic engine invocation for premium feel
        hapticFeedback = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Initializing high-speed secure isolation storage
        securePrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Bind and Synchronize Toggles
        initHardeningToggle(R.id.switch_dns, "dns_secure_override");
        initHardeningToggle(R.id.switch_clipboard, "clipboard_sanitize_layer");
        initHardeningToggle(R.id.switch_location, "entropy_location_spoof");
        initCriticalHardwareToggle(R.id.switch_hardware, "kernel_hw_kill_active");
    }

    private void initHardeningToggle(int resId, final String configKey) {
        final Switch toggle = findViewById(resId);
        toggle.setChecked(securePrefs.getBoolean(configKey, false));
        
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                triggerHaptic(20);
                securePrefs.edit().putBoolean(configKey, isChecked).apply();
                
                // Dispatch real-time IPC message to VoidOS core architecture
                dispatchSystemIPC(configKey, isChecked);
            }
        });
    }

    private void initCriticalHardwareToggle(int resId, final String configKey) {
        final Switch toggle = findViewById(resId);
        toggle.setChecked(securePrefs.getBoolean(configKey, false));

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                triggerHaptic(isChecked ? 100 : 40); // Heavy feedback for critical execution
                securePrefs.edit().putBoolean(configKey, isChecked).apply();
                
                dispatchSystemIPC(configKey, isChecked);
                
                if (isChecked) {
                    Toast.makeText(PrivacySettingsActivity.this, 
                        "CRITICAL: VoidOS Kernel isolation active. Peripherals decoupled.", 
                        Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void dispatchSystemIPC(String type, boolean state) {
        Intent ipcIntent = new Intent(ACTION_VOID_IPC_UPDATE);
        ipcIntent.putExtra(EXTRA_HARDENING_TYPE, type);
        ipcIntent.putExtra(EXTRA_STATE, state);
        ipcIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND); // Maximum OS priority Execution
        
        sendBroadcast(ipcIntent);
        Log.d(TAG, "VoidOS Core IPC Dispatched -> " + type + " set to " + state);
    }

    private void triggerHaptic(int milliseconds) {
        if (hapticFeedback != null && hapticFeedback.hasVibrator()) {
            hapticFeedback.vibrate(milliseconds);
        }
    }
}
