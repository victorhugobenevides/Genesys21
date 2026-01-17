package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onResult: (ByteArray?) -> Unit): () -> Unit
