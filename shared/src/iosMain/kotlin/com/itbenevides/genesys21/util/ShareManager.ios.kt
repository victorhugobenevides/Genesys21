package com.itbenevides.genesys21.util

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

/**
 * Implementação iOS usando UIActivityViewController.
 */
actual val ShareManagerInstance: ShareManager =
    object : ShareManager {
        override fun shareLink(
            title: String,
            text: String,
            url: String,
        ) {
            val activityViewController =
                UIActivityViewController(
                    activityItems = listOf("$title\n$text\n$url"),
                    applicationActivities = null,
                )

            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                viewControllerToPresent = activityViewController,
                animated = true,
                completion = null,
            )
        }
    }
