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
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalTestMode
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

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
                NavigationWrapper(navigationSuiteItems, scaffoldContent)
            }
        )
    } else {
        NavigationWrapper(navigationSuiteItems, scaffoldContent)
    }
}

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
private fun NavigationWrapper(
    navigationSuiteItems: (NavigationSuiteScope.() -> Unit)?,
    content: @Composable () -> Unit
) {
    if (navigationSuiteItems != null) {
        // Deixamos o layoutType como o padrão (AUTO) para que ele mude sozinho entre Barra e Rail
        NavigationSuiteScaffold(
            navigationSuiteItems = navigationSuiteItems,
            containerColor = GenesysTheme.colors.background,
            content = content
        )
    } else {
        content()
    }
}
