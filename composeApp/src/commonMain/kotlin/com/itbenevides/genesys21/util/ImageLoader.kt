package com.itbenevides.genesys21.util

import coil3.PlatformContext
import okio.Path

expect fun getDiskCachePath(context: PlatformContext): Path
