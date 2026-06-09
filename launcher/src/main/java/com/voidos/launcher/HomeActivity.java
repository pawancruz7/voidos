package com.voidos.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.List;

public class HomeActivity extends Activity {

    private PackageManager packageManager;
    private GridView appsGrid;
    private LinearLayout quickSettingsPanel;
    private boolean isFlashlightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        packageManager = getPackageManager();
        appsGrid = findViewById(R.id.apps_grid);
        quickSettingsPanel = findViewById(R.id.quick_settings_panel);

        // Views Initialization
        LinearLayout statusBar = findViewById(R.id.status_bar);
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnHome = findViewById(R.id.btn_home);
        ImageView btnRecents = findViewById(R.id.btn_recents);
        
        Button toggleWifi = findViewById(R.id.toggle_wifi);
        Button toggleFlashlight = findViewById(R.id.toggle_flashlight);

        // --- QUICK SETTINGS PANEL TOGGLE ---
        statusBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quickSettingsPanel.getVisibility() == View.GONE) {
                    quickSettingsPanel.setVisibility(View.VISIBLE);
                } else {
                    quickSettingsPanel.setVisibility(View.GONE);
                }
            }
        });

        // --- NAVIGATION BUTTONS LOGIC ---
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickSettingsPanel.setVisibility(View.GONE);
                loadInstalledApps(); // Refresh home screen
                Toast.makeText(HomeActivity.this, "Home Pressed", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quickSettingsPanel.getVisibility() == View.VISIBLE) {
                    quickSettingsPanel.setVisibility(View.GONE);
                } else {
                    Toast.makeText(HomeActivity.this, "Back Pressed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRecents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Recents Panel", Toast.LENGTH_SHORT).show();
            }
        });

        // --- QUICK TOGGLES FUNCTIONALITY ---
        
        // 1. Flashlight Control
        toggleFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                try {
                    String cameraId = cameraManager.getCameraIdList()[0];
                    isFlashlightOn = !isFlashlightOn;
                    cameraManager.setTorchMode(cameraId, isFlashlightOn);
                    toggleFlashlight.setText(isFlashlightOn ? "Flashlight: ON" : "Flashlight: OFF");
                } catch (CameraAccessException | IllegalArgumentException e) {
                    Toast.makeText(HomeActivity.this, "Flashlight error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 2. WiFi Status (Just a UI simulation for custom ROM wrapper)
        toggleWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Opening WiFi Settings...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            }
        });

        loadInstalledApps();
    }

    private void loadInstalledApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, 0);
        AppsAdapter adapter = new AppsAdapter(this, availableActivities, packageManager);
        appsGrid.setAdapter(adapter);
    }
}
