package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * GenesysPage - O container fundamental de tela do Design System.
 * Implementa a lógica de centralização adaptativa para Desktop e Mobile.
 */
@Composable
fun GenesysPage(
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    useMaxWidth: Boolean = true, // Permite desativar em telas que realmente precisam de full-width (como banners)
    content: @Composable () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    
    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        content = { padding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                val isWideScreen = maxWidth > 1024.dp
                
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .then(
                            if (useMaxWidth) {
                                // Aplica largura máxima no Desktop, full no Mobile
                                Modifier.widthIn(max = if (isWideScreen) 1200.dp else maxWidth)
                            } else {
                                Modifier.fillMaxWidth()
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // CONTEÚDO PRINCIPAL (Flexível)
                    Box(modifier = Modifier.weight(1f)) {
                        content()
                    }
                    
                    // ASSINATURA DE RODAPÉ (Respeita o sistema de cores M3)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .clickable { uriHandler.openUri("https://victorbenevides.dev") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "desenvolvido por victorbenevides.dev",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
