package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysSlider
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun HeaderComponentEditor(
    component: PageComponent.Header,
    onSave: (PageComponent.Header) -> Unit,
) {
    var title by remember { mutableStateOf(component.title) }
    var alignment by remember { mutableStateOf(component.textAlign) }
    var fontSize by remember { mutableStateOf(component.fontSize.toFloat()) }
    var isUppercase by remember { mutableStateOf(component.isUppercase) }
    var usePrimaryColor by remember { mutableStateOf(component.usePrimaryColor) }

    val primaryColor = MaterialTheme.colorScheme.primary

    // Cria uma versão temporária do componente para renderizar na pre-visualização real
    val previewComponent =
        remember(title, alignment, fontSize, isUppercase, usePrimaryColor) {
            component.copy(
                title = title,
                textAlign = alignment,
                fontSize = fontSize.toInt(),
                isUppercase = isUppercase,
                usePrimaryColor = usePrimaryColor,
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
            value = title,
            onValueChange = { title = it },
            label = GenesysStrings.TitleTextLabel,
            icon = GenesysIcons.Description,
        )

        GenesysSpacer(GenesysSpacing.Medium)

        GenesysText(text = GenesysStrings.Alignment, style = GenesysTextStyle.Label)
        GenesysRow(modifier = Modifier.fillMaxWidth()) {
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

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysSlider(
            value = fontSize,
            onValueChange = { fontSize = it },
            label = GenesysStrings.FontSize,
            valueRange = 18f..48f,
        )

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysRow(modifier = Modifier.fillMaxWidth()) {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.UppercaseOption, style = GenesysTextStyle.Body)
                    Switch(checked = isUppercase, onCheckedChange = { isUppercase = it })
                }
            }
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.BrandColorOption, style = GenesysTextStyle.Body)
                    Switch(checked = usePrimaryColor, onCheckedChange = { usePrimaryColor = it })
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = GenesysStrings.UpdateTitle,
            fillWidth = true,
            onClick = {
                onSave(previewComponent)
            },
        )
    }
}
