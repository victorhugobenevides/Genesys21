package com.itbenevides.genesys21.presentation.screens.viewer

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
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysTextAlign
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.AnalyticsManager
import org.koin.compose.viewmodel.koinViewModel
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.input.GenesysQuantitySelector

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
    val isLoading by viewModel.isLoading.collectAsState()
    val backendUrl = remember { getBaseUrl() }

    var state by remember { mutableStateOf(CartScreenState()) }
    
    state = state.copy(
        cartItems = cartItems,
        total = total,
        customerName = customerName,
        isLoading = isLoading
    )

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
            is CartScreenEvent.OnCheckoutClicked -> {
                viewModel.submitOrder(page) { orderId ->
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
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false
        ) {
            GenesysColumn(maxWidth = GenesysDimens.ContentMaxWidth) {
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
                    GenesysWeightBox(1f) {
                        GenesysColumn(usePadding = true, useScroll = true) {
                            GenesysCard {
                                GenesysColumn(usePadding = false) {
                                    GenesysText(GenesysStrings.Identification, style = GenesysTextStyle.Title)
                                    GenesysSpacer(GenesysSpacing.Medium)
                                    
                                    GenesysTextField(
                                        value = state.customerName,
                                        onValueChange = { onEvent(CartScreenEvent.OnCustomerNameChanged(it)) },
                                        label = GenesysStrings.CustomerNameLabel,
                                        placeholder = "Como gostaria de ser chamado?",
                                        icon = GenesysIcons.Person
                                    )
                                }
                            }

                            GenesysSpacer(GenesysSpacing.Large)

                            state.cartItems.forEach { item ->
                                ModernCartItemRow(
                                    item = item,
                                    backendUrl = backendUrl,
                                    onEvent = onEvent
                                )
                                GenesysSpacer(GenesysSpacing.Small)
                            }
                        }
                    }

                    GenesysSpacer(GenesysSpacing.Medium)

                    GenesysCard {
                        GenesysColumn(usePadding = false) {
                            GenesysRow {
                                GenesysWeightBox(1f) {
                                    GenesysText(
                                        text = GenesysStrings.Total, 
                                        style = GenesysTextStyle.Title
                                    )
                                }
                                GenesysText(
                                    text = "R$ ${state.total}", 
                                    style = GenesysTextStyle.Title, 
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
                            
                            if (state.customerName.isBlank()) {
                                GenesysSpacer(GenesysSpacing.Small)
                                GenesysText(
                                    text = "Preencha seu nome para finalizar", 
                                    style = GenesysTextStyle.Label,
                                    textAlign = GenesysTextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
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

    GenesysCard {
        GenesysRow(verticalAlignment = Alignment.Top) {
            GenesysImage(
                url = displayImageUrl,
                size = 80.dp
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(
                        text = item.product.name,
                        style = GenesysTextStyle.Body,
                        fontWeight = GenesysFontWeight.Bold
                    )
                    GenesysText(
                        text = "R$ ${item.product.price}",
                        style = GenesysTextStyle.Body
                    )
                    
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
