package com.itbenevides.genesys21.ui.components.templates.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
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
    navigationSuiteItems: (NavigationSuiteScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == GenesysWindowSizeClass.EXPANDED

    // If navigation items are provided, we use the Adaptive Suite
    if (navigationSuiteItems != null) {
        NavigationSuiteScaffold(
            navigationSuiteItems = navigationSuiteItems,
            containerColor = MaterialTheme.colorScheme.background,
            content = {
                ScaffoldContent(
                    topBar = topBar,
                    bottomBar = bottomBar,
                    floatingActionButton = floatingActionButton,
                    uriHandler = uriHandler,
                    content = content
                )
            }
        )
    } else {
        ScaffoldContent(
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            uriHandler = uriHandler,
            content = content
        )
    }
}

@Composable
private fun ScaffoldContent(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    content: @Composable () -> Unit
) {
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
