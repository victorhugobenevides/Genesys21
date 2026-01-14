package com.itbenevides.genesys21

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Chama a inicialização que é específica para cada plataforma (JS ou WASM)
    initializeFirebase()

    ComposeViewport {
        App()
    }
}
