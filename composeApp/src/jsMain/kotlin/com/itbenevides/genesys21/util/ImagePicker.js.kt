package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(onResult: (ByteArray?) -> Unit): () -> Unit {
    return { /* Not supported on JS yet */ }
}

@Composable
actual fun rememberFilePicker(onResult: (SelectedFile?) -> Unit): (String, Boolean) -> Unit {
    return { _, _ -> /* Not supported on JS yet */ }
}
