package com.android.server.voidos;

import android.os.SystemProperties;
import android.util.Log;

public class VoidSecureDistroRouter extends VoidDistroRouter {
    private static final String TAG = "VoidOS_SecureRouter";
    private String mHardenedFlavor;

    public VoidSecureDistroRouter() {
        super();
        initializeHardenedMatrix();
    }

    private void initializeHardenedMatrix() {
        if (!VoidDistroValidator.checkCallerIntegrity()) {
            Log.e(TAG, "Initialization aborted due to illegitimate process signature mapping.");
            return;
        }

        String rawFlag = SystemProperties.get("ro.voidos.distro_flavor", "BASE");
        this.mHardenedFlavor = VoidDistroValidator.sanitizeDistroFlag(rawFlag);
        
        Log.i(TAG, "Target system environment validated as: " + mHardenedFlavor);
    }

    public String getHardenedFlavor() {
        return this.mHardenedFlavor != null ? this.mHardenedFlavor : "BASE";
    }
}
