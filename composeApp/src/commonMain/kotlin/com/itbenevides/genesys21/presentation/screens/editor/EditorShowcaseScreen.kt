package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.viewer.ComponentEditorUI
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelState
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import org.koin.compose.koinInject

@Composable
fun EditorShowcaseScreen(
    onBack: () -> Unit,
    viewModel: PageViewModel = koinInject(),
) {
    val samplePage = remember { Page.createFromTemplate("professional_vitrine", "showcase", "default", "Editor Test") }

    val components =
        listOf(
            PageComponent.Header(title = "Editable Header"),
            PageComponent.Text(content = "This is a sample text that can be edited in the showcase."),
            PageComponent.Image(url = "https://picsum.photos/800/400"),
            PageComponent.Button(text = "Action Button", url = "#"),
        )

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = "Component Editor Showcase",
                onBack = onBack,
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
        ) {
            components.forEach { component ->
                ComponentShowcaseItem(component, samplePage, viewModel)
                Spacer(Modifier.height(48.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun ComponentShowcaseItem(
    component: PageComponent,
    page: Page,
    viewModel: PageViewModel,
) {
    Column {
        Text(
            text = component::class.simpleName ?: "Component",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Left: Editor
            Column(Modifier.weight(1f)) {
                Text("EDITOR UI", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(Modifier.height(8.dp))

                val mockState =
                    WhiteLabelState(
                        page = page.copy(components = listOf(component)),
                        editingComponentIndex = 0,
                    )

                Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                    ComponentEditorUI(
                        state = mockState,
                        viewModel = viewModel,
                        index = 0,
                        onEvent = {},
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
