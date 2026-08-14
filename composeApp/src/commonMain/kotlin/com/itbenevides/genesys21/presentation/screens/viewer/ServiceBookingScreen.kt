package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Appointment
import com.itbenevides.genesys21.domain.model.BookingNote
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.images.GenesysImage
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysAlignment
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysDivider
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.calendar.GenesysBookingEngine
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceBookingScreen(
    service: BookingService,
    page: Page,
    router: Router,
    viewModel: PageViewModel,
    today: LocalDate? = null,
) {
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    var customerNotes by remember { mutableStateOf("") }

    // Para Atendimento a Domicílio
    var selectedAddress by remember { mutableStateOf<com.itbenevides.genesys21.domain.model.Address?>(null) }
    var travelFee by remember { mutableStateOf(0.0) }

    val coroutineScope = rememberCoroutineScope()
    var availableSlotsForDate by remember { mutableStateOf<List<String>>(emptyList()) }
    var slotLoading by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    val currentToday = remember { today ?: kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

    // Logic to load available slots when date changes
    val updateAvailableSlots = { date: LocalDate ->
        coroutineScope.launch {
            slotLoading = true
            availableSlotsForDate = viewModel.getAvailableSlots(page.storeId ?: "admin", service, date)
            slotLoading = false
        }
    }

    // Load initial slots
    LaunchedEffect(Unit) {
        updateAvailableSlots(currentToday)
    }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = "Agendamento",
                onBack = { router.goBack() },
            )
        },
    ) {
         GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false,
        ) {
            GenesysColumn(
                maxWidth = 600.dp,
                usePadding = true,
                useScroll = true,
                weightValue = 1f,
            ) {
                // Service Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    service.imageUrls.firstOrNull()?.let { url ->
                        GenesysImage(url = url, size = 80.dp)
                        GenesysSpacer(GenesysSpacing.Medium)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GenesysText(text = service.name, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                            if (service.isOnline) {
                                GenesysSpacer(GenesysSpacing.Small)
                                com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysBadge(
                                    label = "ONLINE",
                                    color = GenesysTheme.colors.accent
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GenesysText(text = "${service.durationMinutes} minutos", style = GenesysTextStyle.Label)
                            if (service.maxParticipants > 1) {
                                GenesysText(text = " • Grupo até ${service.maxParticipants} pessoas", style = GenesysTextStyle.Label)
                            } else {
                                GenesysText(text = " • Individual", style = GenesysTextStyle.Label)
                            }
                        }

                        GenesysText(
                            text = "${GenesysStrings.PricePrefix}${service.price}",
                            style = GenesysTextStyle.Body,
                            fontWeight = GenesysFontWeight.Bold,
                            color = GenesysTheme.colors.brand,
                        )
                    }
                }

                GenesysSpacer(GenesysSpacing.Large)
                GenesysDivider()
                GenesysSpacer(GenesysSpacing.Large)

                // User Info
                GenesysText(text = "Seus Dados", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysSpacing.Small)
                GenesysRow {
                    GenesysWeightBox(1f) {
                        com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField(
                            value = customerName,
                            onValueChange = { viewModel.saveCustomerName(it) },
                            label = "Nome",
                            placeholder = "Seu nome completo",
                        )
                    }
                    GenesysSpacer(GenesysSpacing.Small)
                    GenesysWeightBox(1f) {
                        com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField(
                            value = customerPhone,
                            onValueChange = { viewModel.saveCustomerPhone(it) },
                            label = "WhatsApp",
                            placeholder = "(00) 00000-0000",
                        )
                    }
                }

                GenesysSpacer(GenesysSpacing.Medium)

                com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField(
                    value = customerNotes,
                    onValueChange = { customerNotes = it },
                    label = "Algum comentário ou pedido especial?",
                    placeholder = "Ex: Gostaria de lavar o cabelo também...",
                    minLines = 2
                )

                if (service.isHomeService) {
                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysDivider()
                    GenesysSpacer(GenesysSpacing.Large)

                    GenesysText(text = "Endereço para Atendimento", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
                    GenesysSpacer(GenesysSpacing.Small)

                    val address = selectedAddress ?: com.itbenevides.genesys21.domain.model.Address(street = "", number = "", neighborhood = "", city = "", state = "", zipCode = "")

                    com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField(
                        value = address.zipCode,
                        onValueChange = {
                            selectedAddress = address.copy(zipCode = it)
                            if (it.length >= 8) {
                                coroutineScope.launch {
                                    val options = viewModel.calculateShipping(page.storeId ?: "admin", it)
                                    // Pega o valor do Uber/99 Ida e Volta para o travelFee
                                    val uberOption = options.find { o -> o.id == "uber" } ?: options.firstOrNull()
                                    travelFee = uberOption?.price ?: 0.0
                                }
                            }
                        },
                        label = "CEP do Local",
                        icon = GenesysIcons.Search
                    )

                    if (selectedAddress != null) {
                        GenesysSpacer(GenesysSpacing.Small)
                        com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField(
                            value = selectedAddress!!.street,
                            onValueChange = { selectedAddress = selectedAddress!!.copy(street = it) },
                            label = "Logradouro"
                        )
                        GenesysSpacer(GenesysSpacing.Small)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField(
                                    value = selectedAddress!!.number,
                                    onValueChange = { selectedAddress = selectedAddress!!.copy(number = it) },
                                    label = "Número"
                                )
                            }
                            Box(Modifier.weight(2f)) {
                                com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField(
                                    value = selectedAddress!!.neighborhood,
                                    onValueChange = { selectedAddress = selectedAddress!!.copy(neighborhood = it) },
                                    label = "Bairro"
                                )
                            }
                        }
                    }

                    if (travelFee > 0) {
                        GenesysSpacer(GenesysSpacing.Medium)
                        GenesysCard(backgroundColor = GenesysTheme.colors.accent.copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(GenesysIcons.Payments, null, tint = GenesysTheme.colors.accent)
                                GenesysSpacer(GenesysSpacing.Medium)
                                GenesysColumn(modifier = Modifier.weight(1f), usePadding = false) {
                                    GenesysText(text = "Taxa de Deslocamento", fontWeight = GenesysFontWeight.Bold, color = GenesysTheme.colors.accent)
                                    GenesysText(text = "Cálculo ida e volta para seu local", style = GenesysTextStyle.Label)
                                }
                                GenesysText(
                                    text = "${GenesysStrings.PricePrefix}$travelFee",
                                    fontWeight = GenesysFontWeight.ExtraBold,
                                    color = GenesysTheme.colors.accent
                                )
                            }
                        }
                    }
                }

                GenesysSpacer(GenesysSpacing.Large)

                // Booking Engine
                GenesysBookingEngine(
                    selectedDateTime = selectedDateTime,
                    availableSlots = availableSlotsForDate,
                    onDateSelected = { updateAvailableSlots(it) },
                    onDateTimeSelected = { selectedDateTime = it },
                    today = currentToday
                )

                GenesysSpacer(GenesysSpacing.Huge)

                val canConfirm = selectedDateTime != null && customerName.isNotBlank() && customerPhone.length >= 8

                GenesysLoadingButton(
                    text = if (!canConfirm) "Preencha todos os dados" else "Adicionar ao Carrinho",
                    onClick = {
                        selectedDateTime?.let { dt ->
                            val startInstant = dt.toInstant(TimeZone.currentSystemDefault())
                            val endInstant = startInstant.plus(service.durationMinutes.minutes)

                            val appointment = Appointment(
                                id = com.itbenevides.genesys21.util.GenesysUUID.randomUUID(),
                                storeId = page.storeId,
                                serviceId = service.id,
                                customerId = viewModel.userProfile.value?.id,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                startTime = startInstant,
                                endTime = endInstant,
                                travelFee = travelFee,
                                address = selectedAddress,
                                notes = if (customerNotes.isNotBlank()) {
                                    listOf(
                                        BookingNote(
                                            id = "",
                                            content = customerNotes,
                                            createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                                            authorName = customerName,
                                            isPrivate = false
                                        )
                                    )
                                } else emptyList()
                            )

                            viewModel.addServiceToCart(service, appointment)
                            router.navigateTo(com.itbenevides.genesys21.navigation.Route.Cart(page))
                        }
                    },
                    enabled = canConfirm,
                    isLoading = isLoading,
                    fillWidth = true,
                )
            }
        }
    }

    if (showLoginDialog) {
        com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog(
            onDismissRequest = { showLoginDialog = false },
            title = "Acesse sua conta",
            confirmButton = {}
        ) {
            com.itbenevides.genesys21.presentation.components.auth.GoogleSignInButton(
                modifier = Modifier.fillMaxWidth(),
                onTokenReceived = { idToken, accessToken ->
                    viewModel.signInWithToken(
                        idToken = idToken,
                        accessToken = accessToken,
                        provider = "google",
                        onSuccess = {
                            showLoginDialog = false
                            // Agora o usuário está logado, podemos prosseguir com a reserva
                        },
                        onError = { /* erro tratado no viewModel */ }
                    )
                },
                onError = { /* erro tratado no viewModel */ }
            )
        }
    }

    if (showSuccessDialog) {
        com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog(
            onDismissRequest = {
                showSuccessDialog = false
                router.goBack()
            },
            title = "Agendamento Realizado!",
            confirmButton = {
                GenesysLoadingButton(text = "OK", onClick = {
                    showSuccessDialog = false
                    router.goBack()
                })
            }
        ) {
            GenesysText(text = "Seu agendamento para ${service.name} foi confirmado com sucesso.", style = GenesysTextStyle.Body)
        }
    }
}
