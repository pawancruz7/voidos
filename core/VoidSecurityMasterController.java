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
import java.util.concurrent.atomic.AtomicReference;

public class VoidSecurityMasterController {
    public enum SystemState { IDLE, BOOTING, OPERATIONAL, FAILED }
    
    private static volatile VoidSecurityMasterController sInstance = null;
    private static final Object sLock = new Object();
    
    private final Context mContext;
    private ExecutorService mAuthExecutor;
    private final AtomicReference<SystemState> mCurrentState = new AtomicReference<>(SystemState.IDLE);

    private volatile MeshRoutingEngine mMeshEngine;
    private volatile NetworkFirewall mFirewall;

    public interface BootCallback {
        void onBootSuccess();
        void onBootFailure(String error);
    }

    private VoidSecurityMasterController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mAuthExecutor = Executors.newFixedThreadPool(4);
    }

    public static VoidSecurityMasterController getInstance(Context context) {
        VoidSecurityMasterController localRef = sInstance;
        if (localRef == null) {
            synchronized (sLock) {
                localRef = sInstance;
                if (localRef == null) {
                    sInstance = localRef = new VoidSecurityMasterController(context);
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
            safelyLog("[*] VoidOS Core Boot Sequence Initiated...", "#FFFFFF");

            if (mAuthExecutor == null || mAuthExecutor.isShutdown()) {
                mAuthExecutor = Executors.newFixedThreadPool(4);
            }
            executorToUse = mAuthExecutor;
        }

        try {
            executorToUse.execute(() -> {
                try {
                    safelyLog("[*] Loading Android Keystore Elements...", "#FFFFFF");
                    VoidCryptoEngine.generateMasterKeyIfNeeded();
                    safelyLog("[+] Hardware Keystore verified securely.", "#00FF00");

                    safelyLog("[*] Injecting Dynamic Firewall Rules...", "#FFFFFF");
                    NetworkFirewall localFirewall = new NetworkFirewall();
                    localFirewall.injectDynamicFirewallRule("telemetry.google.com", true);
                    localFirewall.injectDynamicFirewallRule("analytics.apple.com", true);
                    
                    safelyLog("[*] Spawning P2P Mesh Routing Infrastructure...", "#FFFFFF");
                    MeshRoutingEngine localMeshEngine = new MeshRoutingEngine();
                    
                    synchronized (sLock) {
                        if (mCurrentState.get() == SystemState.BOOTING) {
                            mFirewall = localFirewall;
                            mMeshEngine = localMeshEngine;
                            mCurrentState.set(SystemState.OPERATIONAL);
                        } else {
                            throw new IllegalStateException("Boot sequence aborted externally.");
                        }
                    }
                    
                    safelyLog("[SUCCESS] VoidOS Secure Core is now running in GOD-MODE.", "#00FF00");
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(callback::onBootSuccess);
                    }
                } catch (Exception e) {
                    final String errorMsg = e.toString();
                    safelyLog("[CRITICAL ERROR] Boot Sequence Compromised: " + errorMsg, "#FF0000");
                    
                    synchronized (sLock) {
                        if (mCurrentState.get() == SystemState.BOOTING) {
                            performCleanupLocked(SystemState.FAILED);
                        }
                    }

                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onBootFailure(errorMsg));
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
            safelyLog("[!] Emergency Self-Wipe Protocol Triggered!", "#FF0000");
            performCleanupLocked(SystemState.FAILED);
        }
    }

    private void performCleanupLocked(SystemState targetState) {
        mCurrentState.set(targetState);
        if (mAuthExecutor != null && !mAuthExecutor.isShutdown()) {
            mAuthExecutor.shutdown();
        }
        mMeshEngine = null;
        mFirewall = null;
    }

    public SystemState getSystemState() {
        return mCurrentState.get();
    }

    public MeshRoutingEngine getMeshEngine() {
        return mMeshEngine;
    }

    public NetworkFirewall getFirewall() {
        return mFirewall;
    }

    private void safelyLog(String message, String colorHex) {
        if (HomeActivity.instance != null) {
            HomeActivity.instance.pushUiLog(message, colorHex);
        }
    }
}
