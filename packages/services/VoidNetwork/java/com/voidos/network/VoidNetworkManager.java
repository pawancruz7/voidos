package com.voidos.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoidNetworkManager {
    private static final String TAG = "VoidNetworkManager";
    private static final int MESH_PORT = 18050;
    
    private final Context mContext;
    private final ExecutorService mExecutor;
    private DatagramSocket mMeshSocket;
    private volatile boolean mIsRunning;

    public VoidNetworkManager(Context context) {
        mContext = context;
        mExecutor = Executors.newCachedThreadPool();
        startMeshNode();
    }

    public void startMeshNode() {
        if (mIsRunning) {
            return;
        }
        mIsRunning = true;
        mExecutor.execute(this::listenForPackets);
    }

    private void listenForPackets() {
        try {
            mMeshSocket = new DatagramSocket(MESH_PORT);
            byte[] buffer = new byte[1024];
            
            while (mIsRunning) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                mMeshSocket.receive(packet);
                processIncomingPacket(packet);
            }
        } catch (IOException e) {
            Log.e(TAG, "Mesh socket error", e);
        }
    }

    public void broadcastPacket(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        mExecutor.execute(() -> {
            try {
                if (mMeshSocket == null || mMeshSocket.isClosed()) {
                    mMeshSocket = new DatagramSocket();
                }
                byte[] data = message.getBytes();
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, MESH_PORT);
                mMeshSocket.send(packet);
            } catch (IOException e) {
                Log.e(TAG, "Failed to broadcast packet", e);
            }
        });
    }

    private void processIncomingPacket(DatagramPacket packet) {
        String payload = new String(packet.getData(), 0, packet.getLength());
        Log.i(TAG, "Received mesh payload: " + payload);
    }

    public void shutdown() {
        mIsRunning = false;
        if (mMeshSocket != null && !mMeshSocket.isClosed()) {
            mMeshSocket.close();
        }
        mExecutor.shutdownNow();
    }
}
