package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun TestimonialComponentEditor(
    component: PageComponent.Testimonial,
    onSave: (PageComponent.Testimonial) -> Unit
) {
    var quote by remember { mutableStateOf(component.quote) }
    var author by remember { mutableStateOf(component.author) }

    val previewComponent = remember(quote, author) {
        component.copy(quote = quote, author = author)
    }

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        
        PageComponentRenderer(component = previewComponent, isEditMode = false)

        GenesysSpacer(GenesysSpacing.Large)
        GenesysTextField(
            value = quote, 
            onValueChange = { quote = it }, 
            label = "Depoimento", 
            singleLine = false, 
            minLines = 3,
            icon = GenesysIcons.FormatQuote
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysTextField(
            value = author, 
            onValueChange = { author = it }, 
            label = "Autor/Cargo", 
            icon = GenesysIcons.Person
        )

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = "Salvar Depoimento", 
            fillWidth = true,
            onClick = { onSave(previewComponent) }
        )
    }
}
