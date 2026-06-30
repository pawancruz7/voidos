package com.voidos.core;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.KeyGenerator;

class VoidCryptoEngine {
    static void generateMasterKeyIfNeeded() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (!keyStore.containsAlias("VoidOS_Master_Root_Key")) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder("VoidOS_Master_Root_Key",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256).build());
            keyGenerator.generateKey();
        }
    }
}

class NetworkFirewall {
    private final Set<String> mBlockedDomains = new HashSet<>();

    NetworkFirewall() {
    }

    void injectDynamicFirewallRule(String domain, boolean block) throws Exception {
        if (domain == null || domain.isEmpty()) return;

        if (!domain.matches("^[a-zA-Z0-9.-]+$")) {
            throw new IllegalArgumentException("Malicious domain pattern detected!");
        }

        String[] command = {
            "iptables", 
            block ? "-A" : "-D", 
            "OUTPUT", 
            "-d", 
            domain, 
            "-j", 
            "DROP"
        };

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("iptables failed with exit code: " + exitCode);
            }

            synchronized (mBlockedDomains) {
                if (block) mBlockedDomains.add(domain);
                else mBlockedDomains.remove(domain);
            }
        } finally {
            if (process != null) process.destroy();
        }
    }

    void clearAllRules() {
        Set<String> domainsToUnblock;
        synchronized (mBlockedDomains) {
            domainsToUnblock = new HashSet<>(mBlockedDomains);
            mBlockedDomains.clear();
        }
        for (String domain : domainsToUnblock) {
            try {
                injectDynamicFirewallRule(domain, false);
            } catch (Exception ignored) {
            }
        }
    }
}

public class VoidSubsystemExtensions {
}
