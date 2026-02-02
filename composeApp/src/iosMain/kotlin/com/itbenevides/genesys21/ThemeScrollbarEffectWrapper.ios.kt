package com.itbenevides.genesys21

import androidx.compose.runtime.Composable

@Composable
actual fun ThemeScrollbarEffectWrapper() {
    // No iOS, a customização da barra de rolagem nativa via Compose 
    // não é suportada da mesma forma que no Web (CSS) ou Android.
    // Deixamos vazio para permitir a compilação.
}
