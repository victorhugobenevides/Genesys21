package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.StatusBadge
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit
) {
    val viewModel: PageViewModel = koinViewModel()
    val order by viewModel.trackedOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(orderId) {
        viewModel.trackOrder(orderId)
    }

    // Identifica o tema do pedido ou usa o padrão enquanto carrega
    val themeToUse = order?.theme ?: com.itbenevides.genesys21.domain.model.PageThemeConfig.ROYAL

    AppTheme(themeConfig = themeToUse) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Detalhes do Pedido", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text("Voltar", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                val maxWidthContent = 1000.dp
                val horizontalPadding = if (maxWidth > maxWidthContent) (maxWidth - maxWidthContent) / 2 else 12.dp

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (order == null) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        Text("Pedido não encontrado", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val currentOrder = order!!
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card de Status
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 1.dp
                        ) {
                            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("STATUS ATUAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                StatusBadge(currentOrder.status)
                                
                                Spacer(Modifier.height(24.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("#${currentOrder.id.uppercase()}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
                                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(currentOrder.id)) }) {
                                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                        TrackingTimeline(currentOrder.status)
                        Spacer(Modifier.height(32.dp))

                        // Itens e Resumo
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Column(Modifier.padding(24.dp)) {
                                Text("Resumo do Pedido", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(16.dp))
                                
                                currentOrder.items.forEach { item ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${item.quantity}x ${item.product.name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        Text("R$ ${item.product.price * item.quantity}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(Modifier.height(16.dp))
                                
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("R$ ${currentOrder.total}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(40.dp))
                        Text(
                            "Dica: Salve o ID do pedido para futuras consultas.", 
                            textAlign = TextAlign.Center, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingTimeline(currentStatus: OrderStatus) {
    val steps = listOf(
        OrderStatus.PENDING to "Pedido Recebido",
        OrderStatus.PROCESSING to "Em Preparação",
        OrderStatus.COMPLETED to "Pedido Concluído"
    )

    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        steps.forEachIndexed { index, (status, label) ->
            val isCompleted = when (currentStatus) {
                OrderStatus.CANCELLED -> false
                OrderStatus.PENDING -> index == 0
                OrderStatus.PROCESSING -> index <= 1
                OrderStatus.COMPLETED -> true
            }
            
            val isActive = when (currentStatus) {
                OrderStatus.PENDING -> index == 0
                OrderStatus.PROCESSING -> index == 1
                OrderStatus.COMPLETED -> index == 2
                else -> false
            }

            val color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) color else color.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted && !isActive) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        } else if (isActive) {
                            Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.onPrimary, CircleShape))
                        }
                    }
                    if (index < steps.size - 1) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .background(if (isCompleted && currentStatus != status) color else color.copy(alpha = 0.3f))
                        )
                    }
                }
                
                Spacer(Modifier.width(20.dp))
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        
        if (currentStatus == OrderStatus.CANCELLED) {
            Spacer(Modifier.height(20.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Text("Este pedido foi cancelado.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
