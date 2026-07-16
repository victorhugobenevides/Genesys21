package com.itbenevides.genesys21.presentation.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.mmk.kmpauth.google.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
actual fun GoogleSignInButton(
    modifier: Modifier,
    onTokenReceived: (idToken: String, accessToken: String?) -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val uiProvider = GoogleAuthProvider.get().getUiProvider()

    GenesysLoadingButton(
        text = "Entrar com Google",
        onClick = {
            scope.launch {
                try {
                    val googleUser = uiProvider.signIn()
                    val idToken = googleUser?.idToken
                    val accessToken = googleUser?.accessToken
                    if (idToken != null) {
                        onTokenReceived(idToken, accessToken)
                    } else {
                        onError("Login cancelado ou falhou")
                    }
                } catch (e: Exception) {
                    onError(e.message ?: "Erro ao entrar com Google")
                }
            }
        },
        modifier = modifier
    )
}
