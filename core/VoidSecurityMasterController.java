package com.voidos.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.voidos.engine.MeshRoutingEngine;
import com.voidos.ui.HomeActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class VoidSecurityMasterController {
    public enum SystemState { IDLE, BOOTING, OPERATIONAL, FAILED }
    
    private static volatile VoidSecurityMasterController sInstance = null;
    private static final Object sLock = new Object();
    
    private ExecutorService mAuthExecutor;
    private final AtomicReference<SystemState> mCurrentState = new AtomicReference<>(SystemState.IDLE);

    private volatile MeshRoutingEngine mMeshEngine;
    private volatile NetworkFirewall mFirewall;

    public interface BootCallback {
        void onBootSuccess();
        void onBootFailure(String error);
    }

    private VoidSecurityMasterController() {
        this.mAuthExecutor = Executors.newFixedThreadPool(4);
    }

    public static VoidSecurityMasterController getInstance(Context context) {
        VoidSecurityMasterController localRef = sInstance;
        if (localRef == null) {
            synchronized (sLock) {
                localRef = sInstance;
                if (localRef == null) {
                    sInstance = localRef = new VoidSecurityMasterController();
                }
            }
        }
        return localRef;
    }

    public void bootSecureSubsystems(final BootCallback callback) {
        ExecutorService executorToUse;

        synchronized (sLock) {
            SystemState state = mCurrentState.get();
            if (state == SystemState.BOOTING || state == SystemState.OPERATIONAL) {
                return;
            }
            
            mCurrentState.set(SystemState.BOOTING);
            safelyLog("[*] Starting VoidOS Security...", "#FFFFFF");

            if (mAuthExecutor == null || mAuthExecutor.isShutdown()) {
                mAuthExecutor = Executors.newFixedThreadPool(4);
            }
            executorToUse = mAuthExecutor;
        }

        try {
            executorToUse.execute(() -> {
                try {
                    safelyLog("[*] Checking Security Key...", "#FFFFFF");
                    VoidCryptoEngine.generateMasterKeyIfNeeded();

                    safelyLog("[*] Setting up Framework Firewall...", "#FFFFFF");
                    NetworkFirewall localFirewall = new NetworkFirewall(); 
                    localFirewall.injectDynamicFirewallRule("telemetry.google.com", true);
                    localFirewall.injectDynamicFirewallRule("analytics.apple.com", true);
                    
                    MeshRoutingEngine localMeshEngine = new MeshRoutingEngine();
                    
                    synchronized (sLock) {
                        if (mCurrentState.get() == SystemState.BOOTING) {
                            mFirewall = localFirewall;
                            mMeshEngine = localMeshEngine;
                            mCurrentState.set(SystemState.OPERATIONAL);
                        } else {
                            throw new IllegalStateException("Aborted");
                        }
                    }
                    
                    safelyLog("[SUCCESS] All Systems Operational!", "#00FF00");
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(callback::onBootSuccess);
                    }
                } catch (Exception e) {
                    safelyLog("[ERROR] Setup Failed: " + e.getMessage(), "#FF0000");
                    synchronized (sLock) {
                        if (mCurrentState.get() == SystemState.BOOTING) {
                            performCleanupLocked(SystemState.FAILED);
                        }
                    }
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onBootFailure(e.toString()));
                    }
                }
            });
        } catch (Exception e) {
            synchronized (sLock) {
                performCleanupLocked(SystemState.FAILED);
            }
            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onBootFailure(e.toString()));
            }
        }
    }

    public void shutdownSystemSecurely() {
        synchronized (sLock) {
            safelyLog("[!] Emergency Shutdown Triggered!", "#FF0000");
            performCleanupLocked(SystemState.FAILED);
        }
    }

    private void performCleanupLocked(SystemState targetState) {
        mCurrentState.set(targetState);
        
        if (mFirewall != null) {
            mFirewall.clearAllRules(); 
        }

        if (mAuthExecutor != null && !mAuthExecutor.isShutdown()) {
            mAuthExecutor.shutdown();
        }
        mMeshEngine = null;
        mFirewall = null;
    }

    public SystemState getSystemState() { return mCurrentState.get(); }
    public MeshRoutingEngine getMeshEngine() { return mMeshEngine; }
    public NetworkFirewall getFirewall() { return mFirewall; }

    private void safelyLog(String message, String colorHex) {
        if (HomeActivity.instance != null) {
            HomeActivity.instance.pushUiLog(message, colorHex);
        }
    }
}
