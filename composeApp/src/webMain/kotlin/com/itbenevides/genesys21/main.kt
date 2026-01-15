package com.itbenevides.genesys21

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.viewModelModule
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Tenta mudar a cor do banner de depuração para indicar que o Kotlin iniciou
    val debugHeader = document.getElementById("debug-header")
    if (debugHeader != null) {
        debugHeader.asDynamic().style.backgroundColor = "green"
        debugHeader.innerHTML = "KOTLIN/JS INICIADO - CONFIGURANDO KOIN..."
    }

    try {
        // Inicializa o Koin
        initKoin(additionalModules = listOf(viewModelModule))
        
        if (debugHeader != null) {
            debugHeader.innerHTML = "KOIN OK - INICIANDO FIREBASE..."
        }
        
        initializeFirebase()
        
        if (debugHeader != null) {
            debugHeader.innerHTML = "FIREBASE OK - CARREGANDO COMPOSE..."
        }

        val body = document.body ?: return

        ComposeViewport(body) {
            App()
        }
        
        // Se chegar aqui, remove o banner de debug
        window.setTimeout({
            debugHeader?.remove()
        }, 2000)

    } catch (e: Exception) {
        if (debugHeader != null) {
            debugHeader.asDynamic().style.backgroundColor = "orange"
            debugHeader.innerHTML = "ERRO KOTLIN: ${e.message}"
        }
        println("Erro: ${e.message}")
    }
}
