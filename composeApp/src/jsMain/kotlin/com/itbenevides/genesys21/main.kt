package com.itbenevides.genesys21

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        initKoin(additionalModules = listOf(viewModelModule))
        initializeFirebase()

        val body = document.body ?: return

        ComposeViewport(body) {
            App()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
