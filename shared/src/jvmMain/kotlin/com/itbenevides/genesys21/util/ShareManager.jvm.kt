package com.itbenevides.genesys21.util

actual val ShareManagerInstance: ShareManager =
    object : ShareManager {
        override fun shareLink(
            title: String,
            text: String,
            url: String,
        ) {
            // No-op no servidor
        }
    }
