package com.itbenevides.genesys21.ui.components.templates.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.ui.util.isTestMode

/**
 * GenesysPage: O container mestre do Design System.
 * Otimizado para ACHATAMENTO de hierarquia, garantindo que o scroll funcione nas abas.
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
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == GenesysWindowSizeClass.EXPANDED
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val mainContent: @Composable (PaddingValues) -> Unit = { padding ->
        // No portal ADM, o padding do Scaffold deve ser aplicado aqui.
        // O conteúdo (Tabs) deve preencher o resto e gerenciar seu próprio scroll interno.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        modifier = Modifier.fillMaxSize(),
                        topBar = topBar,
                        floatingActionButton = floatingActionButton,
                        containerColor = Color.Transparent,
                        content = { mainContent(it) }
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
                            modifier = Modifier.fillMaxSize(),
                            topBar = topBar,
                            bottomBar = bottomBar,
                            floatingActionButton = floatingActionButton,
                            containerColor = GenesysTheme.colors.background,
                            content = { mainContent(it) }
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
                            modifier = Modifier.fillMaxSize(),
                            topBar = topBar,
                            bottomBar = bottomBar,
                            floatingActionButton = floatingActionButton,
                            containerColor = GenesysTheme.colors.background,
                            content = { mainContent(it) }
                        )
                    }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = topBar,
                    bottomBar = bottomBar,
                    floatingActionButton = floatingActionButton,
                    containerColor = GenesysTheme.colors.background,
                    content = { mainContent(it) }
                )
            }
        }
    }
}

val LocalGenesysDrawerState = staticCompositionLocalOf<DrawerState?> { null }
