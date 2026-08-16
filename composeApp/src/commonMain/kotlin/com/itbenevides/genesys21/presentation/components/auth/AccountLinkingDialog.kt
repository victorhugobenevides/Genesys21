package com.itbenevides.genesys21.presentation.components.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.theme.*

@Composable
fun AccountLinkingDialog(
    email: String,
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit,
) {
    GenesysDialog(
        onDismissRequest = onDismiss,
        title = "Vincular Contas",
        confirmButton = {
            Button(onClick = onLoginClick) {
                Text("Ir para Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(GenesysStrings.Cancel)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = GenesysIcons.Person,
                contentDescription = null,
                tint = GenesysTheme.colors.brand,
                modifier = Modifier.size(48.dp)
            )

            GenesysSpacer(GenesysTheme.spacing.m)

            GenesysText(
                text = "Já existe uma conta com o e-mail $email.",
                style = GenesysTextStyle.Body
            )

            GenesysSpacer(GenesysTheme.spacing.s)

            GenesysText(
                text = "Para vincular seu login do Google, entre primeiro usando sua senha tradicional.",
                style = GenesysTextStyle.Body
            )
        }
    }
}
