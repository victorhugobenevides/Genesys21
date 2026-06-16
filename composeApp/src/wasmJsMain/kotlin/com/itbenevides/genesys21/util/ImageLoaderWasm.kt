package com.itbenevides.genesys21.util

import coil3.PlatformContext
import okio.FileSystem
import okio.Path

/**
 * Actual implementation of disk cache path for the wasmJs target.
 * Uses the system temporary directory provided by Okio.
 */
actual fun getDiskCachePath(context: PlatformContext): Path {
    // Using the temporary directory directly; subdirectory creation is not required on wasmJs.
    return FileSystem.SYSTEM_TEMPORARY_DIRECTORY
}

actual fun getFileSystem(): FileSystem? = null
