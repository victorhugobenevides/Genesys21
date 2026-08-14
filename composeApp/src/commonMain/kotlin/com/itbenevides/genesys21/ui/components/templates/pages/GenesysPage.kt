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
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalTestMode
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

/**
 * GenesysPage: The primary Scaffold following Material Design 3 guidelines.
 * Uses NavigationSuiteScaffold for adaptive navigation (BottomBar vs Rail).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
fun GenesysPage(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    navigationSuiteItems: (NavigationSuiteScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val isTestMode = LocalTestMode.current
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == GenesysWindowSizeClass.EXPANDED

    val scaffoldContent: @Composable () -> Unit = {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            containerColor = MaterialTheme.colorScheme.background,
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        content()
                    }

                    // Simple Footer Signature (LGPD & Credits)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clickable { uriHandler.openUri("https://victorbenevides.dev") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "desenvolvido por victorbenevides.dev",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            },
        )
    }

    // Paparazzi/TestMode compatibility: Skip Adaptive Suite which relies on WindowManager
    if (navigationSuiteItems != null && !isTestMode) {
        NavigationSuiteScaffold(
            navigationSuiteItems = navigationSuiteItems,
            containerColor = MaterialTheme.colorScheme.background,
            content = {
                NavigationDrawerWrapper(drawerContent, isExpanded, scaffoldContent)
            }
        )
    } else {
        NavigationDrawerWrapper(drawerContent, isExpanded, scaffoldContent)
    }
}

@Composable
private fun NavigationDrawerWrapper(
    drawerContent: @Composable (ColumnScope.() -> Unit)?,
    isExpanded: Boolean,
    content: @Composable () -> Unit
) {
    if (drawerContent != null && isExpanded) {
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    drawerContent()
                }
            },
            content = content
        )
    } else {
        content()
    }
}
