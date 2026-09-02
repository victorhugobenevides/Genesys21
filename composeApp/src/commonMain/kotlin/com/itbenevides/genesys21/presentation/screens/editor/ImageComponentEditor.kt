package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysSlider
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysDropdownField
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun ImageComponentEditor(
    component: PageComponent.Image,
    userPages: List<Page>,
    isUploading: Boolean,
    onPickImage: () -> Unit,
    onSave: (PageComponent.Image) -> Unit,
) {
    var sizeValue by remember { mutableStateOf(component.size.toFloat()) }
    var isCircular by remember { mutableStateOf(component.isCircular) }
    var isFullWidth by remember { mutableStateOf(component.isFullWidth) }

    val pageOptions = remember(userPages) { userPages.map { it.title } }
    var currentLinkValue by remember(component.destinationUrl, component.destinationPageId, userPages) {
        val internalTitle = userPages.find { it.id == component.destinationPageId }?.title
        mutableStateOf(internalTitle ?: component.destinationUrl ?: "")
    }

    // Cria uma versão temporária do componente para renderizar na pre-visualização real
    val previewComponent =
        remember(component.url, sizeValue, isCircular, isFullWidth) {
            component.copy(
                size = sizeValue.toInt(),
                isCircular = isCircular,
                isFullWidth = isFullWidth,
            )
        }

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysTheme.spacing.s)

        // CORREÇÃO: Usando o renderizador real para que a pre-visualização seja IDÊNTICA ao resultado final
        PageComponentRenderer(
            component = previewComponent,
            storeId = "admin",
            isEditMode = false,
        )

        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysLoadingButton(
            text = if (isUploading) "Enviando..." else "Trocar Imagem",
            icon = GenesysIcons.CloudUpload,
            onClick = onPickImage,
            isLoading = isUploading,
            fillWidth = true,
        )

        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysSlider(value = sizeValue, onValueChange = { sizeValue = it }, label = "Tamanho da Imagem", valueRange = 50f..500f)

        GenesysSpacer(GenesysTheme.spacing.m)
        GenesysRow(modifier = Modifier.fillMaxWidth()) {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText("Imagem Redonda?", style = GenesysTextStyle.Body)
                    Switch(checked = isCircular, onCheckedChange = {
                        isCircular = it
                        if (it) isFullWidth = false
                    })
                }
            }
            if (!isCircular) {
                GenesysWeightBox(1f) {
                    GenesysColumn(usePadding = false) {
                        GenesysText("Largura Total?", style = GenesysTextStyle.Body)
                        Switch(checked = isFullWidth, onCheckedChange = { isFullWidth = it })
                    }
                }
            }
        }

        GenesysSpacer(GenesysTheme.spacing.m)
        GenesysDropdownField(
            value = currentLinkValue,
            onValueChange = { currentLinkValue = it },
            label = "Destino do Clique",
            placeholder = "Link ou Página",
            options = pageOptions,
            icon = GenesysIcons.Language,
        )

        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysLoadingButton(text = "Confirmar Alterações", fillWidth = true, onClick = {
            val matchingPage = userPages.find { it.title == currentLinkValue }
            onSave(
                component.copy(
                    size = sizeValue.toInt(),
                    isCircular = isCircular,
                    isFullWidth = isFullWidth,
                    destinationUrl = if (matchingPage == null) currentLinkValue else "",
                    destinationPageId = matchingPage?.id,
                ),
            )
        })
    }
}
