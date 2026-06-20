package com.itbenevides.genesys21.util

import coil3.PlatformContext
import okio.FileSystem
import okio.Path

/**
 * Actual implementation of disk cache path for the iOS target.
 * Uses the system temporary directory.
 */
actual fun getDiskCachePath(context: PlatformContext): Path {
    return FileSystem.SYSTEM_TEMPORARY_DIRECTORY
}

actual fun getFileSystem(): FileSystem? = FileSystem.SYSTEM
