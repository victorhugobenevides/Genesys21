package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.images.GenesysImage
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysQuantitySelector
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.organisms.payment.StripePaymentElement
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.ui.util.glassmorphic
import com.itbenevides.genesys21.util.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    page: Page? = null,
    onBack: () -> Unit,
    onOrderSubmitted: (String) -> Unit = {},
) {
    val viewModel: PageViewModel = koinViewModel()
    val cartItems by viewModel.cart.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val backendUrl = remember { getBaseUrl() }

    var storeConfig by remember { mutableStateOf<Store?>(null) }
    var state by remember { mutableStateOf(CartScreenState()) }
    var showLoginDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(page?.storeId) {
        page?.storeId?.let { id ->
            viewModel.getStore(id).onSuccess { storeConfig = it }
        }
    }

    LaunchedEffect(customerName, customerPhone) {
        state = state.copy(customerName = customerName, customerPhone = customerPhone)
    }

    LaunchedEffect(cartItems, total, isLoading) {
        state = state.copy(cartItems = cartItems, total = total, isLoading = isLoading)
    }

    val onEvent: (CartScreenEvent) -> Unit = { event ->
        when (event) {
            is CartScreenEvent.OnUpdateQuantity -> viewModel.updateCartQuantity(event.productId, event.newQuantity)
            is CartScreenEvent.OnRemoveItem -> viewModel.removeFromCart(event.itemId)
            is CartScreenEvent.OnCustomerNameChanged -> viewModel.saveCustomerName(event.name)
            is CartScreenEvent.OnCustomerPhoneChanged -> viewModel.saveCustomerPhone(event.phone)
            is CartScreenEvent.OnAddressChanged -> {
                state = state.copy(shippingAddress = event.address)
                if (event.address.zipCode.length >= 8) {
                    coroutineScope.launch {
                        val storeId = page?.storeId ?: cartItems.firstOrNull()?.product?.storeId ?: "admin"
                        val options = viewModel.calculateShipping(storeId, event.address.zipCode)
                        state = state.copy(availableShippingOptions = options)
                    }
                }
            }
            is CartScreenEvent.OnShippingOptionSelected -> state = state.copy(selectedShippingOption = event.option)
            is CartScreenEvent.OnPaymentMethodChanged -> state = state.copy(paymentMethod = event.method)
            is CartScreenEvent.OnStepChanged -> state = state.copy(currentStep = event.step)
            is CartScreenEvent.OnCheckoutClicked -> {
                if (!isLoggedIn) {
                    showLoginDialog = true
                } else {
                    viewModel.submitOrder(
                        page = page,
                        paymentMethod = state.paymentMethod,
                        shippingAddress = state.shippingAddress,
                        shippingPrice = state.selectedShippingOption?.price ?: 0.0,
                        shippingMethod = state.selectedShippingOption?.name,
                    ) { response ->
                        if (response.stripeClientSecret != null) {
                            state = state.copy(
                                stripeClientSecret = response.stripeClientSecret,
                                stripePublishableKey = response.stripePublishableKey
                            )
                        } else {
                            onOrderSubmitted(response.orderId)
                        }
                    }
                }
            }
            is CartScreenEvent.OnBackClicked -> {
                if (state.stripeClientSecret != null) {
                    state = state.copy(stripeClientSecret = null)
                } else if (state.currentStep > 1) {
                    state = state.copy(currentStep = state.currentStep - 1)
                } else {
                    onBack()
                }
            }
            is CartScreenEvent.OnStripePaymentConfirmed -> onOrderSubmitted(event.orderId)
            is CartScreenEvent.OnStripePaymentError -> { }
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val stripeAppearance = remember(appTheme, colorScheme) {
        StripeThemeMapper.mapToAppearance(appTheme, colorScheme)
    }

    CartContent(
        state = state,
        store = storeConfig,
        backendUrl = backendUrl,
        stripeAppearance = stripeAppearance,
        onEvent = onEvent
    )

    if (showLoginDialog) {
        com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog(
            onDismissRequest = { showLoginDialog = false },
            title = "Acesse sua conta",
            confirmButton = {}
        ) {
            GenesysColumn(horizontalAlignment = GenesysAlignment.Center) {
                GenesysText(text = "Para finalizar sua compra com segurança, por favor identifique-se.")
                GenesysSpacer(GenesysTheme.spacing.l)

                com.itbenevides.genesys21.presentation.components.auth.GoogleSignInButton(
                    modifier = Modifier.fillMaxWidth(),
                    onTokenReceived = { idToken, accessToken ->
                        viewModel.signInWithToken(
                            idToken = idToken,
                            accessToken = accessToken,
                            provider = "google",
                            onSuccess = {
                                showLoginDialog = false
                                onEvent(CartScreenEvent.OnCheckoutClicked)
                            },
                            onError = { }
                        )
                    },
                    onError = { }
                )
                GenesysSpacer(GenesysTheme.spacing.m)
                GenesysTextButton(text = "Entrar com e-mail", onClick = { })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    state: CartScreenState,
    store: Store?,
    backendUrl: String,
    stripeAppearance: String,
    onEvent: (CartScreenEvent) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == GenesysWindowSizeClass.EXPANDED

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = if (state.stripeClientSecret != null) "Pagamento Seguro" else when(state.currentStep) {
                    1 -> GenesysStrings.CartTitle
                    2 -> "Entrega"
                    else -> "Revisão"
                },
                onBack = { onEvent(CartScreenEvent.OnBackClicked) },
            )
        },
    ) {
        if (state.cartItems.isEmpty() && !state.isLoading) {
            GenesysEmptyState(
                icon = GenesysIcons.ShoppingBag,
                title = GenesysStrings.EmptyCartTitle,
                description = GenesysStrings.EmptyCartDescription,
                action = {
                    GenesysLoadingButton(
                        text = GenesysStrings.Back,
                        onClick = { onEvent(CartScreenEvent.OnBackClicked) },
                    )
                },
            )
        } else {
            GenesysColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = GenesysAlignment.Center,
                usePadding = false,
            ) {
                GenesysWeightBox(1f) {
                    GenesysColumn(usePadding = true, useScroll = true, maxWidth = if(isExpanded) 800.dp else null) {
                        if (state.stripeClientSecret == null) {
                            GenesysSpacer(GenesysTheme.spacing.m)
                            CartStepperUI(step = state.currentStep)
                            GenesysSpacer(GenesysTheme.spacing.l)
                        }

                        when {
                            state.stripeClientSecret != null -> {
                                StripePaymentElement(
                                    clientSecret = state.stripeClientSecret,
                                    publishableKey = state.stripePublishableKey ?: "",
                                    appearanceJson = stripeAppearance,
                                    onPaymentConfirmed = { },
                                    onPaymentError = { onEvent(CartScreenEvent.OnStripePaymentError(it)) }
                                )
                            }
                            state.currentStep == 1 -> {
                                state.cartItems.forEach { item ->
                                    ModernCartItemRow(item, backendUrl, onEvent)
                                    GenesysSpacer(GenesysTheme.spacing.s)
                                }
                            }
                            state.currentStep == 2 -> {
                                IdentificationCard(state, onEvent)
                                if (state.needsShipping) {
                                    GenesysSpacer(GenesysTheme.spacing.l)
                                    DeliveryMethodSelector(state, store, onEvent)
                                    if (state.selectedShippingOption?.id != "pickup" && state.selectedShippingOption != null) {
                                        GenesysSpacer(GenesysTheme.spacing.l)
                                        AddressFormCard(state, onEvent)
                                        if (state.availableShippingOptions.isNotEmpty()) {
                                            GenesysSpacer(GenesysTheme.spacing.l)
                                            ShippingOptionsCard(state, onEvent)
                                        }
                                    }
                                }
                            }
                            state.currentStep == 3 -> {
                                PaymentMethodCard(state, store, onEvent)
                                GenesysSpacer(GenesysTheme.spacing.l)
                                OrderSummaryCard(state)
                            }
                        }
                        GenesysSpacer(GenesysTheme.spacing.xl)
                    }
                }

                if (state.stripeClientSecret == null) {
                    GenesysCard(
                        elevation = GenesysDimens.ElevationHigh,
                        modifier = Modifier.padding(16.dp).widthIn(max = 800.dp),
                    ) {
                        CartFooter(state, onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartFooter(
    state: CartScreenState,
    onEvent: (CartScreenEvent) -> Unit,
) {
    GenesysColumn(usePadding = false) {
        GenesysRow {
            GenesysWeightBox(1f) {
                GenesysText(text = if (state.currentStep < 3) "Subtotal" else "Total Geral", style = GenesysTextStyle.Body)
            }
            val displaysubtotal = if (state.currentStep < 3) state.total else state.grandTotal
            val totalFormatted = (displaysubtotal * 100.0).roundToLong() / 100.0
            GenesysText(
                text = "${GenesysStrings.PricePrefix}$totalFormatted",
                style = GenesysTextStyle.Headline,
                fontWeight = GenesysFontWeight.ExtraBold,
                color = GenesysTheme.colors.brand,
            )
        }
        GenesysSpacer(GenesysTheme.spacing.l)

        val buttonText = when(state.currentStep) {
            1 -> "Continuar para Entrega"
            2 -> "Continuar para Pagamento"
            else -> if (state.paymentMethod == PaymentMethod.APP) "Pagar Agora" else GenesysStrings.CheckoutButton
        }

        val isNextEnabled = when(state.currentStep) {
            1 -> state.cartItems.isNotEmpty()
            2 -> {
                val isPickup = state.selectedShippingOption?.id == "pickup"
                val hasIdentity = state.customerName.isNotBlank() && state.customerPhone.length >= 8
                val hasShippingOption = state.selectedShippingOption != null

                if (state.needsShipping) {
                    if (isPickup) hasIdentity && hasShippingOption
                    else hasIdentity && hasShippingOption && state.shippingAddress != null
                } else hasIdentity
            }
            else -> state.isCheckoutEnabled
        }

        GenesysLoadingButton(
            text = buttonText,
            onClick = {
                if (state.currentStep < 3) onEvent(CartScreenEvent.OnStepChanged(state.currentStep + 1))
                else onEvent(CartScreenEvent.OnCheckoutClicked)
            },
            fillWidth = true,
            enabled = isNextEnabled,
            icon = if (state.currentStep == 3) GenesysIcons.Check else GenesysIcons.ArrowRight,
            isLoading = state.isLoading,
        )
    }
}

@Composable
private fun DeliveryMethodSelector(
    state: CartScreenState,
    store: Store?,
    onEvent: (CartScreenEvent) -> Unit
) {
    val allowPickup = store?.allowPickup ?: true
    val allowDelivery = store?.allowDelivery ?: true

    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(text = "Como deseja receber seu pedido?", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val isPickup = state.selectedShippingOption?.id == "pickup"
                val isDelivery = state.selectedShippingOption != null && !isPickup

                if (allowPickup) {
                    Surface(
                        onClick = { onEvent(CartScreenEvent.OnShippingOptionSelected(ShippingOption("pickup", "Retirar no Local", 0.0, 0))) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isPickup) GenesysTheme.colors.brandContainer else GenesysTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                        border = if (isPickup) androidx.compose.foundation.BorderStroke(2.dp, GenesysTheme.colors.brand) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(GenesysIcons.ShoppingBag, null, tint = if(isPickup) GenesysTheme.colors.brand else GenesysTheme.colors.onSurfaceVariant)
                            GenesysSpacer(GenesysTheme.spacing.s)
                            GenesysText(text = "Retirar no Local", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
                            GenesysText(text = "Grátis", style = GenesysTextStyle.Label, color = GenesysTheme.colors.brand)
                        }
                    }
                }

                if (allowDelivery) {
                    Surface(
                        onClick = { onEvent(CartScreenEvent.OnShippingOptionSelected(ShippingOption("pending_delivery", "Receber em Casa", 0.0, 0))) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDelivery) GenesysTheme.colors.brandContainer else GenesysTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                        border = if (isDelivery) androidx.compose.foundation.BorderStroke(2.dp, GenesysTheme.colors.brand) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(GenesysIcons.Language, null, tint = if(isDelivery) GenesysTheme.colors.brand else GenesysTheme.colors.onSurfaceVariant)
                            GenesysSpacer(GenesysTheme.spacing.s)
                            GenesysText(text = "Receber em Casa", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
                            GenesysText(text = "Cálculo via CEP", style = GenesysTextStyle.Label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressFormCard(state: CartScreenState, onEvent: (CartScreenEvent) -> Unit) {
    val address = state.shippingAddress ?: Address(street = "", number = "", neighborhood = "", city = "", state = "", zipCode = "")

    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(text = "Endereço de Entrega", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            GenesysTextField(
                value = address.zipCode,
                onValueChange = { onEvent(CartScreenEvent.OnAddressChanged(address.copy(zipCode = it))) },
                label = "CEP",
                placeholder = "00000-000",
                icon = GenesysIcons.Search,
            )

            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysTextField(value = address.street, onValueChange = { onEvent(CartScreenEvent.OnAddressChanged(address.copy(street = it))) }, label = "Logradouro")
            GenesysSpacer(GenesysTheme.spacing.m)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) { GenesysTextField(value = address.number, onValueChange = { onEvent(CartScreenEvent.OnAddressChanged(address.copy(number = it))) }, label = "Número") }
                Box(modifier = Modifier.weight(1.5f)) { GenesysTextField(value = address.neighborhood, onValueChange = { onEvent(CartScreenEvent.OnAddressChanged(address.copy(neighborhood = it))) }, label = "Bairro") }
            }

            GenesysSpacer(GenesysTheme.spacing.m)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(2f)) { GenesysTextField(value = address.city, onValueChange = { onEvent(CartScreenEvent.OnAddressChanged(address.copy(city = it))) }, label = "Cidade") }
                Box(modifier = Modifier.weight(1f)) { GenesysTextField(value = address.state, onValueChange = { onEvent(CartScreenEvent.OnAddressChanged(address.copy(state = it))) }, label = "UF") }
            }
        }
    }
}

@Composable
private fun ShippingOptionsCard(state: CartScreenState, onEvent: (CartScreenEvent) -> Unit) {
    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(text = "Opções de Frete", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            state.availableShippingOptions.forEach { option ->
                val isSelected = state.selectedShippingOption?.id == option.id
                Surface(
                    onClick = { onEvent(CartScreenEvent.OnShippingOptionSelected(option)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) GenesysTheme.colors.brandContainer else GenesysTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GenesysTheme.colors.brand) else null
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isSelected, onClick = { onEvent(CartScreenEvent.OnShippingOptionSelected(option)) })
                        GenesysSpacer(GenesysTheme.spacing.s)
                        Column(modifier = Modifier.weight(1f)) {
                            GenesysText(text = option.name, fontWeight = GenesysFontWeight.Bold)
                            GenesysText(text = "Entrega em até ${option.estimatedDays} dias úteis", style = GenesysTextStyle.Label)
                        }
                        GenesysText(text = "${GenesysStrings.PricePrefix}${option.price}", fontWeight = GenesysFontWeight.ExtraBold, color = GenesysTheme.colors.brand)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(state: CartScreenState) {
    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(text = "Resumo do Pedido", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            SummaryRow("Subtotal", state.total)
            if (state.needsShipping && state.selectedShippingOption != null) {
                SummaryRow("Frete (${state.selectedShippingOption.name})", state.selectedShippingOption.price)
            }
            val totalTravelFees = state.cartItems.sumOf { it.appointment?.travelFee ?: 0.0 }
            if (totalTravelFees > 0) SummaryRow("Taxa de Deslocamento", totalTravelFees)

            GenesysSpacer(GenesysTheme.spacing.s)
            GenesysDivider()
            GenesysSpacer(GenesysTheme.spacing.s)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GenesysText(text = "Total Geral", fontWeight = GenesysFontWeight.ExtraBold, style = GenesysTextStyle.Title)
                val totalFormatted = (state.grandTotal * 100.0).roundToLong() / 100.0
                GenesysText(text = "${GenesysStrings.PricePrefix}$totalFormatted", fontWeight = GenesysFontWeight.ExtraBold, style = GenesysTextStyle.Title, color = GenesysTheme.colors.brand)
            }
        }
    }
}

@Composable
private fun IdentificationCard(state: CartScreenState, onEvent: (CartScreenEvent) -> Unit) {
     GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(text = GenesysStrings.Identification, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysTextField(value = state.customerName, onValueChange = { onEvent(CartScreenEvent.OnCustomerNameChanged(it)) }, label = GenesysStrings.CustomerNameLabel, placeholder = GenesysStrings.CheckoutNameHint, icon = GenesysIcons.Person)
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysTextField(value = state.customerPhone, onValueChange = { onEvent(CartScreenEvent.OnCustomerPhoneChanged(it)) }, label = "Seu WhatsApp / Telefone", placeholder = "(00) 00000-0000", icon = GenesysIcons.Chat)
        }
    }
}

@Composable
private fun PaymentMethodCard(
    state: CartScreenState,
    store: Store?,
    onEvent: (CartScreenEvent) -> Unit,
) {
    val allowLocal = store?.allowPayOnLocation ?: true
    val allowApp = store?.allowPayInApp ?: true

    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(text = "Forma de Pagamento", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (allowLocal) {
                    GenesysFilterChip(selected = state.paymentMethod == PaymentMethod.LOCAL, onClick = { onEvent(CartScreenEvent.OnPaymentMethodChanged(PaymentMethod.LOCAL)) }, label = "Pagar no Local", modifier = Modifier.weight(1f))
                }
                if (allowApp) {
                    GenesysFilterChip(selected = state.paymentMethod == PaymentMethod.APP, onClick = { onEvent(CartScreenEvent.OnPaymentMethodChanged(PaymentMethod.APP)) }, label = "Pagar pelo App", modifier = Modifier.weight(1f))
                }
            }
            val infoText = if (state.paymentMethod == PaymentMethod.LOCAL) "Você pagará diretamente no estabelecimento ao ser atendido ou retirar os produtos."
            else "O pagamento será processado agora via cartão ou Pix dentro do aplicativo."

            GenesysSpacer(GenesysTheme.spacing.s)
            GenesysText(text = infoText, style = GenesysTextStyle.Label, color = GenesysTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        GenesysText(text = label, style = GenesysTextStyle.Body)
        val formatted = (value * 100.0).roundToLong() / 100.0
        GenesysText(text = "${GenesysStrings.PricePrefix}$formatted", fontWeight = GenesysFontWeight.Bold)
    }
}

@Composable
private fun CartStepperUI(step: Int) {
    GenesysRow(horizontalArrangement = Arrangement.Center) {
        repeat(3) { index ->
            val currentStep = index + 1
            val active = currentStep <= step
            val color by animateColorAsState(targetValue = if (active) GenesysTheme.colors.brand else GenesysTheme.colors.outline, animationSpec = GenesysMotion.colorSpring, label = "stepperColor")
            val size by animateDpAsState(targetValue = if (currentStep == step) 12.dp else 8.dp, animationSpec = spring(dampingRatio = 0.7f), label = "stepperSize")
            Box(modifier = Modifier.size(size).background(color, CircleShape))
            if (index < 2) {
                Box(modifier = Modifier.width(24.dp).height(2.dp).background(GenesysTheme.colors.outline).align(Alignment.CenterVertically))
            }
        }
    }
}

@Composable
private fun ModernCartItemRow(item: CartItem, backendUrl: String, onEvent: (CartScreenEvent) -> Unit) {
    val displayImageUrl = remember(item.product?.imageUrls, item.service?.imageUrls) {
        val first = item.product?.imageUrls?.firstOrNull() ?: item.service?.imageUrls?.firstOrNull() ?: ""
        if (first.startsWith("/") && !first.startsWith("http")) "$backendUrl$first" else first
    }

    GenesysCard(
        elevation = GenesysDimens.ElevationLow,
        modifier = Modifier.animateContentSize().semantics(mergeDescendants = true) {
            contentDescription = if (item.product != null) "Item no carrinho: ${item.name}, Quantidade: ${item.quantity}" else "Serviço no carrinho: ${item.name}"
        },
    ) {
        GenesysRow(verticalAlignment = Alignment.Top) {
            GenesysImage(url = displayImageUrl, size = 70.dp)
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = item.name, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                    item.appointment?.let { appt ->
                        val time = appt.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                        GenesysText(text = "Agendado: ${time.dayOfMonth}/${time.monthNumber} às ${time.hour}:${time.minute.toString().padStart(2, '0')}", style = GenesysTextStyle.Label, color = GenesysTheme.colors.accent)
                    }
                    val priceFormatted = (item.price * 100.0).roundToLong() / 100.0
                    GenesysText(text = "${GenesysStrings.PricePrefix}$priceFormatted", style = GenesysTextStyle.Body, color = GenesysTheme.colors.brand)
                    val prod = item.product
                    if (prod != null) {
                        GenesysSpacer(GenesysTheme.spacing.s)
                        GenesysQuantitySelector(
                            quantity = item.quantity,
                            onIncrease = { onEvent(CartScreenEvent.OnUpdateQuantity(prod.id, item.quantity + 1)) },
                            onDecrease = { onEvent(CartScreenEvent.OnUpdateQuantity(prod.id, item.quantity - 1)) },
                        )
                    }
                }
            }
            val itemId = item.product?.id ?: item.service?.id ?: ""
            GenesysIconButton(icon = GenesysIcons.Delete, onClick = { onEvent(CartScreenEvent.OnRemoveItem(itemId)) }, tint = GenesysTheme.colors.error.copy(alpha = 0.6f))
        }
    }
}
