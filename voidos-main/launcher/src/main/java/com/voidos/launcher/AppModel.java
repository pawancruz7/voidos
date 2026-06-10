package com.voidos.launcher;

import android.graphics.drawable.Drawable;

public class AppModel {
    private String label;
    private String packageName;
    private Drawable icon;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public Drawable getIcon() { return icon; }
    public void setIcon(Drawable icon) { this.icon = icon; }
}
