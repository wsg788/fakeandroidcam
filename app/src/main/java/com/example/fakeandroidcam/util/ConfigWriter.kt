package com.example.fakeandroidcam.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object ConfigWriter {
    private const val CONFIG_PATH = "/data/adb/virtualcam/config.json"
    private const val BROADCAST_ACTION = "com.example.fakeandroidcam.CONFIG_UPDATED"
    private const val BINDER_DESCRIPTOR = "com.example.fakeandroidcam.service.CONFIG"

    fun buildConfig(mediaUri: Uri?, enabled: Boolean, scopedPackages: Set<String>): JSONObject {
        val json = JSONObject()
        json.put("mediaUri", mediaUri?.toString())
        json.put("enabled", enabled)
        val packages = JSONArray()
        scopedPackages.forEach(packages::put)
        json.put("whitelistedPackages", packages)
        return json
    }

    fun writeConfig(context: Context, config: JSONObject): Boolean {
        val payload = config.toString()
        val base64 = Base64.encodeToString(payload.toByteArray(), Base64.NO_WRAP)
        val rootCommand = "mkdir -p /data/adb/virtualcam && echo $base64 | base64 -d > $CONFIG_PATH"
        if (RootHelper.isDeviceRooted() && RootHelper.runWithRoot(rootCommand)) {
            Log.i("ConfigWriter", "Wrote config via root shell")
            return true
        }
        sendBroadcastFallback(context, payload)
        sendBinderFallback(context, payload)
        return false
    }

    private fun sendBroadcastFallback(context: Context, payload: String) {
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra("config", payload)
        context.sendBroadcast(intent)
        Log.w("ConfigWriter", "Sent broadcast because root write failed")
    }

    private fun sendBinderFallback(context: Context, payload: String) {
        // In production this would interact with a bound service exposed by the virtual camera daemon.
        // We log here to indicate where binder IPC would be wired in.
        Log.w("ConfigWriter", "Binder ($BINDER_DESCRIPTOR) update invoked with payload length=${payload.length}")
    }
}
