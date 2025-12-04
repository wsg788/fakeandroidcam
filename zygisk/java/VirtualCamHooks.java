package com.fakeandroidcam.zygisk;

import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class VirtualCamHooks {
    private static final String TAG = "VirtualCamHooks";
    private static final String CONFIG_PATH = "/data/adb/virtualcam/config.json";
    private static final Map<String, VirtualAsset> sAssetCache = new HashMap<>();

    static {
        System.loadLibrary("virtualcam");
    }

    public static native boolean nativeShouldHook(String packageName);
    public static native String nativeAssetFor(String packageName);
    public static native String nativeModeFor(String packageName);
    public static native void nativeReload();

    public static boolean shouldHook(String packageName) {
        return nativeShouldHook(packageName);
    }

    public static void refresh() {
        nativeReload();
    }

    public static void installCameraBridge(Camera camera, String packageName) {
        if (!shouldHook(packageName)) {
            return;
        }
        VirtualAsset asset = loadAsset(packageName);
        if (asset == null) {
            Log.w(TAG, "No asset for " + packageName);
            return;
        }
        camera.setPreviewCallback((data, cam) -> asset.feedPreview(cam));
    }

    public static void installCamera2Bridge(CameraManager manager, CameraDevice device, String packageName) {
        if (!shouldHook(packageName)) {
            return;
        }
        VirtualAsset asset = loadAsset(packageName);
        if (asset == null) {
            return;
        }
        asset.attachToCamera2(manager, device);
    }

    public static void installMediaRecorderBridge(MediaRecorder recorder, String packageName) {
        if (!shouldHook(packageName)) {
            return;
        }
        VirtualAsset asset = loadAsset(packageName);
        if (asset == null) {
            return;
        }
        asset.attachToRecorder(recorder);
    }

    private static VirtualAsset loadAsset(String packageName) {
        String assetPath = nativeAssetFor(packageName);
        String mode = nativeModeFor(packageName);
        if (assetPath == null || assetPath.isEmpty()) {
            return null;
        }
        if (sAssetCache.containsKey(assetPath)) {
            return sAssetCache.get(assetPath);
        }
        File path = new File("/data/adb/virtualcam/assets", assetPath);
        VirtualAsset asset = new VirtualAsset(path, mode);
        sAssetCache.put(assetPath, asset);
        return asset;
    }

    private static class VirtualAsset {
        private final File file;
        private final String mode;

        VirtualAsset(File file, String mode) {
            this.file = file;
            this.mode = mode;
        }

        void feedPreview(Camera camera) {
            try (FileInputStream fis = new FileInputStream(file);
                 FileChannel channel = fis.getChannel()) {
                ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                channel.read(buffer);
                camera.addCallbackBuffer(buffer.array());
            } catch (IOException e) {
                Log.e(TAG, "Preview feed failed", e);
            }
        }

        void attachToCamera2(CameraManager manager, CameraDevice device) {
            Log.i(TAG, "Camera2 bridge active for " + device.getId() + " via " + manager.getCameraIdList().length + " cameras");
            // Implementation placeholder: at runtime a Surface/Stream is supplied from native buffer queue.
        }

        void attachToRecorder(MediaRecorder recorder) {
            Log.i(TAG, "MediaRecorder bridge active in " + mode + " mode for " + file.getAbsolutePath());
            // Implementation placeholder: hook recorder surface and write frames from file.
        }
    }
}
