package com.itbenevides.genesys21

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    // Inicializa o Koin se ele ainda não estiver rodando
    initKoinIos()
    
    App()
}
