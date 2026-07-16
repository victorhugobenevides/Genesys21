package com.itbenevides.genesys21.presentation.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onTokenReceived: (idToken: String, accessToken: String?) -> Unit,
    onError: (String) -> Unit
)
