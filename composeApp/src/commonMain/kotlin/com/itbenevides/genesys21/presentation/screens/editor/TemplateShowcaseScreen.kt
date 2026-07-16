package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerContent
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreenState
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme

@Composable
fun TemplateShowcaseScreen(
    onBack: () -> Unit,
) {
    val templates =
        remember {
            listOf(
                "Pro Design" to Page.proDesignTemplate("tpl_1", "Modern Luxury"),
                "Social Bio" to Page.profileTemplate("tpl_2", "Victor Benevides"),
                "Blog Article" to Page.blogPostTemplate("tpl_3", "Building with KMP"),
                "Default Store" to Page.defaultTemplate("tpl_4", "My Vintage Store"),
                "Barber Shop" to Page.barberShopTemplate("tpl_5", "Classic Barbershop"),
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
                TemplatePreviewSection(name, page)
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun TemplatePreviewSection(
    name: String,
    page: Page,
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
                        onEvent = {},
                    )
                }
            }
        }
    }
}
