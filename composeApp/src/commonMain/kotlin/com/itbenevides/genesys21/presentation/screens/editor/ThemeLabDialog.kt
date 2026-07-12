package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.CustomThemeConfig
import com.itbenevides.genesys21.domain.model.TypographySet
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysSlider
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysColorField
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysDropdownField
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysBottomSheet

@Composable
fun ThemeLabDialog(
    initialConfig: CustomThemeConfig?,
    onSave: (CustomThemeConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    var primary by remember { mutableStateOf(initialConfig?.primaryColor ?: "") }
    var onPrimary by remember { mutableStateOf(initialConfig?.onPrimaryColor ?: "") }
    var secondary by remember { mutableStateOf(initialConfig?.secondaryColor ?: "") }
    var background by remember { mutableStateOf(initialConfig?.backgroundColor ?: "") }
    var surface by remember { mutableStateOf(initialConfig?.surfaceColor ?: "") }
    var onSurface by remember { mutableStateOf(initialConfig?.onSurfaceColor ?: "") }
    var cornerRadius by remember { mutableStateOf(initialConfig?.cornerRadius?.toFloat() ?: 16f) }
    var glassIntensity by remember { mutableStateOf(initialConfig?.glassIntensity ?: 0.1f) }
    var typography by remember { mutableStateOf(initialConfig?.typographySet ?: TypographySet.DEFAULT) }

    GenesysBottomSheet(
        onDismiss = onDismiss,
        title = "Theme Lab 🧪",
    ) {
        GenesysColumn(usePadding = true, useScroll = true) {
            GenesysText(text = "Cores da Marca", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysSpacing.Small)

            GenesysColorField(
                value = primary,
                onValueChange = { primary = it },
                label = "Primária",
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysColorField(
                value = onPrimary,
                onValueChange = { onPrimary = it },
                label = "Texto sobre Primária",
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysColorField(
                value = secondary,
                onValueChange = { secondary = it },
                label = "Secundária",
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysColorField(
                value = background,
                onValueChange = { background = it },
                label = "Fundo",
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysColorField(
                value = surface,
                onValueChange = { surface = it },
                label = "Superfície/Cards",
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysColorField(
                value = onSurface,
                onValueChange = { onSurface = it },
                label = "Texto sobre Fundo",
            )

            GenesysSpacer(GenesysSpacing.Large)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Large)

            GenesysText(text = "Tipografia", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysSpacing.Small)

            GenesysDropdownField(
                value = typography.name,
                onValueChange = { name ->
                    typography = TypographySet.valueOf(name)
                },
                label = "Conjunto de Fontes",
                options = TypographySet.entries.map { it.name },
                icon = GenesysIcons.Edit,
            )

            GenesysSpacer(GenesysSpacing.Large)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Large)

            GenesysText(text = "Estilo Pro (Breve)", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)

            GenesysSlider(
                value = cornerRadius,
                onValueChange = { cornerRadius = it },
                label = "Arredondamento: ${cornerRadius.toInt()}dp",
                valueRange = 0f..40f,
            )

            GenesysSlider(
                value = glassIntensity,
                onValueChange = { glassIntensity = it },
                label = "Intensidade Glass: ${(glassIntensity * 100).toInt()}%",
                valueRange = 0.05f..0.5f,
            )

            GenesysSpacer(GenesysSpacing.Huge)

            GenesysLoadingButton(
                text = "Aplicar Estilo Pro",
                onClick = {
                    onSave(
                        CustomThemeConfig(
                            primaryColor = primary.ifBlank { null },
                            onPrimaryColor = onPrimary.ifBlank { null },
                            secondaryColor = secondary.ifBlank { null },
                            backgroundColor = background.ifBlank { null },
                            surfaceColor = surface.ifBlank { null },
                            onSurfaceColor = onSurface.ifBlank { null },
                            cornerRadius = cornerRadius.toInt(),
                            glassIntensity = glassIntensity,
                            typographySet = typography,
                        ),
                    )
                },
                fillWidth = true,
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysTextButton(
                text = "Resetar para Padrão",
                onClick = {
                    primary = ""
                    onPrimary = ""
                    secondary = ""
                    background = ""
                    surface = ""
                    onSurface = ""
                    cornerRadius = 16f
                    glassIntensity = 0.1f
                    typography = TypographySet.DEFAULT
                },
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray,
            )
        }
    }
}
