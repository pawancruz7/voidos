package com.android.server.voidos;

import android.content.Context;
import android.os.Binder;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

public class VoidGhostModeService {
    private static final String TAG = "VoidOS_GhostMode";
    private final Context mContext;
    private boolean mIsGhostModeActive = true;
    private final Set<String> mBlacklistedDomains;

    public VoidGhostModeService(Context context) {
        this.mContext = context;
        this.mBlacklistedDomains = new HashSet<>();
        initializeBlacklist();
    }

    private void initializeBlacklist() {
        mBlacklistedDomains.add("telemetry.query.google.com");
        mBlacklistedDomains.add("graph.facebook.com");
        mBlacklistedDomains.add("app-measurement.com");
        mBlacklistedDomains.add("analytics.tiktok.com");
        mBlacklistedDomains.add("ads.xiaomi.com");
    }

    public boolean verifyNetworkAccess(String domain, int callingUid) {
        if (callingUid < 10000) {
            return true;
        }

        if (mIsGhostModeActive && mBlacklistedDomains.contains(domain)) {
            Log.w(TAG, "Blocked tracking request to: " + domain + " from UID: " + callingUid);
            return false;
        }

        return true;
    }

    public void setGhostMode(boolean active) {
        final long token = Binder.clearCallingIdentity();
        try {
            this.mIsGhostModeActive = active;
            Log.i(TAG, "Ghost Mode state changed to: " + (active ? "STRICT" : "DISABLED"));
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
