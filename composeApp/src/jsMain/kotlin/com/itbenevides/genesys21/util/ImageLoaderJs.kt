package com.itbenevides.genesys21.util

import coil3.PlatformContext
import okio.FileSystem
import okio.Path

import okio.Path.Companion.toPath

actual fun getDiskCachePath(context: PlatformContext): Path {
    return "/tmp".toPath()
}

actual fun getFileSystem(): FileSystem? = null
