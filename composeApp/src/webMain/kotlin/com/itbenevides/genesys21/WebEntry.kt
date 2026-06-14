package com.itbenevides.genesys21

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule
import org.w3c.dom.HTMLElement
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun startComposeApp() {
    val debugHeader = document.getElementById("debug-header") as? HTMLElement
    if (debugHeader != null) {
        debugHeader.setAttribute("style", "background-color: green; color: white; padding: 10px; text-align: center;")
        debugHeader.innerHTML = "KOTLIN INICIADO - CONFIGURANDO KOIN..."
    }

    try {
        initKoin(additionalModules = listOf(viewModelModule))
        initializeFirebase()

        // 1. Tenta pegar o elemento existente
        val existingContainer = document.getElementById("ComposeTarget")

        // 2. Decide qual container usar.
        // Se o existente for nulo OU for um <canvas> (que não suporta Shadow DOM), criamos um novo.
        val container =
            if (existingContainer == null || existingContainer.nodeName.lowercase() == "canvas") {
                (document.createElement("div") as HTMLElement).apply {
                    id = "ComposeTargetDynamic"
                    setAttribute("style", "width: 100%; height: 100%; margin: 0; padding: 0;")
                    document.body?.appendChild(this)
                    // Se existia um canvas antigo, removemos para evitar conflitos visuais
                    existingContainer?.remove()
                }
            } else {
                existingContainer as HTMLElement
            }

        ComposeViewport(container) {
            setSingletonImageLoaderFactory { context ->
                ImageLoader.Builder(context)
                    .components {
                        add(KtorNetworkFetcherFactory())
                    }
                    .build()
            }
            App()
        }

        window.setTimeout({
            debugHeader?.remove()
            null
        }, 2000)
    } catch (e: Exception) {
        if (debugHeader != null) {
            debugHeader.setAttribute("style", "background-color: orange; color: white; padding: 10px; text-align: center;")
            debugHeader.innerHTML = "ERRO KOTLIN: ${e.message}"
        }
        e.printStackTrace()
    }
}
