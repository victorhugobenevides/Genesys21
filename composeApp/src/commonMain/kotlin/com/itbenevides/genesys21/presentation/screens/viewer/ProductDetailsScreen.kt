package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.badge.GenesysStockBadge
import com.itbenevides.genesys21.ui.components.feedback.GenesysConfirmDialog
import com.itbenevides.genesys21.ui.components.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.util.AnalyticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    product: Product, 
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val viewModel: PageViewModel = koinViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val backendUrl = remember { getBaseUrl() }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isAdding by remember { mutableStateOf(false) }
    
    val pagerState = rememberPagerState(pageCount = { product.imageUrls.size.coerceAtLeast(1) })

    val buttonScale by animateFloatAsState(
        targetValue = if (isAdding) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(product.id) {
        AnalyticsManager.logEvent("view_item", mapOf("item_id" to product.id, "item_name" to product.name, "price" to product.price))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GenesysTopAppBar(
                title = "Detalhes",
                onBack = onBack
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            val maxWidthContent = 1300.dp
            val horizontalPadding = if (maxWidth > maxWidthContent) (maxWidth - maxWidthContent) / 2 else 12.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CARROSSEL DE IMAGENS
                GenesysCard(
                    modifier = Modifier
                        .widthIn(max = 700.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 4.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (product.imageUrls.isNotEmpty()) {
                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
                                val url = product.imageUrls[index]
                                val fullUrl = if (url.startsWith("/")) "$backendUrl$url" else url
                                AsyncImage(model = fullUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            
                            if (product.imageUrls.size > 1) {
                                Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                                    GenesysPagerIndicator(
                                        count = product.imageUrls.size,
                                        currentPage = pagerState.currentPage
                                    )
                                }
                            }
                        } else {
                            Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(80.dp).align(Alignment.Center), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                GenesysCard(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 2.dp
                ) {
                    Text(text = product.name.ifBlank { "Produto sem nome" }, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "R$ ${product.price}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    GenesysStockBadge(stock = product.stock)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    GenesysSectionHeader(title = "Descrição")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = product.description.ifBlank { "Este é um produto premium disponível na Genesys21." }, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                     GenesysLoadingButton(
                        text = "Adicionar ao Carrinho",
                        onClick = { 
                            scope.launch {
                                isAdding = true
                                if (viewModel.addToCart(product)) {
                                    delay(300)
                                    showSuccessDialog = true 
                                } else {
                                    snackbarHostState.showSnackbar("Sem estoque disponível!") 
                                }
                                isAdding = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp).scale(buttonScale),
                        isLoading = isAdding,
                        enabled = product.stock > 0,
                        icon = Icons.Default.ShoppingBag,
                        shape = CircleShape
                    )
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    if (showSuccessDialog) {
        GenesysConfirmDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = Icons.Default.CheckCircle,
            title = "Adicionado!",
            text = "${product.name} foi adicionado ao carrinho.",
            confirmButton = { 
                Button(onClick = { showSuccessDialog = false; onNavigateToCart() }, modifier = Modifier.fillMaxWidth(), shape = CircleShape) { 
                    Text("Ver Carrinho") 
                } 
            },
            dismissButton = { 
                TextButton(onClick = { showSuccessDialog = false; onBack() }, modifier = Modifier.fillMaxWidth()) { 
                    Text("Continuar Comprando") 
                } 
            }
        )
    }
}
