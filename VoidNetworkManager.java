package org.voidos.network;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Core Network Manager for VoidOS.
 * Manages decentralized peer-to-peer operations, cryptographic identities,
 * and multi-hop routing infrastructure without central server dependency.
 * * @author VoidOS Developer Team
 * @version 1.0.0
 */
public final class VoidNetworkManager {
    private static final String TAG = "VoidOS_NetworkManager";
    private static volatile VoidNetworkManager instance;
    
    private final Context appContext;
    private final IdentityEngine identityEngine;

    /**
     * Private constructor enforcing the Singleton pattern.
     * Initializes core cryptographic identity engines.
     */
    private VoidNetworkManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.identityEngine = new IdentityEngine(this.appContext);
        
        Log.i(TAG, "VoidOS P2P Network Subsystem successfully initialized.");
        Log.i(TAG, "Local Node Cryptographic Identity: " + getVoidId());
    }

    /**
     * Returns the globally unique instance of VoidNetworkManager.
     * Implements double-checked locking for thread-safe initialization.
     */
    public static VoidNetworkManager getInstance(Context context) {
        if (instance == null) {
            synchronized (VoidNetworkManager.class) {
                if (instance == null) {
                    instance = new VoidNetworkManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Resolves the unique P2P cryptographic identifier of this local node.
     * @return String representation of the node's identity prefixed with 'void_'
     */
    public String getVoidId() {
        return identityEngine.getVoidIdentityString();
    }

    // =========================================================================
    // INNER ENGINE 1: CRYPTOGRAPHIC IDENTITY ENGINE
    // =========================================================================
    
    /**
     * Handles local RSA Key Pair generation, persistence, and verification.
     * Provides identity anchoring without relying on traditional emails or phone numbers.
     */
    private static final class IdentityEngine {
        private static final String KEY_ALGORITHM = "RSA";
        private static final int KEY_SIZE_BITS = 2048;
        private static final String PRIVATE_KEY_FILENAME = "void_private.key";
        private static final String PUBLIC_KEY_FILENAME = "void_public.key";

        private PrivateKey privateKey;
        private PublicKey publicKey;
        private String voidIdentityStr;

        protected IdentityEngine(Context context) {
            initializeIdentity(context);
        }

        private void initializeIdentity(Context context) {
            File privateKeyFile = new File(context.getFilesDir(), PRIVATE_KEY_FILENAME);
            File publicKeyFile = new File(context.getFilesDir(), PUBLIC_KEY_FILENAME);

            if (privateKeyFile.exists() && publicKeyFile.exists()) {
                try {
                    Log.d(TAG, "Locating existing cryptographic key stores...");
                    this.privateKey = loadPrivateKey(privateKeyFile);
                    this.publicKey = loadPublicKey(publicKeyFile);
                    this.voidIdentityStr = generateVoidIdFromPublicKey(this.publicKey);
                } catch (Exception e) {
                    Log.e(TAG, "Key storage corruption detected. Regenerating identity node keys.", e);
                    generateKeyPairSequence(context);
                }
            } else {
                Log.w(TAG, "First-boot sequence initiated: Provisioning clean asymmetric key pairs.");
                generateKeyPairSequence(context);
            }
        }

        private void generateKeyPairSequence(Context context) {
            try {
                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
                keyPairGen.initialize(KEY_SIZE_BITS);
                KeyPair keys = keyPairGen.generateKeyPair();

                this.privateKey = keys.getPrivate();
                this.publicKey = keys.getPublic();
                this.voidIdentityStr = generateVoidIdFromPublicKey(this.publicKey);

                persistKeyToLocalStorage(context, PRIVATE_KEY_FILENAME, privateKey.getEncoded());
                persistKeyToLocalStorage(context, PUBLIC_KEY_FILENAME, publicKey.getEncoded());
                Log.i(TAG, "Secure storage synchronized with new nodes: " + this.voidIdentityStr);

            } catch (Exception e) {
                Log.e(TAG, "Critical failure executing identity generation cryptographic primitive", e);
            }
        }

        private String generateVoidIdFromPublicKey(PublicKey pubKey) throws Exception {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] identityHash = digest.digest(pubKey.getEncoded());
            
            // Generate standard URL-safe Base64 footprint
            String base64Footprint = Base64.encodeToString(identityHash, 
                    Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            
            // Substring to 16 characters for optimized localized mesh network addressing routing tables
            return "void_" + base64Footprint.substring(0, 16);
        }

        private void persistKeyToLocalStorage(Context ctx, String name, byte[] data) throws Exception {
            try (FileOutputStream outStream = ctx.openFileOutput(name, Context.MODE_PRIVATE)) {
                outStream.write(data);
            }
        }

        private PrivateKey loadPrivateKey(File file) throws Exception {
            byte[] buffer = new byte[(int) file.length()];
            try (FileInputStream inStream = new FileInputStream(file)) {
                int bytesRead = inStream.read(buffer);
                if (bytesRead == 0) throw new Exception("Empty cryptographic structural key file encountered.");
            }
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            return KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(keySpec);
        }

        private PublicKey loadPublicKey(File file) throws Exception {
            byte[] buffer = new byte[(int) file.length()];
            try (FileInputStream inStream = new FileInputStream(file)) {
                int bytesRead = inStream.read(buffer);
                if (bytesRead == 0) throw new Exception("Empty cryptographic structural key file encountered.");
            }
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(keySpec);
        }

        protected String getVoidIdentityString() {
            return voidIdentityStr;
        }
    }

    // =========================================================================
    // INNER ENGINE 2: MULTI-HOP MESH ROUTING PIPELINE
    // =========================================================================
    // TODO: Dynamic Ad-Hoc On-Demand Distance Vector (AODV) / Mesh routing matrices will be integrated here.
}
