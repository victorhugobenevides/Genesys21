package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.badge.GenesysStockBadge
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.Analytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong

@Composable
fun ProductDetailsScreen(
    product: Product, 
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val viewModel: PageViewModel = koinViewModel()
    val scope = rememberCoroutineScope()
    val backendUrl = remember { getBaseUrl() }
    val snackbarHostState = remember { SnackbarHostState() }
    
    var state by remember { mutableStateOf(ProductDetailsState(product = product)) }

    LaunchedEffect(product.id) {
        Analytics.logEvent("view_item", mapOf("item_id" to product.id, "item_name" to product.name))
    }

    val onEvent: (ProductDetailsEvent) -> Unit = { event ->
        when (event) {
            is ProductDetailsEvent.OnAddToCartClicked -> {
                scope.launch {
                    state = state.copy(isAddingToCart = true)
                    if (viewModel.addToCart(product)) {
                        delay(200)
                        val result = snackbarHostState.showSnackbar(
                            message = "${product.name} adicionado ao carrinho",
                            actionLabel = "Ver Carrinho",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) onNavigateToCart()
                    } else {
                        snackbarHostState.showSnackbar(GenesysStrings.OutOfStockMessage)
                    }
                    state = state.copy(isAddingToCart = false)
                }
            }
            is ProductDetailsEvent.OnBackClicked -> onBack()
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BoxWithConstraints {
                if (maxWidth < 900.dp) {
                    StickyAddToCartFooter(state, onEvent)
                }
            }
        }
    ) { padding ->
        ProductDetailsContent(
            state = state, 
            backendUrl = backendUrl, 
            onEvent = onEvent,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun ProductDetailsContent(
    state: ProductDetailsState,
    backendUrl: String,
    onEvent: (ProductDetailsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val wide = maxWidth > 900.dp
        
        if (wide) {
            DesktopLayout(state, backendUrl, onEvent)
        } else {
            MobileLayout(state, scrollState, backendUrl, onEvent)
        }
    }
}

@Composable
private fun MobileLayout(
    state: ProductDetailsState,
    scrollState: androidx.compose.foundation.ScrollState,
    backendUrl: String,
    onEvent: (ProductDetailsEvent) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { state.product.imageUrls.size.coerceAtLeast(1) })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.9f)) {
            ProductImageCarousel(state, pagerState, backendUrl)
            
            Surface(
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.2f)
            ) {
                GenesysIconButton(
                    icon = GenesysIcons.ArrowLeft,
                    onClick = { onEvent(ProductDetailsEvent.OnBackClicked) },
                    tint = Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            ProductHeaderSection(state)
            GenesysSpacer(GenesysSpacing.Large)
            
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Large)
            
            ProductDescriptionSection(state)
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ProductHeaderSection(state: ProductDetailsState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            GenesysText(
                text = state.product.name, 
                style = GenesysTextStyle.Headline, 
                fontWeight = GenesysFontWeight.ExtraBold
            )
            val priceFormatted = (state.product.price * 100.0).roundToLong() / 100.0
            GenesysText(
                text = "${GenesysStrings.PricePrefix}$priceFormatted", 
                style = GenesysTextStyle.Title, 
                color = MaterialTheme.colorScheme.primary,
                fontWeight = GenesysFontWeight.ExtraBold,
                modifier = Modifier.testTag("product_price")
            )
        }
        GenesysStockBadge(stock = state.product.stock)
    }
}

@Composable
private fun ProductDescriptionSection(state: ProductDetailsState) {
    GenesysText(
        text = GenesysStrings.Description, 
        style = GenesysTextStyle.Label,
        fontWeight = GenesysFontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    GenesysSpacer(GenesysSpacing.Small)
    GenesysText(
        text = state.product.description.ifBlank { GenesysStrings.ProductDescriptionFallback }, 
        style = GenesysTextStyle.Body
    )
}

@Composable
private fun StickyAddToCartFooter(
    state: ProductDetailsState,
    onEvent: (ProductDetailsEvent) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.4f)) {
                Text("Total", style = MaterialTheme.typography.labelSmall)
                val priceFormatted = (state.product.price * 100.0).roundToLong() / 100.0
                Text(
                    "${GenesysStrings.PricePrefix}$priceFormatted",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("footer_total_price")
                )
            }
            
            GenesysLoadingButton(
                text = "Adicionar",
                onClick = { onEvent(ProductDetailsEvent.OnAddToCartClicked) },
                modifier = Modifier.weight(0.6f).testTag("btn_sticky_add_to_cart"),
                isLoading = state.isAddingToCart,
                enabled = state.product.stock > 0,
                icon = GenesysIcons.ShoppingBag,
                fillWidth = true
            )
        }
    }
}

@Composable
private fun ProductImageCarousel(
    state: ProductDetailsState, 
    pagerState: androidx.compose.foundation.pager.PagerState,
    backendUrl: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            val url = state.product.imageUrls.getOrNull(index) ?: ""
            val fullUrl = if (url.startsWith("/")) "$backendUrl$url" else url
            AsyncImage(
                model = fullUrl, 
                contentDescription = null, 
                modifier = Modifier.fillMaxSize(), 
                contentScale = ContentScale.Crop
            )
        }
        
        if (state.product.imageUrls.size > 1) {
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                GenesysPagerIndicator(count = state.product.imageUrls.size, currentPage = pagerState.currentPage)
            }
        }
    }
}

@Composable
private fun DesktopLayout(state: ProductDetailsState, backendUrl: String, onEvent: (ProductDetailsEvent) -> Unit) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
             val pagerState = rememberPagerState(pageCount = { state.product.imageUrls.size.coerceAtLeast(1) })
             ProductImageCarousel(state, pagerState, backendUrl)
        }
        Column(modifier = Modifier.weight(1f).padding(40.dp).verticalScroll(rememberScrollState())) {
            ProductHeaderSection(state)
            GenesysSpacer(GenesysSpacing.Large)
            ProductDescriptionSection(state)
            GenesysSpacer(GenesysSpacing.Huge)
            GenesysLoadingButton(
                text = "Adicionar ao Carrinho",
                onClick = { onEvent(ProductDetailsEvent.OnAddToCartClicked) },
                isLoading = state.isAddingToCart,
                enabled = state.product.stock > 0,
                icon = GenesysIcons.ShoppingBag,
                fillWidth = true
            )
        }
    }
}
