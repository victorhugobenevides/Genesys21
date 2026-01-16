package com.itbenevides.genesys21

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Garante que o Koin e o Firebase iniciem antes da UI
    try {
        initKoin(additionalModules = listOf(viewModelModule))
        initializeFirebase()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // O ComposeViewport gerencia o Canvas automaticamente
    ComposeViewport(document.body!!) {
        App()
    }
}
