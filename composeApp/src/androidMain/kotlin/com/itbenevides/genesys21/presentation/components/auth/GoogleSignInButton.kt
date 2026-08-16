package com.itbenevides.genesys21.presentation.components.auth

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.util.LocalTestMode
import com.mmk.kmpauth.google.GoogleButtonUiContainer

@Composable
actual fun GoogleSignInButton(
    modifier: Modifier,
    onTokenReceived: (idToken: String, accessToken: String?) -> Unit,
    onError: (String) -> Unit
) {
    if (LocalTestMode.current) {
        GenesysLoadingButton(
            text = "Google Sign In (Mock)",
            icon = Icons.Default.Language,
            onClick = { },
            modifier = modifier.heightIn(min = 48.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    } else {
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
                icon = Icons.Default.Language,
                onClick = { this.onClick() },
                modifier = modifier.heightIn(min = 48.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}
