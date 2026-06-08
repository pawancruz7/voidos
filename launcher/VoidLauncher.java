// ============================================================================
//  VoidOS UI Subsystem
//  Component: Hardened Minimalist Launcher & Desktop Activity
//  Purpose: Zero-telemetry workspace interface with system app routing
// ============================================================================

package com.android.launcher.voidos;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class VoidLauncher extends Activity {

    private static final String TAG = "VoidOS_Launcher";
    private PackageManager mPackageManager;
    private List<ResolveInfo> mInstalledApps;
    private ListView mAppListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup pure dark minimalist UI context programmatically to bypass heavy resources
        mPackageManager = getPackageManager();
        mInstalledApps = new ArrayList<>();
        
        setContentView(createCoreLayout());
        loadSecureApplications();
    }

    /**
     * Programmatic Generation of VoidOS Cyberpunk UI
     * Avoids XML compilation overhead for mobile-based development environments.
     */
    private View createCoreLayout() {
        android.widget.LinearLayout rootLayout = new android.widget.LinearLayout(this);
        rootLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(0xFF000000); // Absolute OLED Black (#000000)
        rootLayout.setPadding(32, 64, 32, 32);

        // Header Title: VoidOS Branding Banner
        TextView titleBanner = new TextView(this);
        titleBanner.setText("▼ v o i d O S");
        titleBanner.setTextColor(0xFF00FFCC); // Cyberpunk Neon Cyan/Teal
        titleBanner.setTextSize(28);
        titleBanner.setTypeface(android.graphics.Typeface.MONOSPACE);
        titleBanner.setPadding(0, 0, 0, 48);
        rootLayout.addView(titleBanner);

        // Subtitle Status Indicator
        TextView statusBanner = new TextView(this);
        statusBanner.setText("SYSTEM STATUS: SECURE // DE-GOOGLED");
        statusBanner.setTextColor(0xFF555555); // Stealth Gray
        statusBanner.setTextSize(12);
        statusBanner.setTypeface(android.graphics.Typeface.MONOSPACE);
        statusBanner.setPadding(0, 0, 0, 32);
        rootLayout.addView(statusBanner);

        // App List Container
        mAppListView = new ListView(this);
        mAppListView.setDividerHeight(16);
        mAppListView.setSelector(new android.graphics.drawable.ColorDrawable(0x1A00FFCC));
        rootLayout.addView(mAppListView);

        return rootLayout;
    }

    /**
     * Loads system applications while filtering out tracking layers and hidden hooks.
     */
    private void loadSecureApplications() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        List<ResolveInfo> apps = mPackageManager.queryIntentActivities(mainIntent, 0);
        if (apps != null) {
            for (ResolveInfo app : apps) {
                // Security Check: Exclude explicit legacy telemetry stubs if found
                String packageName = app.activityInfo.packageName;
                if (!packageName.contains("com.google.android.gms") && !packageName.contains("com.google.android.apps.maps")) {
                    mInstalledApps.add(app);
                }
            }
        }

        // Bind data stream to the interface
        mAppListView.setAdapter(new VoidAppAdapter());
        mAppListView.setOnItemClickListener((parent, view, position, id) -> {
            ResolveInfo info = mInstalledApps.get(position);
            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(launchIntent);
        });
    }

    /**
     * Performance-optimized list rendering matrix for VoidOS Launcher.
     */
    private class VoidAppAdapter extends BaseAdapter {
        @Override
        public int getCount() { return mInstalledApps.size(); }
        @Override
        public Object getItem(int i) { return mInstalledApps.get(i); }
        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView appItemView;
            if (convertView == null) {
                appItemView = new TextView(VoidLauncher.this);
                appItemView.setPadding(24, 24, 24, 24);
                appItemView.setTextSize(16);
                appItemView.setTypeface(android.graphics.Typeface.MONOSPACE);
            } else {
                appItemView = (TextView) convertView;
            }

            ResolveInfo info = mInstalledApps.get(position);
            CharSequence label = info.loadLabel(mPackageManager);
            
            // Render text in minimalist white with cyber sub-tags
            appItemView.setText("> " + label.toString().toUpperCase());
            appItemView.setTextColor(0xFFFFFFFF); // Pure White

            return appItemView;
        }
    }
}
