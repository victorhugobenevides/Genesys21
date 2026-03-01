package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFileHandler(onResult: (String?) -> Unit): () -> Unit {
    return {
        // Implementação para Android (File Picker) pode ser adicionada futuramente.
        // Por enquanto, retorna nulo para não quebrar o build.
        onResult(null)
    }
}

actual fun downloadFile(content: String, fileName: String) {
    // Implementação para Android (Download Manager ou Intent) pode ser adicionada futuramente.
}
