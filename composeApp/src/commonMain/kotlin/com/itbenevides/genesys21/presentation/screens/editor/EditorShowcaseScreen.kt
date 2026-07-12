package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelState
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun EditorShowcaseScreen(
    onBack: () -> Unit,
) {
    // We use koinInject directly as usual.
    val viewModel: PageViewModel = koinInject()
    val samplePage = remember { Page.defaultTemplate("showcase", "Editor Showcase") }

    val componentsToTest =
        remember {
            listOf(
                PageComponent.Header(title = "Example Header"),
                PageComponent.Text(content = "Sample text content for testing layouts."),
                PageComponent.Image(url = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=400"),
                PageComponent.Button(text = "Action Button", url = "#"),
            )
        }

    AppTheme {
        GenesysPage(
            topBar = {
                GenesysTopAppBar(
                    title = "Editor Components",
                    onBack = onBack,
                )
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                componentsToTest.forEach { component ->
                    ComparisonCard(component, viewModel, samplePage)
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ComparisonCard(
    component: PageComponent,
    viewModel: PageViewModel,
    page: Page,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = component::class.simpleName ?: "Component",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Left: Editor
                Column(Modifier.weight(1f)) {
                    Text("EDITOR UI", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))

                    // We must ensure the component list is NOT empty so index 0 works
                    val mockState =
                        WhiteLabelState(
                            page = page.copy(components = listOf(component)),
                            editingComponentIndex = 0,
                        )

                    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        com.itbenevides.genesys21.presentation.screens.viewer.ComponentEditorUI(
                            state = mockState,
                            viewModel = viewModel,
                            index = 0,
                            onEvent = {},
                            isEmbedded = true,
                            originalPage = page,
                            onManageCategories = {},
                            onPickImage = {},
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Right: Result
                Column(Modifier.weight(1f)) {
                    Text("LIVE RESULT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    PageComponentRenderer(
                        component = component,
                        isEditMode = false,
                    )
                }
            }
        }
    }
}
