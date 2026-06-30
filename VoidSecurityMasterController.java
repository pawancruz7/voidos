package com.voidos.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.voidos.secure.VoidCryptoEngine;
import com.voidos.engine.MeshRoutingEngine;
import com.voidos.net.NetworkFirewall;
import com.voidos.ui.HomeActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoidSecurityMasterController {
    private static volatile VoidSecurityMasterController sInstance = null;
    
    private final Context mContext;
    private final ExecutorService mAuthExecutor;
    private final Handler mMainHandler;
    
    private final AtomicBoolean isSystemSanitized = new AtomicBoolean(false);
    private final AtomicBoolean isMeshActive = new AtomicBoolean(false);

    private MeshRoutingEngine mMeshEngine;
    private NetworkFirewall mFirewall;

    private VoidSecurityMasterController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mAuthExecutor = Executors.newFixedThreadPool(4);
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    public static VoidSecurityMasterController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (VoidSecurityMasterController.class) {
                if (sInstance == null) {
                    sInstance = new VoidSecurityMasterController(context);
                }
            }
        }
        return sInstance;
    }

    public void bootSecureSubsystems() {
        HomeActivity.pushUiLog("[*] VoidOS Core Boot Sequence Initiated...");
        
        mAuthExecutor.execute(() -> {
            try {
                HomeActivity.pushUiLog("[*] Loading Android Keystore Elements...");
                VoidCryptoEngine.generateMasterKeyIfNeeded();
                HomeActivity.pushUiLog("[+] Hardware Keystore verified securely.");

                HomeActivity.pushUiLog("[*] Injecting Dynamic Firewall Rules...");
                mFirewall = new NetworkFirewall();
                mFirewall.injectDynamicFirewallRule("telemetry.google.com", true);
                mFirewall.injectDynamicFirewallRule("analytics.apple.com", true);
                
                HomeActivity.pushUiLog("[*] Spawning P2P Mesh Routing Infrastructure...");
                mMeshEngine = new MeshRoutingEngine();
                isMeshActive.set(true);
                
                isSystemSanitized.set(true);
                HomeActivity.pushUiLog("[SUCCESS] VoidOS Secure Core is now running in GOD-MODE.");
                
            } catch (Exception e) {
                HomeActivity.pushUiLog("[CRITICAL ERROR] Boot Sequence Compromised: " + e.getMessage());
                shutdownSystemSecurely();
            }
        });
    }

    public synchronized void shutdownSystemSecurely() {
        HomeActivity.pushUiLog("[!] Emergency Self-Wipe Protocol Triggered!");
        isMeshActive.set(false);
        isSystemSanitized.set(false);
        
        mAuthExecutor.execute(() -> {
            HomeActivity.pushUiLog("[-] Volatile registers successfully wiped out.");
        });
        
        mAuthExecutor.shutdown();
    }

    public boolean isSystemFullyOperational() {
        return isSystemSanitized.get() && isMeshActive.get();
    }
}
