package com.itbenevides.genesys21.util

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import okio.FileSystem
import okio.Path

/**
 * Expect declaration for the disk cache path.
 */
expect fun getDiskCachePath(context: PlatformContext): Path

/**
 * Expect declaration for the file system.
 */
expect fun getFileSystem(): FileSystem?

/**
 * Factory for creating a optimized ImageLoader for KMP.
 */
fun newImageLoader(context: PlatformContext): ImageLoader {
    val builder = ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory())
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .strongReferencesEnabled(true)
                .build()
        }
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)

    getFileSystem()?.let { fs ->
        builder.diskCache {
            DiskCache.Builder()
                .directory(getDiskCachePath(context))
                .fileSystem(fs)
                .maxSizeBytes(100L * 1024 * 1024) // 100MB
                .build()
        }
    }

    return builder.build()
}
