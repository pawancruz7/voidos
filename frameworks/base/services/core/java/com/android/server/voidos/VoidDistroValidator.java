package com.android.server.voidos;

import android.os.Binder;
import android.util.Log;

public class VoidDistroValidator {
    private static final String TAG = "VoidOS_Shield";
    private static final int MAX_ALLOWED_FLAG_LENGTH = 12;

    public static String sanitizeDistroFlag(String rawFlag) {
        if (rawFlag == null || rawFlag.trim().isEmpty()) {
            Log.w(TAG, "Empty distro flag intercepted. Reverting to default.");
            return "BASE";
        }

        if (rawFlag.length() > MAX_ALLOWED_FLAG_LENGTH) {
            Log.e(TAG, "Buffer overflow vector detected in system property string.");
            return "BASE";
        }

        String cleanFlag = rawFlag.replaceAll("[^a-zA-Z_]", "").toUpperCase();

        if (cleanFlag.equals("BASE") || cleanFlag.equals("STEALTH") || cleanFlag.equals("KERNEL_MOD")) {
            return cleanFlag;
        }

        Log.w(TAG, "Unrecognized structural variant: " + cleanFlag + ". Defaulting to BASE.");
        return "BASE";
    }

    public static boolean checkCallerIntegrity() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 1000 || callingUid == 0) {
            return true;
        }
        Log.e(TAG, "Rogue process interaction blocked! Unauthorized UID: " + callingUid);
        return false;
    }
}
