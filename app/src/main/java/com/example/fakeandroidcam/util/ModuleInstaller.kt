package com.example.fakeandroidcam.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ModuleInstaller(private val context: Context) {
    private val tag = "ModuleInstaller"

    fun installModule(moduleUri: Uri): Boolean {
        return if (RootHelper.isMagiskInstalled(context)) {
            installViaMagiskIntent(moduleUri) || sideloadFallback(moduleUri)
        } else {
            sideloadFallback(moduleUri)
        }
    }

    private fun installViaMagiskIntent(moduleUri: Uri): Boolean {
        return try {
            val intent = Intent("com.topjohnwu.magisk.INSTALL_MODULE")
            intent.setPackage("com.topjohnwu.magisk")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = moduleUri
            context.startActivity(intent)
            Log.i(tag, "Requested Magisk to install module")
            true
        } catch (e: Exception) {
            Log.w(tag, "Magisk install intent failed", e)
            false
        }
    }

    private fun sideloadFallback(moduleUri: Uri): Boolean {
        return try {
            val input = context.contentResolver.openInputStream(moduleUri) ?: return false
            val target = File(context.cacheDir, "virtualcam-module.zip")
            copyToFile(input, target)
            val installCommand = "magisk --install-module ${target.absolutePath}"
            val success = RootHelper.runWithRoot(installCommand)
            Log.i(tag, "Sideloaded module via shell: $success")
            success
        } catch (e: Exception) {
            Log.e(tag, "Sideload fallback failed", e)
            false
        }
    }

    private fun copyToFile(inputStream: InputStream, destination: File) {
        inputStream.use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }
}
