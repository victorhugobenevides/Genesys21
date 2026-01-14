package com.itbenevides.genesys21

actual fun syncUrlWithScreen(screen: Screen, pageId: String?) {
    // No iOS, não há uma barra de endereços para sincronizar.
    // Esta função fica vazia para permitir a compilação do código compartilhado.
    // Futuramente, isso poderia ser usado para atualizar o estado de Deep Linking.
}
