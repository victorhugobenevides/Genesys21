package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(onResult: (ByteArray?) -> Unit): () -> Unit {
    return { /* Not supported on JS yet */ }
}
