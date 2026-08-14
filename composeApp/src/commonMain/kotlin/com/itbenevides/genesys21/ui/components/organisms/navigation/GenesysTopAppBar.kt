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
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

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
        isTranslucent -> GenesysTheme.colors.background.copy(alpha = 0.8f)
        containerColor != null -> containerColor
        else -> GenesysTheme.colors.surface
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = if (isCompact) GenesysTheme.typography.title else GenesysTheme.typography.headline,
                fontWeight = FontWeight.ExtraBold,
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
                        modifier = Modifier.size(24.dp),
                        tint = GenesysTheme.colors.onSurface
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = finalContainerColor,
            scrolledContainerColor = GenesysTheme.colors.backgroundSecondary,
            titleContentColor = GenesysTheme.colors.onSurface,
            actionIconContentColor = GenesysTheme.colors.onSurface,
            navigationIconContentColor = GenesysTheme.colors.onSurface
        ),
    )
}
