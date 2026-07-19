package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageTemplate
import com.itbenevides.genesys21.domain.model.PageTemplateRegistry
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerContent
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreenState
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.images.GenesysImage
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

@Composable
fun TemplateCatalogScreen(
    viewModel: PageViewModel,
    onBack: () -> Unit,
    onTemplateSelected: (PageTemplate) -> Unit
) {
    val templates by viewModel.templates.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT
    var previewTemplate by remember { mutableStateOf<PageTemplate?>(null) }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = "Catálogo de Templates",
                onBack = onBack
            )
        }
    ) {
        GenesysColumn(usePadding = true, modifier = Modifier.fillMaxSize()) {
            GenesysText(
                text = "Escolha um ponto de partida",
                style = GenesysTextStyle.Headline,
                fontWeight = GenesysFontWeight.ExtraBold
            )
            GenesysText(
                text = "Selecione um layout pronto para começar a vender em minutos.",
                style = GenesysTextStyle.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GenesysSpacer(GenesysSpacing.Large)

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = if (isCompact) 160.dp else 280.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onSelect = { onTemplateSelected(template) },
                        onPreview = { previewTemplate = template }
                    )
                }
            }
        }
    }

    if (previewTemplate != null) {
        val template = previewTemplate!!
        val previewPage = remember(template) {
            PageTemplateRegistry.createPageFromTemplate(template.id, "preview", "preview")
        }

        GenesysDialog(
            onDismissRequest = { previewTemplate = null },
            title = "Preview: ${template.title}",
            confirmButton = {
                GenesysLoadingButton(
                    text = "Usar este Template",
                    onClick = {
                        onTemplateSelected(template)
                        previewTemplate = null
                    },
                    fillWidth = true
                )
            },
            dismissButton = {
                GenesysTextButton(
                    text = "Fechar",
                    onClick = { previewTemplate = null }
                )
            }
        ) {
            AppTheme(themeConfig = previewPage.theme, customTheme = previewPage.customTheme) {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    PageViewerContent(
                        state = PageViewerScreenState(page = previewPage),
                        currentFilterQuery = "",
                        isCompact = isCompact,
                        onEvent = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: PageTemplate,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    GenesysCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPreview
    ) {
        val thumb = template.thumbnailUrl
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(if (template.id == "empty") 120.dp else 180.dp)) {
                if (thumb != null) {
                    GenesysImage(
                        url = thumb,
                        contentDescription = template.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = GenesysIcons.WebAssetOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd)
                ) {
                    Text(
                        text = template.category.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                GenesysText(
                    text = template.title,
                    style = GenesysTextStyle.Title,
                    fontWeight = GenesysFontWeight.Bold
                )
                GenesysText(
                    text = template.description,
                    style = GenesysTextStyle.Label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                GenesysSpacer(GenesysSpacing.Medium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPreview,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Preview")
                    }
                    GenesysLoadingButton(
                        text = "Criar",
                        onClick = onSelect,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
