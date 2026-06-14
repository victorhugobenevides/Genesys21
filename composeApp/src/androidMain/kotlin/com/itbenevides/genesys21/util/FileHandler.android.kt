package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFileHandler(onResult: (String?) -> Unit): () -> Unit {
    return {
        // TODO: Implementar usando Storage Access Framework se necessário no Android
        onResult(null)
    }
}

actual fun downloadFile(
    content: String,
    fileName: String,
) {
    // TODO: Implementar download no Android
}
