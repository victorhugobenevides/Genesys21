package com.itbenevides.genesys21.ui.components.organisms.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

/**
 * Standard Genesys21 TopAppBar following Material Design 3 guidelines.
 * Features center alignment and responsive typography.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesysTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color? = null,
    isTranslucent: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    val finalContainerColor = when {
        isTranslucent -> MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
        containerColor != null -> containerColor
        else -> MaterialTheme.colorScheme.surface
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = if (isCompact) {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        modifier = Modifier.size(24.dp), // M3 standard size
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = finalContainerColor,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), // M3 elevation tint
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
    )
}
