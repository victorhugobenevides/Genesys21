package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysSlider
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun TestimonialComponentEditor(
    component: PageComponent.Testimonial,
    onSave: (PageComponent.Testimonial) -> Unit
) {
    var quote by remember(component) { mutableStateOf(component.quote) }
    var author by remember(component) { mutableStateOf(component.author) }
    var authorTitle by remember(component) { mutableStateOf(component.authorTitle ?: "") }
    var rating by remember(component) { mutableStateOf(component.rating.toFloat()) }

    LaunchedEffect(quote, author, authorTitle, rating) {
        onSave(component.copy(
            quote = quote,
            author = author,
            authorTitle = authorTitle.ifBlank { null },
            rating = rating.toInt()
        ))
    }

    GenesysColumn(usePadding = false) {
        GenesysTextField(
            value = quote,
            onValueChange = { quote = it },
            label = "Depoimento",
            icon = GenesysIcons.Feedback,
            singleLine = false,
            minLines = 3
        )

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysTextField(value = author, onValueChange = { author = it }, label = "Autor", icon = GenesysIcons.Person)
        GenesysSpacer(GenesysTheme.spacing.s)
        GenesysTextField(value = authorTitle, onValueChange = { authorTitle = it }, label = "Cargo/Título (Opcional)", icon = GenesysIcons.Description)

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysSlider(
            value = rating,
            onValueChange = { rating = it },
            label = "Avaliação (Estrelas)",
            valueRange = 1f..5f,
            steps = 4
        )
    }
}
