package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.di.getBaseUrl
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(whatsappNumber: String? = null, onBack: () -> Unit) {
    val viewModel: PageViewModel = koinViewModel()
    val cartItems by viewModel.cart.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val uriHandler = LocalUriHandler.current
    val backendUrl = remember { getBaseUrl() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Seu Carrinho", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Voltar", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            val isDesktop = maxWidth > 800.dp
            val horizontalPadding = if (isDesktop) (maxWidth - 600.dp) / 2 else 16.dp

            if (cartItems.isEmpty()) {
                EmptyCartView(onBack)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        contentPadding = PaddingValues(vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cartItems) { item ->
                            ModernCartItemRow(
                                item = item,
                                backendUrl = backendUrl,
                                onIncrease = { viewModel.updateCartQuantity(item.product.id, item.quantity + 1) },
                                onDecrease = { viewModel.updateCartQuantity(item.product.id, item.quantity - 1) },
                                onRemove = { viewModel.removeFromCart(item.product.id) }
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isDesktop) horizontalPadding else 0.dp)
                            .padding(bottom = if (isDesktop) 24.dp else 0.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        shape = if (isDesktop) RoundedCornerShape(24.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(
                            Modifier
                                .navigationBarsPadding()
                                .padding(24.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                                Text(
                                    "R$ $total", 
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { 
                                    val url = viewModel.generateWhatsappMessage(whatsappNumber)
                                    if (url != null) uriHandler.openUri(url)
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !whatsappNumber.isNullOrBlank()
                            ) {
                                Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Finalizar via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernCartItemRow(
    item: CartItem,
    backendUrl: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    val displayImageUrl = remember(item.product.imageUrls) {
        val first = item.product.imageUrls.firstOrNull() ?: ""
        if (first.startsWith("/")) "$backendUrl$first" else first
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (displayImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = displayImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, null, tint = Color.LightGray)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    item.product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "R$ ${item.product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuantityButton(Icons.Default.Remove, onDecrease)
                    Text("${item.quantity}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    QuantityButton(Icons.Default.Add, onIncrease, isPrimary = true)
                }
            }

            IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.Top)) {
                Icon(Icons.Default.DeleteOutline, "Remover", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun QuantityButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, isPrimary: Boolean = false) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(30.dp),
        shape = CircleShape,
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun EmptyCartView(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ShoppingBag, 
            null, 
            modifier = Modifier.size(80.dp), 
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(24.dp))
        Text("Carrinho Vazio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Você ainda não adicionou nenhum item.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onBack) {
            Text("Explorar Vitrine", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}
