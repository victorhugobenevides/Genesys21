package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.badge.GenesysStockBadge
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.feedback.GenesysConfirmDialog
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.theme.GenesysDimens
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

    // 3. Render
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            GenesysColumn(
                maxWidth = GenesysDimens.ViewerMaxWidth,
                useScroll = true,
                horizontalAlignment = GenesysAlignment.Center
            ) {
                // CARROSSEL DE IMAGENS
                GenesysCard(
                    modifier = Modifier
                        .widthIn(max = 700.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    elevation = 4.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (state.product.imageUrls.isNotEmpty()) {
                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
                                val url = state.product.imageUrls[index]
                                val fullUrl = if (url.startsWith("/")) "$backendUrl$url" else url
                                AsyncImage(model = fullUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            
                            if (state.product.imageUrls.size > 1) {
                                Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                                    GenesysPagerIndicator(
                                        count = state.product.imageUrls.size,
                                        currentPage = pagerState.currentPage
                                    )
                                }
                            }
                        } else {
                            GenesysIconButton(
                                icon = GenesysIcons.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).align(Alignment.Center),
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                onClick = {}
                            )
                        }
                    }
                }

                GenesysSpacer(GenesysSpacing.Large)

                GenesysCard(elevation = 2.dp) {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = state.product.name.ifBlank { GenesysStrings.ProductName }, 
                            style = GenesysTextStyle.Headline, 
                            fontWeight = GenesysFontWeight.ExtraBold
                        )
                        
                        GenesysSpacer(GenesysSpacing.Small)
                        
                        GenesysText(
                            text = "R$ ${state.product.price}", 
                            style = GenesysTextStyle.Title, 
                            fontWeight = GenesysFontWeight.ExtraBold, 
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                        
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysStockBadge(stock = state.product.stock)
                        
                        GenesysSpacer(GenesysSpacing.Large)
                        GenesysDivider()
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysSectionHeader(title = GenesysStrings.Description)
                        GenesysSpacer(GenesysSpacing.Small)
                        
                        GenesysText(
                            text = state.product.description.ifBlank { GenesysStrings.ProductDescriptionFallback }, 
                            style = GenesysTextStyle.Body
                        )
                        
                        GenesysSpacer(GenesysSpacing.Large)
                        
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
                }
                
                GenesysSpacer(GenesysSpacing.Huge)
            }
        }
    }

    if (state.showSuccessDialog) {
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
}
