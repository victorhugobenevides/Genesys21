package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysStockBadge
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysConfirmDialog
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.glassmorphic
import com.itbenevides.genesys21.util.*
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProductDetailsScreen(
    product: Product,
    pageId: String? = null,
    whatsapp: String? = null,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    val viewModel: PageViewModel = koinViewModel()
    val scope = rememberCoroutineScope()
    val backendUrl = remember { getBaseUrl() }
    val uriHandler = LocalUriHandler.current

    var state by remember { mutableStateOf(ProductDetailsState(product = product)) }

    LaunchedEffect(product.id) {
        AnalyticsManager.trackViewProduct(product.id, product.name, product.price)
    }

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
            is ProductDetailsEvent.OnContactSellerClicked -> {
                whatsapp?.let { number ->
                    val cleanNumber = number.filter { it.isDigit() }
                    val message = "Olá! Vi o produto ${product.name} na sua vitrine e gostaria de mais informações."
                    val url = "https://wa.me/$cleanNumber?text=${message.replace(" ", "%20")}"
                    AnalyticsManager.logEvent("contact_seller", mapOf("product_id" to product.id))
                    uriHandler.openUri(url)
                }
            }
            is ProductDetailsEvent.OnShareProductClicked -> {
                val url =
                    if (pageId != null) {
                        "${getWebBaseUrl()}/p/$pageId/product/${product.id}"
                    } else {
                        "${getWebBaseUrl()}/p/${product.id}"
                    }
                AnalyticsManager.logEvent("share_product", mapOf("product_id" to product.id))
                ShareManagerInstance.shareLink(
                    title = product.name,
                    text = "Olha que produto incrível: ${product.name}",
                    url = url,
                )
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

    ProductDetailsContent(state, backendUrl, onEvent)
}

@Composable
private fun ProductDetailsContent(
    state: ProductDetailsState,
    backendUrl: String,
    onEvent: (ProductDetailsEvent) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { state.product.imageUrls.size.coerceAtLeast(1) })
    val scope = rememberCoroutineScope()

    val buttonScale by animateFloatAsState(
        targetValue = if (state.isAddingToCart) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    )

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.Details,
                onBack = { onEvent(ProductDetailsEvent.OnBackClicked) },
                actions = {
                    GenesysIconButton(
                        icon = GenesysIcons.Share,
                        contentDescription = "Compartilhar Produto",
                        onClick = { onEvent(ProductDetailsEvent.OnShareProductClicked) },
                    )
                },
            )
        },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 900.dp

            if (isWideScreen) {
                // --- LAYOUT DESKTOP ---
                GenesysRow(modifier = Modifier.fillMaxSize(), usePadding = false) {
                    GenesysWeightBox(0.5f) {
                        GenesysBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ProductImageCarousel(state, pagerState, backendUrl) { index ->
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                        }
                    }

                    GenesysWeightBox(0.5f) {
                        GenesysColumn(
                            usePadding = true,
                            useScroll = true,
                            modifier = Modifier.fillMaxHeight(),
                            horizontalAlignment = GenesysAlignment.Start,
                        ) {
                            ProductInfoSection(state, buttonScale, onEvent)
                        }
                    }
                }
            } else {
                // --- LAYOUT MOBILE ---
                GenesysColumn(
                    modifier = Modifier.fillMaxSize(),
                    usePadding = false,
                    useScroll = true,
                    horizontalAlignment = GenesysAlignment.Center,
                ) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                        ProductImageCarousel(state, pagerState, backendUrl) { index ->
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    }

                    GenesysSpacer(GenesysSpacing.Large)

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
    backendUrl: String,
    onNavigate: (Int) -> Unit,
) {
    GenesysCard(
        modifier = Modifier.fillMaxSize(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        elevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.product.imageUrls.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { index ->
                    val url = state.product.imageUrls[index]
                    val fullUrl =
                        remember(url, backendUrl) {
                            if (url.startsWith("/") && !url.startsWith("http")) "$backendUrl$url" else url
                        }
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = "Foto ${index + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                // Botões de Navegação Laterais
                if (state.product.imageUrls.size > 1) {
                    if (pagerState.currentPage > 0) {
                        Surface(
                            modifier =
                                Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 16.dp)
                                    .glassmorphic(CircleShape, alpha = 0.4f),
                            shape = CircleShape,
                            color = Color.Transparent,
                            contentColor = Color.White,
                        ) {
                            GenesysIconButton(
                                icon = GenesysIcons.ArrowLeft,
                                onClick = { onNavigate(pagerState.currentPage - 1) },
                                tint = Color.White,
                            )
                        }
                    }

                    if (pagerState.currentPage < state.product.imageUrls.size - 1) {
                        Surface(
                            modifier =
                                Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                                    .glassmorphic(CircleShape, alpha = 0.4f),
                            shape = CircleShape,
                            color = Color.Transparent,
                            contentColor = Color.White,
                        ) {
                            GenesysIconButton(
                                icon = GenesysIcons.ArrowRight,
                                onClick = { onNavigate(pagerState.currentPage + 1) },
                                tint = Color.White,
                            )
                        }
                    }

                    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
                        GenesysPagerIndicator(
                            count = state.product.imageUrls.size,
                            currentPage = pagerState.currentPage,
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = GenesysIcons.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
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
    onEvent: (ProductDetailsEvent) -> Unit,
) {
    GenesysText(
        text = state.product.name.ifBlank { GenesysStrings.ProductName },
        style = GenesysTextStyle.Headline,
        fontWeight = GenesysFontWeight.ExtraBold,
    )

    GenesysSpacer(GenesysSpacing.Small)

    val priceFormatted = (state.product.price * 100.0).roundToLong() / 100.0
    GenesysText(
        text = "${GenesysStrings.PricePrefix}$priceFormatted",
        style = GenesysTextStyle.Title,
        fontWeight = GenesysFontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
    )

    GenesysSpacer(GenesysSpacing.Medium)

    GenesysStockBadge(stock = state.product.stock)

    GenesysSpacer(GenesysSpacing.ExtraLarge)
    GenesysDivider()
    GenesysSpacer(GenesysSpacing.Large)

    GenesysText(
        text = GenesysStrings.Description,
        style = GenesysTextStyle.Label,
        fontWeight = GenesysFontWeight.Bold,
    )
    GenesysSpacer(GenesysSpacing.Small)

    GenesysText(
        text = state.product.description.ifBlank { GenesysStrings.ProductDescriptionFallback },
        style = GenesysTextStyle.Body,
    )

    GenesysSpacer(GenesysSpacing.ExtraLarge)

    GenesysLoadingButton(
        text = GenesysStrings.AddToCartAction,
        onClick = { onEvent(ProductDetailsEvent.OnAddToCartClicked) },
        modifier = Modifier.fillMaxWidth().scale(buttonScale),
        isLoading = state.isAddingToCart,
        enabled = state.product.stock > 0,
        icon = GenesysIcons.ShoppingBag,
        fillWidth = true,
    )

    GenesysSpacer(GenesysSpacing.Medium)

    GenesysTextButton(
        text = "Falar com o Vendedor",
        onClick = { onEvent(ProductDetailsEvent.OnContactSellerClicked) },
        modifier = Modifier.fillMaxWidth(),
        icon = GenesysIcons.WhatsApp,
    )
}

@Composable
private fun SuccessDialogUI(
    state: ProductDetailsState,
    onEvent: (ProductDetailsEvent) -> Unit,
) {
    GenesysConfirmDialog(
        onDismissRequest = { onEvent(ProductDetailsEvent.OnDismissSuccessDialog) },
        icon = GenesysIcons.Check,
        title = GenesysStrings.AddedToCartTitle,
        text = "${state.product.name} ${GenesysStrings.AddedToCartMessageSuffix}",
        confirmButton = {
            GenesysLoadingButton(
                text = GenesysStrings.ViewCart,
                onClick = { onEvent(ProductDetailsEvent.OnViewCartClicked) },
                fillWidth = true,
            )
        },
        dismissButton = {
            GenesysTextButton(
                text = GenesysStrings.ContinueShopping,
                onClick = { onEvent(ProductDetailsEvent.OnContinueShoppingClicked) },
            )
        },
    )
}
