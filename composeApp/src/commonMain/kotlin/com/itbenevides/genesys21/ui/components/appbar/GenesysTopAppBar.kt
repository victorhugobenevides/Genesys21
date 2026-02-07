package com.itbenevides.genesys21.ui.components.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons

/**
 * GenesysTopAppBar - Barra de navegação padronizada seguindo Material 3.
 * Implementa suporte a títulos semânticos e estados de elevação.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesysTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    CenterAlignedTopAppBar(
        title = { 
            GenesysText(
                text = title, 
                style = GenesysTextStyle.Title,
                fontWeight = GenesysFontWeight.ExtraBold,
                color = titleColor
            ) 
        },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = GenesysIcons.ArrowLeft, 
                        contentDescription = "Voltar", 
                        modifier = Modifier.size(24.dp), // Tamanho padrão M3
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = containerColor,
            titleContentColor = titleColor,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}
