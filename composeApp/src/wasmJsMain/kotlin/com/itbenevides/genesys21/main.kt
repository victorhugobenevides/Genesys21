package com.itbenevides.genesys21

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(additionalModules = listOf(viewModelModule))
    initializeFirebase()
    
    CanvasBasedWindow(
        title = "Genesys21",
        canvasElementId = "ComposeTarget"
    ) {
        // A configuração do Coil DEVE estar dentro do contexto Composable
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory())
                }
                .build()
        }
        
        App()
    }
}
