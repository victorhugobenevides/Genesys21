package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFileHandler(onResult: (String?) -> Unit): () -> Unit

expect fun downloadFile(content: String, fileName: String)
