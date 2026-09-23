package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import kotlinx.coroutines.launch

@Composable
fun AiRefinementDialog(
    component: PageComponent,
    onRefined: (PageComponent) -> Unit,
    onDismiss: () -> Unit,
    refineAction: suspend (PageComponent, String) -> PageComponent,
) {
    var instruction by remember { mutableStateOf("") }
    var isRefining by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    GenesysBottomSheet(
        onDismiss = onDismiss,
        title = "Refinar com IA 🪄",
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Descreva como você quer melhorar este componente. A IA irá reescrever os textos para você.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GenesysSpacer(GenesysTheme.spacing.m)

            GenesysTextField(
                value = instruction,
                onValueChange = { instruction = it },
                label = "Instrução (ex: Faça parecer mais luxuoso)",
                placeholder = "O que a IA deve fazer?",
                icon = GenesysIcons.Magic,
                singleLine = false,
                minLines = 2,
            )

            if (error != null) {
                GenesysSpacer(GenesysTheme.spacing.s)
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            GenesysSpacer(GenesysTheme.spacing.l)

            GenesysLoadingButton(
                text = "Gerar Sugestão",
                onClick = {
                    scope.launch {
                        isRefining = true
                        error = null
                        try {
                            val refined = refineAction(component, instruction)
                            onRefined(refined)
                            onDismiss()
                        } catch (e: Exception) {
                            error = "Falha ao gerar: ${e.message}"
                        } finally {
                            isRefining = false
                        }
                    }
                },
                isLoading = isRefining,
                enabled = instruction.isNotBlank(),
                fillWidth = true,
                icon = GenesysIcons.Magic,
            )
        }
    }
}
