package com.example.fakeandroidcam.util

import android.net.Uri

object ConfigStore {
    var selectedMedia: Uri? = null
    var enabled: Boolean = false
    val scopedPackages: MutableSet<String> = linkedSetOf()
}
