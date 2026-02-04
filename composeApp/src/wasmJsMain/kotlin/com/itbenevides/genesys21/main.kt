package com.itbenevides.genesys21

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // A configuração do Coil DEVE estar definida antes ou dentro do App()
    // Mas como o startComposeApp gerencia o Viewport, podemos injetar isso lá ou aqui.
    // Vamos chamar o inicializador comum que agora está no webMain
    startComposeApp()
}

/**
 * Injeta CSS globalmente para customizar a barra de rolagem do navegador 
 * de acordo com as cores do tema.
 */
@JsFun("(primary, background) => { " +
    "const style = document.createElement('style'); " +
    "style.innerHTML = ` " +
    "  ::-webkit-scrollbar { width: 10px; height: 10px; } " +
    "  ::-webkit-scrollbar-track { background: ${'$'}{background}; } " +
    "  ::-webkit-scrollbar-thumb { background: ${'$'}{primary}; border-radius: 5px; border: 2px solid ${'$'}{background}; } " +
    "  ::-webkit-scrollbar-thumb:hover { background: ${'$'}{primary}cc; } " +
    "  * { scrollbar-width: thin; scrollbar-color: ${'$'}{primary} ${'$'}{background}; } " +
    "`; " +
    "document.head.appendChild(style); " +
    "}")
private external fun jsInjectScrollbarStyles(primary: String, background: String)

/**
 * Função utilitária para converter Color para String Hex CSS
 */
private fun androidx.compose.ui.graphics.Color.toCssHex(): String {
    val r = (red * 255).toInt().toString(16).padStart(2, '0')
    val g = (green * 255).toInt().toString(16).padStart(2, '0')
    val b = (blue * 255).toInt().toString(16).padStart(2, '0')
    return "#${'$'}r${'$'}g${'$'}b"
}

@Composable
fun ThemeScrollbarEffect() {
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
    val primaryHex = colorScheme.primary.toCssHex()
    val backgroundHex = colorScheme.background.toCssHex()
    
    LaunchedEffect(primaryHex, backgroundHex) {
        jsInjectScrollbarStyles(primaryHex, backgroundHex)
    }
}
