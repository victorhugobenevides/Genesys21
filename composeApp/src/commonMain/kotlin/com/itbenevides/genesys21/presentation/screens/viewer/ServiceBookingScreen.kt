package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.organisms.calendar.GenesysBookingEngine
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@Composable
fun ServiceBookingScreen(
    service: BookingService,
    page: Page,
    router: Router,
    viewModel: PageViewModel,
) {
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    var customerNotes by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var availableSlotsForDate by remember { mutableStateOf<List<String>>(emptyList()) }
    var slotLoading by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    // Logic to load available slots when date changes
    val updateAvailableSlots = { date: LocalDate ->
        coroutineScope.launch {
            slotLoading = true
            availableSlotsForDate = viewModel.getAvailableSlots(page.ownerId ?: "admin", service, date)
            slotLoading = false
        }
    }

    // Load initial slots
    LaunchedEffect(Unit) {
        updateAvailableSlots(kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
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
                        GenesysText(text = service.name, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                        GenesysText(text = "${service.durationMinutes} minutos", style = GenesysTextStyle.Label)
                        GenesysText(
                            text = "${GenesysStrings.PricePrefix}${service.price}",
                            style = GenesysTextStyle.Body,
                            fontWeight = GenesysFontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
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

                GenesysSpacer(GenesysSpacing.Large)

                // Booking Engine
                GenesysBookingEngine(
                    selectedDateTime = selectedDateTime,
                    availableSlots = availableSlotsForDate,
                    onDateSelected = { updateAvailableSlots(it) },
                    onDateTimeSelected = { selectedDateTime = it },
                )

                GenesysSpacer(GenesysSpacing.Huge)

                val canConfirm = selectedDateTime != null && customerName.isNotBlank() && customerPhone.length >= 8

                GenesysLoadingButton(
                    text = if (!canConfirm) "Preencha todos os dados" else "Confirmar Agendamento",
                    onClick = {
                        if (!isLoggedIn) {
                            showLoginDialog = true
                        } else {
                            selectedDateTime?.let { dt ->
                                val startInstant = dt.toInstant(TimeZone.currentSystemDefault())
                                val endInstant = startInstant.plus(service.durationMinutes.minutes)

                                val appointment = Appointment(
                                    id = "", // Server will generate
                                    serviceId = service.id,
                                    merchantId = page.ownerId ?: "admin",
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    startTime = startInstant,
                                    endTime = endInstant,
                                    userId = viewModel.userProfile.value?.id,
                                    notes = if (customerNotes.isNotBlank()) {
                                        listOf(
                                            BookingNote(
                                                content = customerNotes,
                                                createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                                                authorName = customerName,
                                                isPrivate = false
                                            )
                                        )
                                    } else emptyList()
                                )

                                viewModel.createAppointment(
                                    merchantId = page.ownerId ?: "admin",
                                    appointment = appointment
                                ) {
                                    showSuccessDialog = true
                                }
                            }
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
            com.itbenevides.genesys21.presentation.screens.login.LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    showLoginDialog = false
                    // Trigger confirm again or directly
                }
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
