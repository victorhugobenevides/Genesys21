package com.itbenevides.genesys21.util

import android.content.Context
import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath

/**
 * Actual implementation of disk cache path for the Android target.
 * Uses the Android Context's cache directory.
 */
actual fun getDiskCachePath(context: PlatformContext): Path {
    // PlatformContext on Android is a Context instance.
    val androidContext = context as Context
    return androidContext.cacheDir.absolutePath.toPath()
}
