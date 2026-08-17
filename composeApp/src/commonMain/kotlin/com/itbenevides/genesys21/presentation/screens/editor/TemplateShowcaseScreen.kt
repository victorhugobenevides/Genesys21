package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageTemplateRegistry
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerContent
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreenState
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateShowcaseScreen(
    onBack: () -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    val templates =
        remember {
            listOf(
                "Vendas Premium" to Page.createFromTemplate("premium_store", "tpl_1", "default", "Modern Luxury"),
                "Personal Hub" to Page.createFromTemplate("personal_hub", "tpl_2", "default", "Victor Benevides"),
                "Agendamento Profissional" to Page.createFromTemplate("service_booking", "tpl_3", "default", "Serviços"),
                "Página em Branco" to Page.createFromTemplate("empty", "tpl_4", "default", "Nova Página"),
            )
        }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = "Template Catalog",
                onBack = onBack,
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            templates.forEach { (name, page) ->
                TemplatePreviewSection(name, page, isCompact)
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun TemplatePreviewSection(
    name: String,
    page: Page,
    isCompact: Boolean,
) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // We wrap each template in its OWN AppTheme to respect its custom configuration
        AppTheme(themeConfig = page.theme, customTheme = page.customTheme) {
            GenesysCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(600.dp),
                // Fixed height for catalog consistency
                elevation = 4.dp,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    PageViewerContent(
                        state = PageViewerScreenState(page = page),
                        currentFilterQuery = "",
                        isCompact = isCompact,
                        onEvent = {},
                    )
                }
            }
        }
    }
}
