package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFileHandler(onResult: (String?) -> Unit): () -> Unit {
    return {
        // Implementação stub para iOS
        onResult(null)
    }
}

actual fun downloadFile(content: String, fileName: String) {
    // Implementação stub para iOS
}
