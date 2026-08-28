package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
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
fun PaymentsTab(
    viewModel: PageViewModel,
    userProfile: UserProfile?,
    uriHandler: UriHandler,
    scope: CoroutineScope
) {
    val storeId = userProfile?.id ?: "admin"
    var store by remember { mutableStateOf<Store?>(null) }

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
            stripePublic = s.stripePublicKey ?: ""
            stripeSecret = s.stripeSecretKey ?: ""
            asaasKey = s.asaasApiKey ?: ""
            selectedGateway = s.paymentGateway
        }
    }

    GenesysColumn(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), usePadding = true) {
        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysText(text = "Pagamentos e Gateways", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(text = "Gerencie como você recebe pelas suas vendas e serviços.", style = GenesysTextStyle.Body, color = GenesysTheme.colors.onSurfaceVariant)

        GenesysSpacer(GenesysTheme.spacing.l)

        GenesysCard {
            GenesysColumn(usePadding = false) {
                GenesysText(
                    text = "Stripe Connect",
                    style = GenesysTextStyle.Title,
                    fontWeight = GenesysFontWeight.Bold
                )
                GenesysText(
                    text = "Receba pagamentos diretamente em sua conta bancária via Checkout Seguro.",
                    style = GenesysTextStyle.Label,
                    color = GenesysTheme.colors.onSurfaceVariant
                )

                GenesysSpacer(GenesysTheme.spacing.m)

                if (store?.stripeAccountId.isNullOrBlank()) {
                    GenesysLoadingButton(
                        text = "Configurar Conta Stripe",
                        icon = GenesysIcons.Payments,
                        onClick = {
                            val userEmail = userProfile?.email ?: ""
                            viewModel.connectStripe(storeId, userEmail) { url ->
                                uriHandler.openUri(url)
                            }
                        },
                        isLoading = isLoading,
                        fillWidth = true
                    )
                } else {
                    StripeConnectedUI(store, viewModel, storeId, isLoading, scope) { secret, component ->
                        connectSessionSecret = secret
                        activeConnectComponent = component
                    }
                }

                if (connectSessionSecret != null) {
                    GenesysSpacer(GenesysTheme.spacing.l)
                    StripeConnectComponent(
                        componentName = if (store?.stripeAccountId.isNullOrBlank()) "account-onboarding" else activeConnectComponent,
                        publishableKey = stripePublic.ifBlank { "pk_test_placeholder" },
                        clientSecret = connectSessionSecret!!,
                        modifier = Modifier.fillMaxWidth().height(600.dp).background(Color.White, RoundedCornerShape(8.dp))
                    )

                    GenesysSpacer(GenesysTheme.spacing.s)
                    GenesysTextButton(
                        text = "Fechar Gestão Stripe",
                        onClick = { connectSessionSecret = null },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        GenesysSpacer(GenesysTheme.spacing.l)

        // Gateway Manual (Legacy ou Pro)
        GenesysCard {
            GenesysColumn(usePadding = false) {
                GenesysText(text = "Configuração Manual de Gateway", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysTheme.spacing.m)

                GenesysText(text = "Use esta seção apenas se tiver chaves de API próprias.", style = GenesysTextStyle.Label)
                GenesysSpacer(GenesysTheme.spacing.m)

                GenesysTextField(value = asaasKey, onValueChange = { asaasKey = it }, label = "Asaas API Key (Opcional)")
                GenesysSpacer(GenesysTheme.spacing.m)

                GenesysLoadingButton(
                    text = "Salvar Gateways",
                    onClick = {
                        store?.let {
                            viewModel.saveStore(it.copy(asaasApiKey = asaasKey)) { }
                        }
                    },
                    isLoading = isLoading,
                    fillWidth = true
                )
            }
        }

        GenesysSpacer(GenesysTheme.spacing.huge)
    }
}

@Composable
private fun StripeConnectedUI(
    store: Store?,
    viewModel: PageViewModel,
    storeId: String,
    isLoading: Boolean,
    scope: CoroutineScope,
    onShowComponent: (String, String) -> Unit
) {
    Column {
        GenesysRow(
            modifier = Modifier.fillMaxWidth().background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(GenesysTheme.spacing.s)
            ).padding(GenesysTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            usePadding = false
        ) {
            Icon(GenesysIcons.Check, null, tint = Color(0xFF34C759), modifier = Modifier.size(20.dp))
            GenesysSpacer(GenesysTheme.spacing.xs)
            GenesysColumn(usePadding = false) {
                GenesysText(text = "Stripe Conectado", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                GenesysText(text = "ID: ${store?.stripeAccountId}", style = GenesysTextStyle.Label)
            }
        }

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), usePadding = false) {
            Box(modifier = Modifier.weight(1f)) {
                GenesysLoadingButton(
                    text = "Vendas",
                    onClick = {
                        scope.launch {
                            viewModel.getAccountSession(storeId).onSuccess { onShowComponent(it, "payments") }
                        }
                    },
                    isLoading = isLoading
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                GenesysLoadingButton(
                    text = "Saques",
                    onClick = {
                        scope.launch {
                            viewModel.getAccountSession(storeId).onSuccess { onShowComponent(it, "payouts") }
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}
