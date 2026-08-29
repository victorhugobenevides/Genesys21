package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.presentation.screens.list.*
import com.itbenevides.genesys21.presentation.screens.list.components.AdminTabHeader
import com.itbenevides.genesys21.presentation.screens.list.components.PageItemRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.theme.*

@Composable
fun PagesTab(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
    onViewPage: (Page) -> Unit,
    onEditPage: (Page) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        AdminTabHeader(
            title = GenesysStrings.ManageVitrines,
            subtitle = GenesysStrings.ManageVitrinesSubtitle,
            action = {
                GenesysLoadingButton(
                    text = "Nova Vitrine",
                    icon = GenesysIcons.Add,
                    onClick = { onEvent(PageListEvent.OnCreatePageClicked) },
                    fillWidth = false
                )
            }
        )

        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            if (state.pages.isEmpty() && !state.isLoading) {
                GenesysEmptyState(
                    icon = GenesysIcons.WebAssetOff,
                    title = GenesysStrings.NoPagesFound,
                    description = GenesysStrings.NoPagesDescription,
                    action = {
                        GenesysLoadingButton(
                            text = "Criar Minha Primeira Página",
                            icon = GenesysIcons.Add,
                            onClick = { onEvent(PageListEvent.OnCreatePageClicked) }
                        )
                    }
                )
            } else {
                state.pages.forEach { page ->
                    PageItemRow(
                        page = page,
                        onView = { onViewPage(page) },
                        onEdit = { onEditPage(page) },
                        onRename = { onEvent(PageListEvent.OnRenamePageClicked(page)) },
                        onCopyUrl = {
                            val baseUrl = getWebBaseUrl()
                            val url = "$baseUrl/p/${page.id}"
                            clipboardManager.setText(AnnotatedString(url))
                        },
                        onExport = { onEvent(PageListEvent.OnExportPageClicked(page)) },
                        onDelete = { onEvent(PageListEvent.OnDeletePageClicked(page.id)) },
                    )
                    GenesysSpacer(GenesysTheme.spacing.m)
                }
            }
        }
    }
}
