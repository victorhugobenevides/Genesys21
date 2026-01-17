package com.itbenevides.genesys21

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(additionalModules = listOf(viewModelModule))
    initializeFirebase()
    
    CanvasBasedWindow(
        title = "Genesys21",
        canvasElementId = "ComposeTarget"
    ) {
        App()
    }
}
