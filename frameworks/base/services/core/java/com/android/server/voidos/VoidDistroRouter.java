package com.android.server.voidos;

import android.os.SystemProperties;
import android.util.Log;

public class VoidDistroRouter {
    private static final String TAG = "VoidOS_DistroRouter";

    public enum DistroFlavor {
        BASE,
        STEALTH,
        KERNEL_MOD
    }

    private final DistroFlavor mActiveFlavor;

    public VoidDistroRouter() {
        String distroFlag = SystemProperties.get("ro.voidos.distro_flavor", "BASE");
        DistroFlavor flavor;
        try {
            flavor = DistroFlavor.valueOf(distroFlag.toUpperCase());
        } catch (IllegalArgumentException e) {
            flavor = DistroFlavor.BASE;
        }
        mActiveFlavor = flavor;
        
        Log.i(TAG, "Subsystem initialized with flavor: " + mActiveFlavor);
        enforceDistroArchitecture();
    }

    private void enforceDistroArchitecture() {
        switch (mActiveFlavor) {
            case BASE:
                Log.i(TAG, "VoidOS Base flavor active.");
                break;

            case STEALTH:
                Log.w(TAG, "VoidOS Stealth Engine armed.");
                SystemProperties.set("persist.sys.voidos.sandbox", "strict");
                break;

            case KERNEL_MOD:
                Log.v(TAG, "VoidOS Kernel-Mod variant unlocked.");
                SystemProperties.set("ro.debuggable", "1");
                SystemProperties.set("persist.sys.voidos.freedom_mode", "1");
                break;
        }
    }

    public DistroFlavor getActiveFlavor() {
        return mActiveFlavor;
    }
}
