package com.itbenevides.genesys21.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageTemplateRegistry
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerContent
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreenState
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshotWithPrefix
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import org.junit.Rule
import org.junit.Test
import org.koin.compose.koinInject

class TemplatesSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testAllTemplatesResponsive() {
        val templates = PageTemplateRegistry.templates

        templates.forEach { template ->
            val page = PageTemplateRegistry.createPageFromTemplate(template.id, "test-id", "test-store")

            genesysResponsiveSnapshotWithPrefix(paparazzi, template.id) {
                val windowSizeClass = LocalWindowSizeClass.current
                val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

                com.itbenevides.genesys21.ui.theme.AppTheme(themeConfig = page.theme, customTheme = page.customTheme) {
                    PageViewerContent(
                        state = PageViewerScreenState(page = page),
                        currentFilterQuery = "",
                        isCompact = isCompact,
                        onEvent = {}
                    )
                }
            }
        }
    }

    @Test
    fun testTemplateCatalogPreviewDialogResponsive() {
        val templates = PageTemplateRegistry.templates
        val firstTemplate = templates.first()

        genesysResponsiveSnapshotWithPrefix(paparazzi, "CatalogPreview") {
            val previewPage = PageTemplateRegistry.createPageFromTemplate(firstTemplate.id, "preview", "preview")

            com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog(
                onDismissRequest = {},
                title = "Preview: ${firstTemplate.title}",
                confirmButton = {
                    com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton(
                        text = "Usar este Template",
                        onClick = {},
                        fillWidth = true
                    )
                },
                dismissButton = {
                    com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton(
                        text = "Fechar",
                        onClick = {}
                    )
                }
            ) {
                com.itbenevides.genesys21.ui.theme.AppTheme(themeConfig = previewPage.theme, customTheme = previewPage.customTheme) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(650.dp)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), androidx.compose.foundation.shape.RoundedCornerShape(32.dp))
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                                .border(4.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                        ) {
                            PageViewerContent(
                                state = PageViewerScreenState(page = previewPage),
                                currentFilterQuery = "",
                                isCompact = true,
                                onEvent = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
