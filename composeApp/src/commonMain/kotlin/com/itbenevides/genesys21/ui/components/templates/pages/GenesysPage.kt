package com.itbenevides.genesys21.ui.components.templates.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.ui.util.isTestMode

/**
 * GenesysPage: O container mestre do Design System.
 * Aplica automaticamente os tokens de background e espaçamento global.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
fun GenesysPage(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    navigationSuiteItems: (NavigationSuiteScope.() -> Unit)? = null,
    usePadding: Boolean = false,
    content: @Composable () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == GenesysWindowSizeClass.EXPANDED

    val scaffoldContent: @Composable () -> Unit = {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            containerColor = GenesysTheme.colors.background,
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .then(if (usePadding) Modifier.padding(GenesysTheme.spacing.m) else Modifier)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        content()
                    }

                    // Assinatura do Rodapé padronizada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = GenesysTheme.spacing.l)
                            .clickable { uriHandler.openUri("https://victorbenevides.dev") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "desenvolvido por victorbenevides.dev",
                            style = GenesysTheme.typography.label,
                            color = GenesysTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            },
        )
    }

    if (drawerContent != null && isExpanded) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(
                    drawerContainerColor = GenesysTheme.colors.surface,
                    drawerContentColor = GenesysTheme.colors.onSurface,
                    modifier = Modifier.width(280.dp)
                ) {
                    drawerContent()
                }
            },
            content = {
                NavigationWrapper(navigationSuiteItems, isExpanded, true, scaffoldContent)
            }
        )
    } else if (drawerContent != null) {
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = GenesysTheme.colors.surface,
                    drawerContentColor = GenesysTheme.colors.onSurface
                ) {
                    drawerContent()
                }
            },
            content = {
                NavigationWrapper(navigationSuiteItems, isExpanded, true, scaffoldContent)
            }
        )
    } else {
        NavigationWrapper(navigationSuiteItems, isExpanded, false, scaffoldContent)
    }
}

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
private fun NavigationWrapper(
    navigationSuiteItems: (NavigationSuiteScope.() -> Unit)?,
    isExpanded: Boolean,
    hasSidebar: Boolean,
    content: @Composable () -> Unit
) {
    if (navigationSuiteItems != null) {
        val windowSizeClass = LocalWindowSizeClass.current
        val testMode = isTestMode()

        if (testMode) {
            // ... (keep test mode logic)
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (windowSizeClass != GenesysWindowSizeClass.COMPACT) {
                        NavigationRail(containerColor = GenesysTheme.colors.background) {}
                    }
                    Box(modifier = Modifier.weight(1f)) { content() }
                }
            }
        } else {
            // SE tivermos Sidebar e estivermos no Expanded, omitimos a NavigationRail para evitar duplicidade
            if (isExpanded && hasSidebar) {
                content()
            } else {
                val layoutType = when (windowSizeClass) {
                    GenesysWindowSizeClass.COMPACT -> NavigationSuiteType.NavigationBar
                    GenesysWindowSizeClass.MEDIUM -> NavigationSuiteType.NavigationRail
                    GenesysWindowSizeClass.EXPANDED -> NavigationSuiteType.NavigationRail
                }

                NavigationSuiteScaffold(
                    navigationSuiteItems = navigationSuiteItems,
                    layoutType = layoutType,
                    containerColor = GenesysTheme.colors.background,
                    content = content
                )
            }
        }
    } else {
        content()
    }
}
