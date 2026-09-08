package com.itbenevides.genesys21

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import coil3.compose.setSingletonImageLoaderFactory
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule
import com.itbenevides.genesys21.util.newImageLoader
import org.w3c.dom.HTMLElement
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun startComposeApp() {
    val statusText = document.getElementById("loading-status")
    val overlay = document.getElementById("loading-overlay")

    if (statusText != null) {
        statusText.innerHTML = "Configurando ambiente (Koin)..."
    }

    try {
        initKoin(additionalModules = listOf(viewModelModule))

        if (statusText != null) {
            statusText.innerHTML = "Conectando ao Firebase..."
        }
        initializeFirebase()

        // 1. Tenta pegar o elemento existente
        val existingContainer = document.getElementById("ComposeTarget")

        // ... (resto do container logic mantido) ...
        val container =
            if (existingContainer == null || existingContainer.nodeName.lowercase() == "canvas") {
                (document.createElement("div") as HTMLElement).apply {
                    id = "ComposeTargetDynamic"
                    setAttribute("style", "width: 100%; height: 100%; margin: 0; padding: 0;")
                    document.body?.appendChild(this)
                    existingContainer?.remove()
                }
            } else {
                existingContainer as HTMLElement
            }

        ComposeViewport(container) {
            setSingletonImageLoaderFactory { context -> newImageLoader(context) }
            App()
        }

        // Remove o overlay de carregamento quando o Compose estiver pronto
        window.setTimeout({
            overlay?.remove()
            null
        }, 1000)

    } catch (e: Exception) {
        if (statusText != null) {
            statusText.setAttribute("style", "color: orange; margin-top: 20px;")
            statusText.innerHTML = "ERRO NA INICIALIZAÇÃO: ${e.message}"
        }
        e.printStackTrace()
    }
}
