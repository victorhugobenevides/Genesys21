package com.itbenevides.genesys21.presentation.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mmk.kmpauth.google.GoogleUser
import com.mmk.kmpauth.uihelper.google.GoogleButton

@Composable
actual fun GoogleSignInButton(
    modifier: Modifier,
    onTokenReceived: (idToken: String, accessToken: String?) -> Unit,
    onError: (String) -> Unit
) {
    GoogleButton(
        modifier = modifier,
        onResult = { googleUser: GoogleUser? ->
            val idToken = googleUser?.idToken
            val accessToken = googleUser?.accessToken
            if (idToken != null) {
                onTokenReceived(idToken, accessToken)
            } else {
                onError("Falha ao obter token do Google")
            }
        }
    )
}
