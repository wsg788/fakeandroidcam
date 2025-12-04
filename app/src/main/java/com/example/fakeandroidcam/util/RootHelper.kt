package com.example.fakeandroidcam.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object RootHelper {
    private const val TAG = "RootHelper"

    fun isDeviceRooted(): Boolean {
        val paths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        if (paths.any { File(it).exists() }) {
            return true
        }
        return try {
            val process = ProcessBuilder("su", "-c", "id")
                .redirectErrorStream(true)
                .start()
            process.waitFor(1500, TimeUnit.MILLISECONDS)
            val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
            output.contains("uid=0")
        } catch (e: Exception) {
            Log.w(TAG, "su check failed", e)
            false
        }
    }

    fun isMagiskInstalled(context: Context): Boolean {
        val magiskDirs = listOf("/sbin/.magisk", "/data/adb/magisk")
        if (magiskDirs.any { File(it).exists() }) {
            return true
        }
        return try {
            context.packageManager.getPackageInfo("com.topjohnwu.magisk", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun runWithRoot(command: String): Boolean {
        return try {
            val process = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start()
            val exit = if (process.waitFor(3, TimeUnit.SECONDS)) process.exitValue() else -1
            Log.d(TAG, "Command '$command' exited with $exit")
            exit == 0
        } catch (e: Exception) {
            Log.e(TAG, "Root command failed", e)
            false
        }
    }
}
