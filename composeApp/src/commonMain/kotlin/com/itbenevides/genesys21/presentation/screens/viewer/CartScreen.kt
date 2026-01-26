package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val backendUrl = remember { getBaseUrl() }
    val customerName by viewModel.customerName.collectAsState()

    LaunchedEffect(Unit) {
        AnalyticsManager.trackPageView(GenesysStrings.CartTitle)
    }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.CartTitle,
                onBack = onBack
            )
        }
    ) {
        // Root que centraliza o conteúdo em telas largas (WasmJs)
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false
        ) {
            // Conteúdo limitado pela largura máxima do DS
            GenesysColumn(maxWidth = GenesysDimens.ContentMaxWidth) {
                if (cartItems.isEmpty()) {
                    GenesysEmptyState(
                        icon = GenesysIcons.ShoppingBag,
                        title = GenesysStrings.EmptyCartTitle,
                        description = GenesysStrings.EmptyCartDescription,
                        action = {
                            GenesysLoadingButton(
                                text = GenesysStrings.Back, 
                                onClick = onBack
                            )
                        }
                    )
                } else {
                    // Usando GenesysWeightBox para garantir que a lista ocupe o espaço e role
                    GenesysWeightBox(1f) {
                        GenesysColumn(usePadding = false, useScroll = true) {
                            GenesysCard {
                                GenesysColumn(usePadding = false) {
                                    GenesysText(GenesysStrings.Identification, style = GenesysTextStyle.Title)
                                    GenesysSpacer(GenesysSpacing.Medium)
                                    
                                    GenesysTextField(
                                        value = customerName,
                                        onValueChange = { newValue -> viewModel.saveCustomerName(newValue) },
                                        label = GenesysStrings.CustomerNameLabel,
                                        placeholder = "Como gostaria de ser chamado?",
                                        icon = GenesysIcons.Person
                                    )
                                }
                            }

                            GenesysSpacer(GenesysSpacing.Large)

                            cartItems.forEach { item ->
                                ModernCartItemRow(
                                    item = item,
                                    backendUrl = backendUrl,
                                    onIncrease = { viewModel.updateCartQuantity(item.product.id, item.quantity + 1) },
                                    onDecrease = { viewModel.updateCartQuantity(item.product.id, item.quantity - 1) },
                                    onRemove = {
                                        AnalyticsManager.logEvent("remove_from_cart", mapOf("item_id" to item.product.id, "item_name" to item.product.name))
                                        viewModel.removeFromCart(item.product.id)
                                    }
                                )
                                GenesysSpacer(GenesysSpacing.Small)
                            }
                        }
                    }

                    GenesysSpacer(GenesysSpacing.Medium)

                    GenesysCard {
                        GenesysColumn(usePadding = false) {
                            GenesysRow {
                                GenesysText(
                                    text = GenesysStrings.Total, 
                                    style = GenesysTextStyle.Title,
                                    weightValue = 1f
                                )
                                GenesysText(
                                    text = "R$ $total", 
                                    style = GenesysTextStyle.Title, 
                                    fontWeight = GenesysFontWeight.ExtraBold
                                )
                            }
                            GenesysSpacer(GenesysSpacing.Large)
                            
                            GenesysLoadingButton(
                                text = GenesysStrings.CheckoutButton,
                                onClick = {
                                    viewModel.submitOrder(page) { orderId ->
                                        onOrderSubmitted(orderId)
                                    }
                                },
                                fillWidth = true,
                                enabled = customerName.isNotBlank(),
                                icon = GenesysIcons.Check
                            )
                            
                            if (customerName.isBlank()) {
                                GenesysSpacer(GenesysSpacing.Small)
                                GenesysText(
                                    text = "Preencha seu nome para finalizar", 
                                    style = GenesysTextStyle.Label,
                                    textAlign = GenesysTextAlign.Center
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

    GenesysCard {
        GenesysRow {
            GenesysImage(
                url = displayImageUrl,
                size = GenesysDimens.IconLogo
            )

            GenesysSpacer(GenesysSpacing.Medium)

            // Usando Column do DS sem weightValue, pois ele deve ser controlado pelo container pai se necessário
            GenesysColumn(usePadding = false) {
                GenesysText(
                    text = item.product.name,
                    style = GenesysTextStyle.Body,
                    fontWeight = GenesysFontWeight.Bold
                )
                GenesysText(
                    text = "R$ ${item.product.price}",
                    style = GenesysTextStyle.Body,
                    fontWeight = GenesysFontWeight.Bold
                )
                
                GenesysSpacer(GenesysSpacing.Small)

                GenesysQuantitySelector(
                    quantity = item.quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease
                )
            }

            GenesysWeightSpacer(1f)

            GenesysIconButton(
                icon = GenesysIcons.Delete,
                onClick = onRemove
            )
        }
    }
}
