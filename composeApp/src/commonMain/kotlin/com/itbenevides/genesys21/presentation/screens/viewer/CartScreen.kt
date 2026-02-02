package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.AnalyticsManager
import org.koin.compose.viewmodel.koinViewModel
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.input.GenesysQuantitySelector
import kotlin.math.roundToLong

@Composable
fun CartScreen(
    page: Page? = null,
    onBack: () -> Unit,
    onOrderSubmitted: (String) -> Unit = {}
) {
    val viewModel: PageViewModel = koinViewModel()
    val cartItems by viewModel.cart.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState() // CARREGA TELEFONE SALVO
    val isLoading by viewModel.isLoading.collectAsState()
    val backendUrl = remember { getBaseUrl() }

    var state by remember { mutableStateOf(CartScreenState()) }
    
    // Sincroniza o estado inicial com os dados persistidos
    LaunchedEffect(customerName, customerPhone) {
        state = state.copy(
            customerName = customerName,
            customerPhone = customerPhone
        )
    }

    LaunchedEffect(cartItems, total, isLoading) {
        state = state.copy(
            cartItems = cartItems,
            total = total,
            isLoading = isLoading
        )
    }

    LaunchedEffect(Unit) {
        AnalyticsManager.trackPageView(GenesysStrings.CartTitle)
    }

    val onEvent: (CartScreenEvent) -> Unit = { event ->
        when (event) {
            is CartScreenEvent.OnUpdateQuantity -> viewModel.updateCartQuantity(event.productId, event.newQuantity)
            is CartScreenEvent.OnRemoveItem -> {
                AnalyticsManager.logEvent("remove_from_cart", mapOf("item_id" to event.productId))
                viewModel.removeFromCart(event.productId)
            }
            is CartScreenEvent.OnCustomerNameChanged -> viewModel.saveCustomerName(event.name)
            is CartScreenEvent.OnCustomerPhoneChanged -> viewModel.saveCustomerPhone(event.phone) // SALVA TELEFONE NA HORA
            is CartScreenEvent.OnCheckoutClicked -> {
                viewModel.submitOrder(page, state.customerPhone) { orderId ->
                    onOrderSubmitted(orderId)
                }
            }
            is CartScreenEvent.OnBackClicked -> onBack()
        }
    }

    CartContent(state, backendUrl, onEvent)
}

@Composable
private fun CartContent(
    state: CartScreenState,
    backendUrl: String,
    onEvent: (CartScreenEvent) -> Unit
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.CartTitle,
                onBack = { onEvent(CartScreenEvent.OnBackClicked) }
            )
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 900.dp

            if (state.cartItems.isEmpty() && !state.isLoading) {
                GenesysEmptyState(
                    icon = GenesysIcons.ShoppingBag,
                    title = GenesysStrings.EmptyCartTitle,
                    description = GenesysStrings.EmptyCartDescription,
                    action = {
                        GenesysLoadingButton(
                            text = GenesysStrings.Back, 
                            onClick = { onEvent(CartScreenEvent.OnBackClicked) }
                        )
                    }
                )
            } else {
                if (isWideScreen) {
                    GenesysRow(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.Top,
                        usePadding = false
                    ) {
                        GenesysWeightBox(0.65f) {
                            GenesysColumn(usePadding = true, useScroll = true) {
                                GenesysText(
                                    text = GenesysStrings.AppName, 
                                    style = GenesysTextStyle.Title,
                                    fontWeight = GenesysFontWeight.ExtraBold
                                )
                                GenesysSpacer(GenesysSpacing.Medium)
                                
                                state.cartItems.forEach { item ->
                                    ModernCartItemRow(item, backendUrl, onEvent)
                                    GenesysSpacer(GenesysSpacing.Small)
                                }
                            }
                        }

                        GenesysSpacer(GenesysSpacing.Large)

                        GenesysWeightBox(0.35f) {
                            GenesysColumn(usePadding = true) {
                                CheckoutSummarySection(state, onEvent)
                            }
                        }
                    }
                } else {
                    GenesysColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = GenesysAlignment.Center,
                        usePadding = false
                    ) {
                        GenesysWeightBox(1f) {
                            GenesysColumn(usePadding = true, useScroll = true) {
                                CartStepperUI(step = 1)
                                GenesysSpacer(GenesysSpacing.Large)
                                state.cartItems.forEach { item ->
                                    ModernCartItemRow(item, backendUrl, onEvent)
                                    GenesysSpacer(GenesysSpacing.Small)
                                }
                                GenesysSpacer(GenesysSpacing.Large)
                                IdentificationCard(state, onEvent)
                                GenesysSpacer(GenesysSpacing.ExtraLarge)
                            }
                        }

                        GenesysCard(
                            elevation = GenesysDimens.ElevationHigh,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            MobileCheckoutFooter(state, onEvent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdentificationCard(state: CartScreenState, onEvent: (CartScreenEvent) -> Unit) {
    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(
                text = GenesysStrings.Identification, 
                style = GenesysTextStyle.Title,
                fontWeight = GenesysFontWeight.Bold
            )
            GenesysSpacer(GenesysSpacing.Medium)
            
            GenesysTextField(
                value = state.customerName,
                onValueChange = { onEvent(CartScreenEvent.OnCustomerNameChanged(it)) },
                label = GenesysStrings.CustomerNameLabel,
                placeholder = GenesysStrings.CheckoutNameHint,
                icon = GenesysIcons.Person
            )
            
            GenesysSpacer(GenesysSpacing.Medium)
            
            GenesysTextField(
                value = state.customerPhone,
                onValueChange = { onEvent(CartScreenEvent.OnCustomerPhoneChanged(it)) },
                label = "Seu WhatsApp / Telefone",
                placeholder = "(00) 00000-0000",
                icon = GenesysIcons.Chat
            )
        }
    }
}

@Composable
private fun CheckoutSummarySection(state: CartScreenState, onEvent: (CartScreenEvent) -> Unit) {
    GenesysColumn(usePadding = false) {
        IdentificationCard(state, onEvent)
        GenesysSpacer(GenesysSpacing.Large)
        GenesysCard(backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)) {
            GenesysColumn(usePadding = false) {
                GenesysRow {
                    GenesysWeightBox(1f) {
                        GenesysText(text = GenesysStrings.Total, style = GenesysTextStyle.Title)
                    }
                    val totalFormatted = (state.total * 100.0).roundToLong() / 100.0
                    GenesysText(
                        text = "${GenesysStrings.PricePrefix}$totalFormatted", 
                        style = GenesysTextStyle.Headline, 
                        fontWeight = GenesysFontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                GenesysSpacer(GenesysSpacing.Large)
                GenesysLoadingButton(
                    text = GenesysStrings.CheckoutButton,
                    onClick = { onEvent(CartScreenEvent.OnCheckoutClicked) },
                    fillWidth = true,
                    enabled = state.isCheckoutEnabled,
                    icon = GenesysIcons.Check,
                    isLoading = state.isLoading
                )
                if (!state.isCheckoutEnabled && state.cartItems.isNotEmpty()) {
                    GenesysSpacer(GenesysSpacing.Small)
                    GenesysText(
                        text = "Preencha nome e telefone para continuar", 
                        style = GenesysTextStyle.Label,
                        textAlign = GenesysTextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileCheckoutFooter(state: CartScreenState, onEvent: (CartScreenEvent) -> Unit) {
    GenesysColumn(usePadding = false) {
        GenesysRow {
            GenesysWeightBox(1f) {
                GenesysText(text = GenesysStrings.Total, style = GenesysTextStyle.Body)
            }
            val totalFormatted = (state.total * 100.0).roundToLong() / 100.0
            GenesysText(
                text = "${GenesysStrings.PricePrefix}$totalFormatted", 
                style = GenesysTextStyle.Title, 
                fontWeight = GenesysFontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysLoadingButton(
            text = GenesysStrings.CheckoutButton,
            onClick = { onEvent(CartScreenEvent.OnCheckoutClicked) },
            fillWidth = true,
            enabled = state.isCheckoutEnabled,
            icon = GenesysIcons.Check,
            isLoading = state.isLoading
        )
    }
}

@Composable
private fun CartStepperUI(step: Int) {
    GenesysRow(horizontalArrangement = Arrangement.Center) {
        repeat(3) { index ->
            val active = index + 1 <= step
            val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            Box(
                modifier = Modifier
                    .size(if (index + 1 == step) 12.dp else 8.dp)
                    .background(color, androidx.compose.foundation.shape.CircleShape)
            )
            if (index < 2) {
                Box(
                    modifier = Modifier.width(24.dp).height(2.dp).background(MaterialTheme.colorScheme.outlineVariant).align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun ModernCartItemRow(
    item: CartItem,
    backendUrl: String,
    onEvent: (CartScreenEvent) -> Unit
) {
    val displayImageUrl = remember(item.product.imageUrls) {
        val first = item.product.imageUrls.firstOrNull() ?: ""
        if (first.startsWith("/")) "$backendUrl$first" else first
    }

    GenesysCard(elevation = GenesysDimens.ElevationLow) {
        GenesysRow(verticalAlignment = Alignment.Top) {
            GenesysImage(
                url = displayImageUrl,
                size = 90.dp
            )
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = item.product.name, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                    val priceFormatted = (item.product.price * 100.0).roundToLong() / 100.0
                    GenesysText(text = "${GenesysStrings.PricePrefix}$priceFormatted", style = GenesysTextStyle.Body, color = MaterialTheme.colorScheme.primary)
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysQuantitySelector(
                        quantity = item.quantity,
                        onIncrease = { onEvent(CartScreenEvent.OnUpdateQuantity(item.product.id, item.quantity + 1)) },
                        onDecrease = { onEvent(CartScreenEvent.OnUpdateQuantity(item.product.id, item.quantity - 1)) }
                    )
                }
            }
            GenesysIconButton(
                icon = GenesysIcons.Delete,
                onClick = { onEvent(CartScreenEvent.OnRemoveItem(item.product.id)) },
                tint = Color.Red.copy(alpha = 0.6f)
            )
        }
    }
}
