package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFileHandler(onResult: (String?) -> Unit): () -> Unit {
    return {
        // TODO: Implementar seletor de arquivo para Desktop (Swing/AWT) se necessário
        onResult(null)
    }
}

actual fun downloadFile(content: String, fileName: String) {
    // TODO: Implementar salvamento de arquivo local para Desktop
}
