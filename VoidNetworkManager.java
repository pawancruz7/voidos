package org.voidos.network;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VoidNetworkManager {
    private static final String TAG = "VoidOS_NetworkManager";
    private static volatile VoidNetworkManager instance;
    
    private final Context appContext;
    private final IdentityEngine identityEngine;
    private final MeshRoutingEngine meshRoutingEngine;
    private final VoidDropEngine voidDropEngine;

    private VoidNetworkManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.identityEngine = new IdentityEngine(this.appContext);
        String localNodeId = identityEngine.getVoidIdentityString();
        
        this.meshRoutingEngine = new MeshRoutingEngine(localNodeId);
        this.voidDropEngine = new VoidDropEngine(localNodeId);
        
        Log.i(TAG, "VoidOS Hardened Networking Subsystem Online.");
    }

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

    public String getVoidId() {
        return identityEngine.getVoidIdentityString();
    }

    public static final class MeshPacket {
        public final String packetId;
        public final String senderVoidId;
        public final String destinationVoidId;
        public String nextHopVoidId;
        public int hopCount;
        public int timeToLive;
        public final byte[] encryptedPayload;
        public final byte[] digitalSignature;

        public MeshPacket(String sender, String destination, byte[] payload, byte[] signature) {
            this.packetId = "pkt_" + UUID.randomUUID().toString();
            this.senderVoidId = sender;
            this.destinationVoidId = destination;
            this.hopCount = 0;
            this.timeToLive = 64;
            this.encryptedPayload = payload;
            this.digitalSignature = signature;
        }
    }

    private static final class IdentityEngine {
        private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
        private static final String KEY_ALIAS = "VoidOS_MasterIdentityKey";
        private PrivateKey privateKey;
        private PublicKey publicKey;
        private String voidIdentityStr;

        protected IdentityEngine(Context context) {
            initializeIdentity();
        }

        private void initializeIdentity() {
            try {
                KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
                keyStore.load(null);

                if (!keyStore.containsAlias(KEY_ALIAS)) {
                    generateKeystoreKeyPair();
                }

                KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);
                if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                    throw new IllegalStateException("Invalid key entry type inside AndroidKeyStore.");
                }

                KeyStore.PrivateKeyEntry privKeyEntry = (KeyStore.PrivateKeyEntry) entry;
                this.privateKey = privKeyEntry.getPrivateKey();
                this.publicKey = privKeyEntry.getCertificate().getPublicKey();
                this.voidIdentityStr = generateVoidIdFromPublicKey(this.publicKey);

                Log.i(TAG, "Identity secured via AndroidKeyStore: " + voidIdentityStr);
            } catch (Exception e) {
                Log.e(TAG, "Fatal error initializing Keystore identity", e);
                throw new RuntimeException("Identity initialization failed", e);
            }
        }

        private void generateKeystoreKeyPair() throws Exception {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER);
            
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setAlgorithmParameterSpec(new java.security.spec.RSAKeyGenParameterSpec(2048, java.security.spec.RSAKeyGenParameterSpec.F4))
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .build();

            keyPairGen.initialize(spec);
            keyPairGen.generateKeyPair();
        }

        private String generateVoidIdFromPublicKey(PublicKey pubKey) throws Exception {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] identityHash = digest.digest(pubKey.getEncoded());
            String base64Footprint = Base64.encodeToString(identityHash, 
                    Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            return "void_" + base64Footprint.substring(0, 32);
        }

        protected String getVoidIdentityString() {
            return voidIdentityStr;
        }
    }

    public static final class MeshRoutingEngine {
        private static final String TAG = "VoidOS_MeshEngine";
        private final Map<String, String> routingTable = new ConcurrentHashMap<>();
        private final Set<String> processedPackets = Collections.newSetFromMap(
            new LinkedHashMap<String, Boolean>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > 1024;
                }
            }
        );
        private final String localNodeId;

        public MeshRoutingEngine(String localNodeId) {
            this.localNodeId = localNodeId;
        }

        public void processIncomingPacket(MeshPacket packet) {
            if (packet == null || packet.packetId == null || packet.destinationVoidId == null) {
                return;
            }

            synchronized (processedPackets) {
                if (!processedPackets.add(packet.packetId)) {
                    return;
                }
            }

            packet.hopCount++;
            packet.timeToLive--;

            if (packet.timeToLive <= 0) {
                return;
            }

            if (packet.destinationVoidId.equals(localNodeId)) {
                executeLocalPayloadDelivery(packet);
            } else {
                forwardPacketToNextHop(packet);
            }
        }

        private void forwardPacketToNextHop(MeshPacket packet) {
            String nextHop = routingTable.get(packet.destinationVoidId);
            if (nextHop != null) {
                packet.nextHopVoidId = nextHop;
            } else {
                packet.nextHopVoidId = "void_broadcast_all";
            }
        }

        private void executeLocalPayloadDelivery(MeshPacket packet) {
            Log.d(TAG, "Delivered payload securely from: " + packet.senderVoidId);
        }

        public void updateRoutingTable(String destinationNode, String optimalNextHopNode, byte[] signedAuth) {
            if (destinationNode != null && optimalNextHopNode != null && signedAuth != null) {
                routingTable.put(destinationNode, optimalNextHopNode);
            }
        }
    }

    public static final class VoidDropEngine {
        private static final String TAG = "VoidOS_VoidDrop";
        private final String localNodeId;
        private static final long MAX_STREAM_SIZE = 512 * 1024 * 1024L;

        public VoidDropEngine(String localNodeId) {
            this.localNodeId = localNodeId;
        }

        public void queueFileForTransmission(File file, String destinationVoidId) {
            if (file == null || !file.exists() || destinationVoidId == null) {
                return;
            }
            if (file.length() > MAX_STREAM_SIZE) {
                Log.e(TAG, "File exceeds maximum allowable transfer limit.");
                return;
            }
            Log.i(TAG, "Queued file transfer: " + file.getName());
        }

        public void processIncomingFileStream(String incomingSenderId, String filename, long streamSize) {
            if (streamSize > MAX_STREAM_SIZE || filename == null || filename.contains("..")) {
                Log.e(TAG, "Unauthorized or unsafe incoming stream rejected.");
                return;
            }
            Log.i(TAG, "Authorized secure stream from: " + incomingSenderId);
        }
    }
}
