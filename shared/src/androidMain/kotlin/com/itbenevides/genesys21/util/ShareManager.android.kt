package com.itbenevides.genesys21.util

import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Implementação Android usando Intent.ACTION_SEND.
 */
class AndroidShareManager(private val context: Context) : ShareManager {
    override fun shareLink(
        title: String,
        text: String,
        url: String,
    ) {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_TEXT, "$text\n\n$url")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        val chooser =
            Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(chooser)
    }
}

/**
 * Singleton que resolve o contexto via Koin (injetado no shared).
 */
actual val ShareManagerInstance: ShareManager by lazy {
    // Como o ShareManagerInstance é um val global, precisamos de uma forma de pegar o context.
    // Usaremos o Koin global.
    object : KoinComponent {
        val manager: ShareManager by inject()
    }.manager
}
