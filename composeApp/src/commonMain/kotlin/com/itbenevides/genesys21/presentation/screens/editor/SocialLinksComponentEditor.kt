package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons

@Composable
fun SocialLinksComponentEditor(
    component: PageComponent.SocialLinks,
    onSave: (PageComponent.SocialLinks) -> Unit
) {
    var instagram by remember { mutableStateOf(component.instagram ?: "") }
    var whatsapp by remember { mutableStateOf(component.whatsapp ?: "") }
    var email by remember { mutableStateOf(component.email ?: "") }

    GenesysColumn(usePadding = false) {
        GenesysText(text = "Configurar Redes Sociais", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysSpacer(GenesysSpacing.Medium)

        GenesysTextField(value = instagram, onValueChange = { instagram = it }, label = "Instagram (URL)", icon = GenesysIcons.Instagram)
        GenesysSpacer(GenesysSpacing.Small)
        GenesysTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = "WhatsApp (URL ou Número)", icon = GenesysIcons.WhatsApp)
        GenesysSpacer(GenesysSpacing.Small)
        GenesysTextField(value = email, onValueChange = { email = it }, label = "E-mail", icon = GenesysIcons.Email)

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = "Salvar Links",
            fillWidth = true,
            onClick = {
                onSave(component.copy(
                    instagram = instagram.ifBlank { null },
                    whatsapp = whatsapp.ifBlank { null },
                    email = email.ifBlank { null }
                ))
            }
        )
    }
}
