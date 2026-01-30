package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.badge.GenesysStockBadge
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysConfirmDialog
import com.itbenevides.genesys21.ui.components.layout.GenesysAlignment
import com.itbenevides.genesys21.ui.components.layout.GenesysBox
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysDivider
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysRow
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.layout.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.AnalyticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductDetailsScreen(
    product: Product, 
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val viewModel: PageViewModel = koinViewModel()
    val scope = rememberCoroutineScope()
    val backendUrl = remember { getBaseUrl() }
    
    // 1. State Management
    var state by remember { mutableStateOf(ProductDetailsState(product = product)) }

    LaunchedEffect(product.id) {
        AnalyticsManager.logEvent("view_item", mapOf("item_id" to product.id, "item_name" to product.name, "price" to product.price))
    }

    // 2. Event Handler
    val onEvent: (ProductDetailsEvent) -> Unit = { event ->
        when (event) {
            is ProductDetailsEvent.OnAddToCartClicked -> {
                scope.launch {
                    state = state.copy(isAddingToCart = true)
                    if (viewModel.addToCart(product)) {
                        delay(300)
                        state = state.copy(showSuccessDialog = true)
                    } else {
                        state = state.copy(error = GenesysStrings.OutOfStockMessage)
                    }
                    state = state.copy(isAddingToCart = false)
                }
            }
            is ProductDetailsEvent.OnDismissSuccessDialog -> state = state.copy(showSuccessDialog = false)
            is ProductDetailsEvent.OnViewCartClicked -> {
                state = state.copy(showSuccessDialog = false)
                onNavigateToCart()
            }
            is ProductDetailsEvent.OnContinueShoppingClicked -> {
                state = state.copy(showSuccessDialog = false)
                onBack()
            }
            is ProductDetailsEvent.OnBackClicked -> onBack()
        }
    }

    // 3. Renderização
    ProductDetailsContent(state, backendUrl, onEvent)
}

@Composable
private fun ProductDetailsContent(
    state: ProductDetailsState,
    backendUrl: String,
    onEvent: (ProductDetailsEvent) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { state.product.imageUrls.size.coerceAtLeast(1) })
    
    val buttonScale by animateFloatAsState(
        targetValue = if (state.isAddingToCart) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.Details,
                onBack = { onEvent(ProductDetailsEvent.OnBackClicked) }
            )
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 900.dp

            if (isWideScreen) {
                // --- LAYOUT DESKTOP / WASM (DUAS COLUNAS) ---
                GenesysRow(modifier = Modifier.fillMaxSize(), usePadding = false) {
                    // Coluna 1: Carrossel de Imagens (Fixa ou Proporcional)
                    GenesysWeightBox(0.5f) {
                        GenesysBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ProductImageCarousel(state, pagerState, backendUrl)
                        }
                    }

                    // Coluna 2: Detalhes e Compra (Com scroll independente)
                    GenesysWeightBox(0.5f) {
                        GenesysColumn(
                            usePadding = true, 
                            useScroll = true, 
                            modifier = Modifier.fillMaxHeight(),
                            horizontalAlignment = GenesysAlignment.Start
                        ) {
                            ProductInfoSection(state, buttonScale, onEvent)
                        }
                    }
                }
            } else {
                // --- LAYOUT MOBILE (COLUNA ÚNICA) ---
                GenesysColumn(
                    modifier = Modifier.fillMaxSize(),
                    usePadding = false,
                    useScroll = true,
                    horizontalAlignment = GenesysAlignment.Center
                ) {
                    // Imagem ocupa o topo no mobile
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                        ProductImageCarousel(state, pagerState, backendUrl)
                    }

                    GenesysSpacer(GenesysSpacing.Large)

                    // Informações abaixo
                    GenesysColumn(maxWidth = 600.dp, usePadding = true) {
                        ProductInfoSection(state, buttonScale, onEvent)
                    }
                    
                    GenesysSpacer(GenesysSpacing.Huge)
                }
            }
        }
    }

    if (state.showSuccessDialog) {
        SuccessDialogUI(state, onEvent)
    }
}

@Composable
private fun ProductImageCarousel(
    state: ProductDetailsState, 
    pagerState: androidx.compose.foundation.pager.PagerState,
    backendUrl: String
) {
    GenesysCard(
        modifier = Modifier.fillMaxSize(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp), // Sangrado no container
        elevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.product.imageUrls.isNotEmpty()) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
                    val url = state.product.imageUrls[index]
                    val fullUrl = if (url.startsWith("/") && !url.startsWith("http")) "$backendUrl$url" else url
                    AsyncImage(
                        model = fullUrl, 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize(), 
                        contentScale = ContentScale.Crop
                    )
                }
                
                if (state.product.imageUrls.size > 1) {
                    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
                        GenesysPagerIndicator(
                            count = state.product.imageUrls.size,
                            currentPage = pagerState.currentPage
                        )
                    }
                }
            } else {
                GenesysBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    GenesysIconButton(
                        icon = GenesysIcons.ShoppingBag,
                        modifier = Modifier.size(120.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductInfoSection(
    state: ProductDetailsState,
    buttonScale: Float,
    onEvent: (ProductDetailsEvent) -> Unit
) {
    GenesysText(
        text = state.product.name.ifBlank { GenesysStrings.ProductName }, 
        style = GenesysTextStyle.Headline, 
        fontWeight = GenesysFontWeight.ExtraBold
    )
    
    GenesysSpacer(GenesysSpacing.Small)
    
    GenesysText(
        text = "${GenesysStrings.PricePrefix}${state.product.price}", 
        style = GenesysTextStyle.Title, 
        fontWeight = GenesysFontWeight.ExtraBold, 
        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
    )
    
    GenesysSpacer(GenesysSpacing.Medium)
    
    GenesysStockBadge(stock = state.product.stock)
    
    GenesysSpacer(GenesysSpacing.ExtraLarge)
    GenesysDivider()
    GenesysSpacer(GenesysSpacing.Large)
    
    GenesysText(
        text = GenesysStrings.Description, 
        style = GenesysTextStyle.Label,
        fontWeight = GenesysFontWeight.Bold
    )
    GenesysSpacer(GenesysSpacing.Small)
    
    GenesysText(
        text = state.product.description.ifBlank { GenesysStrings.ProductDescriptionFallback }, 
        style = GenesysTextStyle.Body
    )
    
    GenesysSpacer(GenesysSpacing.ExtraLarge)
    
    GenesysLoadingButton(
        text = GenesysStrings.AddToCartAction,
        onClick = { onEvent(ProductDetailsEvent.OnAddToCartClicked) },
        modifier = Modifier.fillMaxWidth().scale(buttonScale),
        isLoading = state.isAddingToCart,
        enabled = state.product.stock > 0,
        icon = GenesysIcons.ShoppingBag,
        fillWidth = true
    )
}

@Composable
private fun SuccessDialogUI(state: ProductDetailsState, onEvent: (ProductDetailsEvent) -> Unit) {
    GenesysConfirmDialog(
        onDismissRequest = { onEvent(ProductDetailsEvent.OnDismissSuccessDialog) },
        icon = GenesysIcons.Check,
        title = GenesysStrings.AddedToCartTitle,
        text = "${state.product.name} ${GenesysStrings.AddedToCartMessageSuffix}",
        confirmButton = { 
            GenesysLoadingButton(
                text = GenesysStrings.ViewCart, 
                onClick = { onEvent(ProductDetailsEvent.OnViewCartClicked) },
                fillWidth = true
            ) 
        },
        dismissButton = { 
            GenesysTextButton(
                text = GenesysStrings.ContinueShopping, 
                onClick = { onEvent(ProductDetailsEvent.OnContinueShoppingClicked) }
            ) 
        }
    )
}
