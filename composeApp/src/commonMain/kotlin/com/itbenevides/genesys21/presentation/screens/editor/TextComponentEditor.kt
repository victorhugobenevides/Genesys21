package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysSlider
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun TextComponentEditor(
    component: PageComponent.Text,
    onSave: (PageComponent.Text) -> Unit,
) {
    var content by remember { mutableStateOf(component.content) }
    var alignment by remember { mutableStateOf(component.textAlign) }
    var fontSize by remember { mutableStateOf(component.fontSize.toFloat()) }
    var weight by remember { mutableStateOf(component.fontWeight) }

    val primaryColor = MaterialTheme.colorScheme.primary

    // Cria uma versão temporária do componente para renderizar na pre-visualização real
    val previewComponent =
        remember(content, alignment, fontSize, weight) {
            component.copy(
                content = content,
                textAlign = alignment,
                fontSize = fontSize.toInt(),
                fontWeight = weight,
            )
        }

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)

        // CORREÇÃO: Usando o renderizador real para que a pre-visualização seja IDÊNTICA ao resultado final
        PageComponentRenderer(
            component = previewComponent,
            isEditMode = false,
        )

        GenesysSpacer(GenesysSpacing.Large)
        GenesysTextField(
            value = content,
            onValueChange = { content = it },
            label = GenesysStrings.ContentTextLabel,
            singleLine = false,
            minLines = 3,
            icon = GenesysIcons.Description,
        )

        GenesysSpacer(GenesysSpacing.Medium)

        GenesysRow(modifier = Modifier.fillMaxWidth()) {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.Alignment, style = GenesysTextStyle.Label)
                    Row {
                        listOf("LEFT", "CENTER", "RIGHT").forEach { align ->
                            val isSelected = alignment == align
                            GenesysIconButton(
                                icon =
                                    when (align) {
                                        "LEFT" -> GenesysIcons.AlignLeft
                                        "RIGHT" -> GenesysIcons.AlignRight
                                        else -> GenesysIcons.AlignCenter
                                    },
                                onClick = { alignment = align },
                                tint = if (isSelected) primaryColor else Color.Unspecified,
                            )
                        }
                    }
                }
            }

            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.FontStyle, style = GenesysTextStyle.Label)
                    Row {
                        GenesysIconButton(
                            icon = GenesysIcons.Bold,
                            onClick = { weight = if (weight == "BOLD") "NORMAL" else "BOLD" },
                            tint = if (weight == "BOLD") primaryColor else Color.Unspecified,
                        )
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysSlider(
            value = fontSize,
            onValueChange = { fontSize = it },
            label = GenesysStrings.FontSize,
            valueRange = 12f..24f,
        )

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = GenesysStrings.SaveText,
            fillWidth = true,
            onClick = {
                onSave(previewComponent)
            },
        )
    }
}
