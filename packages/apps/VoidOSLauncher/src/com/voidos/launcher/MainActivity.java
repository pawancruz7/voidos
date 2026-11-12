package com.voidos.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView view = new TextView(this);
        view.setText("VoidOS - Secure Mesh Node Active");
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(18.0f);

        setContentView(view);
    }

    @Override
    public void onBackPressed() {
    }
}
