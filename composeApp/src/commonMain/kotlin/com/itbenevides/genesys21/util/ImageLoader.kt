package com.itbenevides.genesys21.util

import coil3.PlatformContext
import okio.Path

/**
 * Expect declaration for the disk cache path.
 * Implementations should provide a valid path for storing image cache.
 */
expect fun getDiskCachePath(context: PlatformContext): Path
