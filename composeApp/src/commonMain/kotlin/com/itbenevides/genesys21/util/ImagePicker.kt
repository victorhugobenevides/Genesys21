package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onResult: (ByteArray?) -> Unit): () -> Unit

data class SelectedFile(
    val bytes: ByteArray,
    val mimeType: String
)

@Composable
expect fun rememberFilePicker(onResult: (SelectedFile?) -> Unit): (accept: String, useCamera: Boolean) -> Unit
