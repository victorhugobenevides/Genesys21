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

@Composable
fun TemplateShowcaseScreen(
    onBack: () -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    val templates =
        remember {
            listOf(
                "Pro Design" to Page.createFromTemplate("pro_design", "tpl_1", "default", "Modern Luxury"),
                "Social Bio" to Page.createFromTemplate("bio_profile", "tpl_2", "default", "Victor Benevides"),
                "Blog Article" to Page.createFromTemplate("blog_post", "tpl_3", "default", "Building with KMP"),
                "Default Store" to Page.createFromTemplate("professional_vitrine", "tpl_4", "default", "My Vintage Store"),
                "Barber Shop" to Page.createFromTemplate("barber_shop", "tpl_5", "default", "Classic Barbershop"),
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
