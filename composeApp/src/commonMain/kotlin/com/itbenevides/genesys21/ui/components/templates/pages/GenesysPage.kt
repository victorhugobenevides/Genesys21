package com.itbenevides.genesys21.ui.components.templates.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.ui.util.isTestMode

/**
 * GenesysPage: O container mestre do Design System.
 * Ultra-achatado para garantir que o scroll funcione nas abas do portal ADM.
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

    val contentWithPadding: @Composable (PaddingValues) -> Unit = { padding ->
        // RESPONSIVIDADE ELITE: Centralizamos o conteúdo em telas grandes (max 1200dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1200.dp)
            ) {
                content()
            }
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
                        content = { contentWithPadding(it) }
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
                            content = { contentWithPadding(it) }
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
                            content = { contentWithPadding(it) }
                        )
                    }
                )
            } else {
                Scaffold(
                    topBar = topBar,
                    bottomBar = bottomBar,
                    floatingActionButton = floatingActionButton,
                    containerColor = GenesysTheme.colors.background,
                    content = { contentWithPadding(it) }
                )
            }
        }
    }
}

val LocalGenesysDrawerState = staticCompositionLocalOf<DrawerState?> { null }
