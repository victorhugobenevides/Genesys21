package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.ToggleOptionRow
import com.itbenevides.genesys21.ui.components.StripeConnectComponent
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun StoreSettingsTab(
    viewModel: PageViewModel,
    userProfile: UserProfile?,
    uriHandler: UriHandler,
    scope: CoroutineScope
) {
    val storeId = userProfile?.id ?: "admin"
    var store by remember { mutableStateOf<Store?>(null) }

    var originZip by remember { mutableStateOf("") }
    var originStreet by remember { mutableStateOf("") }
    var originNumber by remember { mutableStateOf("") }
    var originNeighborhood by remember { mutableStateOf("") }
    var originCity by remember { mutableStateOf("") }
    var originState by remember { mutableStateOf("") }

    var allowPayLocal by remember { mutableStateOf(true) }
    var allowPayApp by remember { mutableStateOf(true) }
    var allowPickup by remember { mutableStateOf(true) }
    var allowDelivery by remember { mutableStateOf(true) }

    var stripePublic by remember { mutableStateOf("") }
    var stripeSecret by remember { mutableStateOf("") }
    var asaasKey by remember { mutableStateOf("") }
    var selectedGateway by remember { mutableStateOf("STRIPE") }

    var connectSessionSecret by remember { mutableStateOf<String?>(null) }
    var activeConnectComponent by remember { mutableStateOf("account-onboarding") }

    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(storeId) {
        viewModel.getStore(storeId).onSuccess { s ->
            store = s
            originZip = s.originZipCode ?: ""
            originStreet = s.originStreet ?: ""
            originNumber = s.originNumber ?: ""
            originNeighborhood = s.originNeighborhood ?: ""
            originCity = s.originCity ?: ""
            originState = s.originState ?: ""
            allowPayLocal = s.allowPayOnLocation
            allowPayApp = s.allowPayInApp
            allowPickup = s.allowPickup
            allowDelivery = s.allowDelivery
            stripePublic = s.stripePublicKey ?: ""
            stripeSecret = s.stripeSecretKey ?: ""
            asaasKey = s.asaasApiKey ?: ""
            selectedGateway = s.paymentGateway
        }
    }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysText(text = "Configurações da Loja", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(text = "Configure os dados de remetente e as opções do checkout.", style = GenesysTextStyle.Body, color = GenesysTheme.colors.onSurfaceVariant)

        GenesysSpacer(GenesysTheme.spacing.l)

        GenesysCard {
            GenesysColumn(usePadding = false) {
                GenesysText(text = "Dados do Remetente (Frete)", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysTheme.spacing.m)

                GenesysTextField(value = originZip, onValueChange = { originZip = it }, label = "CEP de Origem", icon = GenesysIcons.Search)
                GenesysSpacer(GenesysTheme.spacing.m)
                GenesysTextField(value = originStreet, onValueChange = { originStreet = it }, label = "Rua/Logradouro")
                GenesysSpacer(GenesysTheme.spacing.m)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { GenesysTextField(value = originNumber, onValueChange = { originNumber = it }, label = "Número") }
                    Box(Modifier.weight(2f)) { GenesysTextField(value = originNeighborhood, onValueChange = { originNeighborhood = it }, label = "Bairro") }
                }
                GenesysSpacer(GenesysTheme.spacing.m)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(2f)) { GenesysTextField(value = originCity, onValueChange = { originCity = it }, label = "Cidade") }
                    Box(Modifier.weight(1f)) { GenesysTextField(value = originState, onValueChange = { originState = it }, label = "UF") }
                }
            }
        }

        GenesysSpacer(GenesysTheme.spacing.l)

        GenesysCard {
            GenesysColumn(usePadding = false) {
                GenesysText(text = "Opções de Pagamento e Entrega", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysTheme.spacing.m)

                ToggleOptionRow("Permitir Pagar no Local", allowPayLocal) { allowPayLocal = it }
                ToggleOptionRow("Permitir Pagar pelo App", allowPayApp) { allowPayApp = it }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ToggleOptionRow("Permitir Retirada no Local", allowPickup) { allowPickup = it }
                ToggleOptionRow("Permitir Envio / Entrega", allowDelivery) { allowDelivery = it }
            }
        }

        GenesysSpacer(GenesysTheme.spacing.l)

    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(
                text = "Pagamentos (Stripe Connect)",
                style = GenesysTextStyle.Title,
                fontWeight = GenesysFontWeight.Bold
            )
            GenesysText(
                text = "Receba pagamentos diretamente em sua conta bancária.",
                style = GenesysTextStyle.Label,
                color = GenesysTheme.colors.onSurfaceVariant
            )

            GenesysSpacer(GenesysTheme.spacing.m)

            if (store?.stripeAccountId.isNullOrBlank()) {
                GenesysLoadingButton(
                    text = "Conectar com Stripe",
                    icon = GenesysIcons.Payments,
                    onClick = {
                        val userEmail = userProfile?.email ?: ""
                        viewModel.connectStripe(storeId, userEmail) { _ ->
                            scope.launch {
                                viewModel.getAccountSession(storeId).onSuccess { secret ->
                                    connectSessionSecret = secret
                                }
                            }
                        }
                    },
                    isLoading = isLoading,
                    fillWidth = true
                )
            } else {
                GenesysRow(
                    modifier = Modifier.fillMaxWidth().background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(GenesysTheme.spacing.s)
                    ).padding(GenesysTheme.spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                    usePadding = false
                ) {
                    Icon(
                        GenesysIcons.Check,
                        null,
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(20.dp)
                    )
                    GenesysSpacer(GenesysTheme.spacing.xs)
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = "Stripe Conectado",
                            style = GenesysTextStyle.Body,
                            fontWeight = GenesysFontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        GenesysText(
                            text = "ID: ${store?.stripeAccountId}",
                            style = GenesysTextStyle.Label,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                GenesysSpacer(GenesysTheme.spacing.m)

                GenesysRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), usePadding = false) {
                    Box(modifier = Modifier.weight(1f)) {
                        GenesysLoadingButton(
                            text = "Vendas",
                            icon = GenesysIcons.Payments,
                            onClick = {
                                activeConnectComponent = "payments"
                                scope.launch {
                                    viewModel.getAccountSession(storeId).onSuccess { secret ->
                                        connectSessionSecret = secret
                                    }
                                }
                            },
                            isLoading = isLoading
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        GenesysLoadingButton(
                            text = "Saques",
                            icon = GenesysIcons.AccountBalanceWallet,
                            onClick = {
                                activeConnectComponent = "payouts"
                                scope.launch {
                                    viewModel.getAccountSession(storeId).onSuccess { secret ->
                                        connectSessionSecret = secret
                                    }
                                }
                            },
                            isLoading = isLoading
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        GenesysLoadingButton(
                            text = "Painel",
                            icon = GenesysIcons.Language,
                            containerColor = MaterialTheme.colorScheme.secondary,
                            onClick = {
                                viewModel.openStripeDashboard(storeId) { url ->
                                    uriHandler.openUri(url)
                                }
                            },
                            isLoading = isLoading
                        )
                    }
                }
            }

            if (connectSessionSecret != null) {
                GenesysSpacer(GenesysTheme.spacing.l)
                StripeConnectComponent(
                    componentName = if (store?.stripeAccountId.isNullOrBlank()) "account-onboarding" else activeConnectComponent,
                    publishableKey = stripePublic.ifBlank { "pk_test_placeholder" },
                    clientSecret = connectSessionSecret!!,
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp))
                )

                GenesysSpacer(GenesysTheme.spacing.s)
                GenesysTextButton(
                    text = "Fechar Gestão Stripe",
                    onClick = { connectSessionSecret = null },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            if (selectedGateway == "ASAAS") {
                GenesysSpacer(GenesysTheme.spacing.l)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                GenesysSpacer(GenesysTheme.spacing.m)
                GenesysTextField(
                    value = asaasKey,
                    onValueChange = { asaasKey = it },
                    label = "Asaas API Key",
                    placeholder = "$"
                )
            }
        }
    }

        GenesysSpacer(GenesysTheme.spacing.huge)

        GenesysLoadingButton(
            text = "Salvar Configurações",
            onClick = {
                val currentStore = store ?: Store(
                    id = storeId,
                    ownerId = "",
                    name = "Minha Loja"
                )
                val updated = currentStore.copy(
                    originZipCode = originZip,
                    originStreet = originStreet,
                    originNumber = originNumber,
                    originNeighborhood = originNeighborhood,
                    originCity = originCity,
                    originState = originState,
                    allowPayOnLocation = allowPayLocal,
                    allowPayInApp = allowPayApp,
                    allowPickup = allowPickup,
                    allowDelivery = allowDelivery,
                    stripePublicKey = if (selectedGateway == "STRIPE") stripePublic else null,
                    stripeSecretKey = if (selectedGateway == "STRIPE") stripeSecret else null,
                    stripeAccountId = currentStore.stripeAccountId,
                    asaasApiKey = asaasKey,
                    paymentGateway = selectedGateway
                )
                viewModel.saveStore(updated) { }
            },
            fillWidth = true,
            isLoading = isLoading
        )
    }
}
