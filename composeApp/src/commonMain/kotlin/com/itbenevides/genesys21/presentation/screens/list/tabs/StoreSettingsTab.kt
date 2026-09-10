package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.itbenevides.genesys21.presentation.screens.list.components.ToggleOptionRow
import com.itbenevides.genesys21.ui.components.StripeConnectComponent
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Tab de Configurações da Loja.
 */
@Composable
fun StoreSettingsTab(
    viewModel: PageViewModel,
    userProfile: UserProfile?,
    uriHandler: UriHandler,
    scope: CoroutineScope
) {
    val storeId = userProfile?.id ?: "admin"
    var store by remember { mutableStateOf<Store?>(null) }
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

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

    val horizontalPadding = if (isCompact) GenesysTheme.spacing.m else GenesysTheme.spacing.l

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            com.itbenevides.genesys21.presentation.screens.list.components.AdminTabHeader(
                title = "Configurações da Loja",
                subtitle = "Dados de remetente e opções do checkout."
            )
        }

        // Seção: Endereço
        item {
            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                GenesysCard {
                    Column {
                        GenesysText(text = "Dados do Remetente (Frete)", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold, isSelectable = true)
                        GenesysSpacer(GenesysTheme.spacing.m)

                        GenesysTextField(
                            value = originZip,
                            onValueChange = { originZip = it },
                            label = "CEP de Origem",
                            icon = GenesysIcons.Search
                        )
                        GenesysSpacer(GenesysTheme.spacing.m)
                        GenesysTextField(
                            value = originStreet,
                            onValueChange = { originStreet = it },
                            label = "Rua/Logradouro"
                        )
                        GenesysSpacer(GenesysTheme.spacing.m)

                        if (isCompact) {
                            GenesysTextField(value = originNumber, onValueChange = { originNumber = it }, label = "Número")
                            GenesysSpacer(GenesysTheme.spacing.m)
                            GenesysTextField(value = originNeighborhood, onValueChange = { originNeighborhood = it }, label = "Bairro")
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.m)) {
                                Box(Modifier.weight(1f)) {
                                    GenesysTextField(value = originNumber, onValueChange = { originNumber = it }, label = "Número")
                                }
                                Box(Modifier.weight(2f)) {
                                    GenesysTextField(value = originNeighborhood, onValueChange = { originNeighborhood = it }, label = "Bairro")
                                }
                            }
                        }

                        GenesysSpacer(GenesysTheme.spacing.m)

                        if (isCompact) {
                            GenesysTextField(value = originCity, onValueChange = { originCity = it }, label = "Cidade")
                            GenesysSpacer(GenesysTheme.spacing.m)
                            GenesysTextField(value = originState, onValueChange = { originState = it }, label = "UF")
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.m)) {
                                Box(Modifier.weight(2f)) {
                                    GenesysTextField(value = originCity, onValueChange = { originCity = it }, label = "Cidade")
                                }
                                Box(Modifier.weight(1f)) {
                                    GenesysTextField(value = originState, onValueChange = { originState = it }, label = "UF")
                                }
                            }
                        }
                    }
                }
            }
        }

        item { GenesysSpacer(GenesysTheme.spacing.l) }

        // Seção: Pagamento e Entrega
        item {
            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                GenesysCard {
                    Column {
                        GenesysText(text = "Opções de Pagamento e Entrega", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                        GenesysSpacer(GenesysTheme.spacing.m)

                        ToggleOptionRow("Permitir Pagar no Local", allowPayLocal) { allowPayLocal = it }
                        ToggleOptionRow("Permitir Pagar pelo App", allowPayApp) { allowPayApp = it }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = GenesysTheme.spacing.m),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )

                        ToggleOptionRow("Permitir Retirada no Local", allowPickup) { allowPickup = it }
                        ToggleOptionRow("Permitir Envio / Entrega", allowDelivery) { allowDelivery = it }
                    }
                }
            }
        }

        item { GenesysSpacer(GenesysTheme.spacing.xl) }

        // Botão de Ação Principal
        item {
            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                GenesysLoadingButton(
                    text = "Salvar Configurações",
                    onClick = {
                        val currentStore = store ?: Store(id = storeId, ownerId = "", name = "Minha Loja")
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

        item { GenesysSpacer(GenesysTheme.spacing.huge) }
    }
}
