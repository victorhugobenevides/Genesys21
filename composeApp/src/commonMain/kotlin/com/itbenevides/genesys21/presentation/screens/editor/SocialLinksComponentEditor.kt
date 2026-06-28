package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton

@Composable
fun SocialLinksComponentEditor(
    component: PageComponent.SocialLinks,
    onSave: (PageComponent.SocialLinks) -> Unit,
) {
    var instagram by remember(component) { mutableStateOf(component.instagram ?: "") }
    var whatsapp by remember(component) { mutableStateOf(component.whatsapp ?: "") }
    var youtube by remember(component) { mutableStateOf(component.youtube ?: "") }
    var email by remember(component) { mutableStateOf(component.email ?: "") }

    GenesysColumn(usePadding = false) {
        GenesysTextField(value = instagram, onValueChange = { instagram = it }, label = "Instagram (URL)", icon = GenesysIcons.Person)
        GenesysSpacer(GenesysSpacing.Small)
        GenesysTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = "WhatsApp (URL ou Link)", icon = GenesysIcons.Chat)
        GenesysSpacer(GenesysSpacing.Small)
        GenesysTextField(value = youtube, onValueChange = { youtube = it }, label = "YouTube (URL)", icon = GenesysIcons.Web)
        GenesysSpacer(GenesysSpacing.Small)
        GenesysTextField(value = email, onValueChange = { email = it }, label = "E-mail", icon = GenesysIcons.Email)

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = "Salvar Redes Sociais",
            onClick = {
                onSave(
                    component.copy(
                        instagram = instagram.ifBlank { null },
                        whatsapp = whatsapp.ifBlank { null },
                        youtube = youtube.ifBlank { null },
                        email = email.ifBlank { null },
                    ),
                )
            },
            fillWidth = true,
        )
    }
}
