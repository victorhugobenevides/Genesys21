package com.itbenevides.genesys21.ui.components.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesysTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Color.Transparent,
    isTranslucent: Boolean = false
) {
    val finalContainerColor = if (isTranslucent) {
        MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
    } else {
        containerColor
    }

    CenterAlignedTopAppBar(
        modifier = if (isTranslucent) Modifier.background(Color.Transparent) else Modifier,
        title = { 
            Text(
                title, 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            ) 
        },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        "Voltar", 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = finalContainerColor,
            scrolledContainerColor = finalContainerColor
        )
    )
}
