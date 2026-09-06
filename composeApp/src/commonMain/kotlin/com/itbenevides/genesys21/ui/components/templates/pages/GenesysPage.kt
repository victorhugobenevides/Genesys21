package com.itbenevides.genesys21.ui.components.templates.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val pageContent: @Composable (PaddingValues) -> Unit = { scaffoldPadding ->
        // No portal ADM, queremos que o conteúdo ocupe a tela toda e o scroll esteja no componente interno (tab)
        // O Rodapé agora deve ser parte do conteúdo para não roubar espaço fixo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .then(if (usePadding) Modifier.padding(GenesysTheme.spacing.m) else Modifier)
        ) {
            content()
        }
    }

    CompositionLocalProvider(LocalGenesysDrawerState provides drawerState) {
        if (navigationSuiteItems != null) {
            val layoutType = when (windowSizeClass) {
                GenesysWindowSizeClass.COMPACT -> NavigationSuiteType.NavigationBar
                GenesysWindowSizeClass.MEDIUM -> NavigationSuiteType.NavigationRail
                GenesysWindowSizeClass.EXPANDED -> NavigationSuiteType.NavigationRail
            }

            NavigationSuiteScaffold(
                navigationSuiteItems = navigationSuiteItems,
                layoutType = if (isTestMode()) NavigationSuiteType.NavigationRail else layoutType,
                containerColor = GenesysTheme.colors.background,
                content = {
                    Scaffold(
                        topBar = topBar,
                        floatingActionButton = floatingActionButton,
                        containerColor = Color.Transparent,
                        content = { pageContent(it) }
                    )
                }
            )
        } else {
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
                        Scaffold(
                            topBar = topBar,
                            bottomBar = bottomBar,
                            floatingActionButton = floatingActionButton,
                            containerColor = GenesysTheme.colors.background,
                            content = { pageContent(it) }
                        )
                    }
                )
            } else if (drawerContent != null) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = GenesysTheme.colors.surface,
                            drawerContentColor = GenesysTheme.colors.onSurface
                        ) {
                            drawerContent()
                        }
                    },
                    content = {
                        Scaffold(
                            topBar = topBar,
                            bottomBar = bottomBar,
                            floatingActionButton = floatingActionButton,
                            containerColor = GenesysTheme.colors.background,
                            content = { pageContent(it) }
                        )
                    }
                )
            } else {
                Scaffold(
                    topBar = topBar,
                    bottomBar = bottomBar,
                    floatingActionButton = floatingActionButton,
                    containerColor = GenesysTheme.colors.background,
                    content = { pageContent(it) }
                )
            }
        }
    }
}

val LocalGenesysDrawerState = staticCompositionLocalOf<DrawerState?> { null }
