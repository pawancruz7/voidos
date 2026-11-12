package com.voidos.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.Color;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);

        TextView titleView = new TextView(this);
        titleView.setText("VoidOS");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(26.0f);
        titleView.setGravity(Gravity.CENTER);

        TextView statusView = new TextView(this);
        statusView.setText("Mesh Node: ACTIVE\nSecure Sanitizer: RUNNING\nTarget: Realme RMX1805");
        statusView.setTextColor(Color.parseColor("#00FF66"));
        statusView.setTextSize(14.0f);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 40, 0, 0);

        layout.addView(titleView);
        layout.addView(statusView);

        setContentView(layout);
    }

    @Override
    public void onBackPressed() {
    }
}
