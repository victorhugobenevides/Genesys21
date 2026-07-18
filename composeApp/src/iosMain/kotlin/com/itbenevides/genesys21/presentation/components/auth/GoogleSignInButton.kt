package com.itbenevides.genesys21.presentation.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.mmk.kmpauth.google.GoogleButtonUiContainer

@Composable
actual fun GoogleSignInButton(
    modifier: Modifier,
    onTokenReceived: (idToken: String, accessToken: String?) -> Unit,
    onError: (String) -> Unit
) {
    GoogleButtonUiContainer(
        onGoogleSignInResult = { googleUser ->
            val idToken = googleUser?.idToken
            val accessToken = googleUser?.accessToken
            if (idToken != null) {
                onTokenReceived(idToken, accessToken)
            } else {
                onError("Login cancelado ou falhou")
            }
        }
    ) {
        GenesysLoadingButton(
            text = "Entrar com Google",
            onClick = { this.onClick() },
            modifier = modifier
        )
    }
}
