package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.PageListEvent
import com.itbenevides.genesys21.presentation.screens.list.PageListState
import com.itbenevides.genesys21.presentation.screens.list.components.AdminTabHeader
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.calendar.GenesysDatePicker
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaTab(
    state: PageListState,
    viewModel: PageViewModel,
    onEvent: (PageListEvent) -> Unit,
) {
    val appointments by viewModel.appointments.collectAsState()
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val storeId = userProfile?.id ?: "admin"
    val availability by viewModel.availability.collectAsState()

    var agendaViewMode by remember { mutableStateOf(0) } // 0: Vista Diária, 1: Todos, 2: Disponibilidade

    val today = remember { kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val selectedDate = state.selectedDate ?: today

    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT
    val horizontalPadding = if (isCompact) GenesysTheme.spacing.m else GenesysTheme.spacing.l

    var selectedAppointmentForEdit by remember { mutableStateOf<Appointment?>(null) }

    LaunchedEffect(selectedDate, storeId, agendaViewMode) {
        when (agendaViewMode) {
            0 -> viewModel.loadAppointments(selectedDate, storeId)
            1 -> viewModel.loadUpcomingAppointments(storeId)
            2 -> viewModel.loadAvailability(storeId)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            AdminTabHeader(
                title = "Gestão de Agenda",
                subtitle = "Configure seus atendimentos e horários."
            )
        }

        item {
            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                // Seletor de Visualização
                GenesysTabRow(
                    selectedTabIndex = agendaViewMode,
                    tabs = listOf(
                        GenesysTabData("Vista Diária", GenesysIcons.Schedule),
                        GenesysTabData("Todos", GenesysIcons.List, badgeCount = upcomingAppointments.size),
                        GenesysTabData("Horários", GenesysIcons.Settings)
                    ),
                    onTabSelected = { agendaViewMode = it }
                )
            }
        }

        item { GenesysSpacer(GenesysTheme.spacing.l) }

        // Conteúdo Dinâmico por Item para garantir Scroll Robusto
        when (agendaViewMode) {
            0 -> { // Vista Diária
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        GenesysCard(modifier = Modifier.widthIn(max = 600.dp)) {
                            Column(modifier = Modifier.padding(GenesysTheme.spacing.m)) {
                                GenesysDatePicker(
                                    selectedDate = selectedDate,
                                    onDateSelected = { onEvent(PageListEvent.OnDateSelected(it)) },
                                )
                            }
                        }
                    }
                }
                item { GenesysSpacer(GenesysTheme.spacing.l) }
                item {
                    Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        GenesysText(
                            text = "Agendamentos para ${selectedDate.dayOfMonth}/${selectedDate.monthNumber}",
                            style = GenesysTextStyle.Label,
                            fontWeight = GenesysFontWeight.Bold,
                            color = GenesysTheme.colors.brand
                        )
                    }
                }
                item { GenesysSpacer(GenesysTheme.spacing.m) }

                if (appointments.isEmpty() && !state.isLoading) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                            GenesysEmptyState(
                                icon = GenesysIcons.Schedule,
                                title = "Nenhum agendamento",
                                description = "Não há atendimentos para esta data.",
                            )
                        }
                    }
                } else {
                    items(items = appointments.sortedBy { it.startTime }, key = { it.id }) { appointment ->
                        Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                            AppointmentCard(appointment = appointment, onClick = { selectedAppointmentForEdit = appointment })
                        }
                        GenesysSpacer(GenesysTheme.spacing.s)
                    }
                }
            }
            1 -> { // Todos
                if (upcomingAppointments.isEmpty() && !state.isLoading) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                            GenesysEmptyState(icon = GenesysIcons.List, title = "Agenda vazia", description = "Sem agendamentos futuros.")
                        }
                    }
                } else {
                    val grouped = upcomingAppointments.groupBy { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date }
                    grouped.forEach { (date, appts) ->
                        item {
                            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                                GenesysText(text = "${date.dayOfMonth}/${date.monthNumber}/${date.year}", fontWeight = GenesysFontWeight.Bold, color = GenesysTheme.colors.accent)
                            }
                        }
                        item { GenesysSpacer(GenesysTheme.spacing.s) }
                        items(items = appts, key = { it.id }) { appointment ->
                            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                                AppointmentCard(appointment = appointment, onClick = { selectedAppointmentForEdit = appointment })
                            }
                            GenesysSpacer(GenesysTheme.spacing.s)
                        }
                        item { GenesysSpacer(GenesysTheme.spacing.m) }
                    }
                }
            }
            2 -> { // Horários (Disponibilidade)
                item {
                    Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        AvailabilityManagementView(
                            initialAvailability = availability ?: MerchantAvailability(storeId = storeId),
                            onSave = { viewModel.saveAvailability(it.copy(storeId = storeId)) }
                        )
                    }
                }
            }
        }

        item { GenesysSpacer(GenesysTheme.spacing.huge) }
    }

    if (selectedAppointmentForEdit != null) {
        EditAppointmentDialog(
            appointment = selectedAppointmentForEdit!!,
            onDismiss = { selectedAppointmentForEdit = null },
            onSave = { updated ->
                viewModel.updateAppointment(updated)
                selectedAppointmentForEdit = null
            },
            onCancel = {
                viewModel.updateAppointment(selectedAppointmentForEdit!!.copy(status = BookingStatus.CANCELLED))
                selectedAppointmentForEdit = null
            }
        )
    }
}

@Composable
private fun EditAppointmentDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    onCancel: () -> Unit
) {
    var newNoteContent by remember { mutableStateOf("") }
    var isPrivateNote by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf(appointment.status) }
    var currentNotes by remember { mutableStateOf(appointment.notes) }

    GenesysDialog(
        onDismissRequest = onDismiss,
        title = "Gerenciar Agendamento",
        confirmButton = {
            GenesysLoadingButton(
                text = "Salvar Alterações",
                fillWidth = true,
                onClick = {
                    onSave(appointment.copy(
                        notes = currentNotes,
                        status = status
                    ))
                }
            )
        },
        dismissButton = {
            if (appointment.status != BookingStatus.CANCELLED) {
                GenesysLoadingButton(
                    text = "Cancelar Agendamento",
                    containerColor = GenesysTheme.colors.error,
                    fillWidth = true,
                    onClick = onCancel
                )
            }
        }
    ) {
        GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 600.dp), useScroll = true) {
            GenesysText(text = "Cliente: ${appointment.customerName}", fontWeight = GenesysFontWeight.Bold, isSelectable = true)
            GenesysText(text = "Telefone: ${appointment.customerPhone}", style = GenesysTextStyle.Label, isSelectable = true)

            GenesysSpacer(GenesysTheme.spacing.m)

            // Status Selector
            GenesysText(text = "Status Atual", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BookingStatus.entries.forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            GenesysSpacer(GenesysTheme.spacing.l)
            GenesysDivider()
            GenesysSpacer(GenesysTheme.spacing.m)

            // Timeline of Notes
            GenesysText(text = "Notas e Histórico", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.s)

            if (currentNotes.isEmpty()) {
                GenesysText(text = "Nenhuma nota adicionada.", style = GenesysTextStyle.Label, color = GenesysTheme.colors.onSurfaceVariant)
            } else {
                currentNotes.forEach { note ->
                    NoteItem(note)
                    GenesysSpacer(GenesysTheme.spacing.s)
                }
            }

            GenesysSpacer(GenesysTheme.spacing.m)

            // Add New Note Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GenesysTheme.colors.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    GenesysTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        label = "Nova nota...",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPrivateNote, onCheckedChange = { isPrivateNote = it })
                        Text("Nota Interna (Privada)", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        GenesysTextButton(
                            text = "Adicionar",
                            enabled = newNoteContent.isNotBlank(),
                            onClick = {
                                val note = BookingNote(
                                    id = "", // Server generates
                                    content = newNoteContent,
                                    createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                                    authorName = "Estabelecimento",
                                    isPrivate = isPrivateNote
                                )
                                currentNotes = currentNotes + note
                                newNoteContent = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteItem(note: BookingNote) {
    val date = Instant.fromEpochMilliseconds(note.createdAt).toLocalDateTime(TimeZone.currentSystemDefault())
    val dateStr = "${date.hour}:${date.minute.toString().padStart(2, '0')}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (note.isPrivate) GenesysTheme.colors.accent.copy(alpha = 0.1f)
                else GenesysTheme.colors.brandContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(note.authorName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = GenesysTheme.colors.onSurfaceVariant)
        }
        Text(note.content, style = MaterialTheme.typography.bodySmall)
        if (note.isPrivate) {
            Text("🔒 Privada", style = MaterialTheme.typography.labelSmall, color = GenesysTheme.colors.accent)
        }
    }
}

@Composable
private fun AvailabilityManagementView(
    initialAvailability: MerchantAvailability,
    onSave: (MerchantAvailability) -> Unit
) {
    var availabilityState by remember { mutableStateOf(initialAvailability) }

    Column(modifier = Modifier.fillMaxWidth()) {
        GenesysText(text = "Defina os dias e intervalos de horário que você atende.", style = GenesysTextStyle.Body)
        GenesysSpacer(GenesysTheme.spacing.m)

        val days = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")

        days.forEachIndexed { index, day ->
            val dayOfWeekNumber = index + 1
            val dayConfig = availabilityState.weeklyConfig.find { it.dayOfWeek == dayOfWeekNumber }
                ?: DayConfig(dayOfWeek = dayOfWeekNumber, isClosed = true)

            GenesysCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(day, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = !dayConfig.isClosed,
                            onCheckedChange = { isOpen ->
                                val newConfig = if (isOpen) {
                                    dayConfig.copy(
                                        isClosed = false,
                                        slots = if (dayConfig.slots.isEmpty()) listOf(TimeSlotRange("08:00", "18:00")) else dayConfig.slots
                                    )
                                } else {
                                    dayConfig.copy(isClosed = true)
                                }

                                val newList = availabilityState.weeklyConfig.toMutableList()
                                newList.removeAll { it.dayOfWeek == dayOfWeekNumber }
                                newList.add(newConfig)
                                availabilityState = availabilityState.copy(weeklyConfig = newList.sortedBy { it.dayOfWeek })
                            }
                        )
                    }

                    if (!dayConfig.isClosed) {
                        GenesysSpacer(GenesysTheme.spacing.s)
                        dayConfig.slots.forEachIndexed { slotIndex, slot ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GenesysTextField(
                                    value = slot.startTime,
                                    onValueChange = { newTime ->
                                        val newSlots = dayConfig.slots.toMutableList()
                                        newSlots[slotIndex] = slot.copy(startTime = newTime)
                                        val newList = availabilityState.weeklyConfig.toMutableList()
                                        newList.removeAll { it.dayOfWeek == dayOfWeekNumber }
                                        newList.add(dayConfig.copy(slots = newSlots))
                                        availabilityState = availabilityState.copy(weeklyConfig = newList)
                                    },
                                    label = "Início",
                                    modifier = Modifier.weight(1f),
                                    placeholder = "08:00"
                                )
                                GenesysSpacer(GenesysTheme.spacing.s)
                                GenesysTextField(
                                    value = slot.endTime,
                                    onValueChange = { newTime ->
                                        val newSlots = dayConfig.slots.toMutableList()
                                        newSlots[slotIndex] = slot.copy(endTime = newTime)
                                        val newList = availabilityState.weeklyConfig.toMutableList()
                                        newList.removeAll { it.dayOfWeek == dayOfWeekNumber }
                                        newList.add(dayConfig.copy(slots = newSlots))
                                        availabilityState = availabilityState.copy(weeklyConfig = newList)
                                    },
                                    label = "Fim",
                                    modifier = Modifier.weight(1f),
                                    placeholder = "18:00"
                                )
                            }
                        }
                    }
                }
            }
        }

        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysLoadingButton(
            text = "Salvar Alterações de Horário",
            onClick = { onSave(availabilityState) },
            fillWidth = true
        )
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit
) {
    val startTime = appointment.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr = "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}"

    GenesysCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(60.dp)) {
                GenesysText(text = timeStr, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold, color = GenesysTheme.colors.brand)
            }
            GenesysSpacer(GenesysTheme.spacing.m)
            Column(Modifier.weight(1f)) {
                GenesysText(text = appointment.customerName, fontWeight = GenesysFontWeight.Bold, isSelectable = true)
                GenesysText(text = "Serviço ID: ${appointment.serviceId}", style = GenesysTextStyle.Label, isSelectable = true)

                val publicNotesCount = appointment.notes.count { !it.isPrivate }
                val privateNotesCount = appointment.notes.count { it.isPrivate }

                if (appointment.notes.isNotEmpty()) {
                    GenesysText(
                        text = "Notas: $publicNotesCount públicas, $privateNotesCount privadas",
                        style = GenesysTextStyle.Label,
                        color = GenesysTheme.colors.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = when (appointment.status) {
                    BookingStatus.CONFIRMED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    BookingStatus.PENDING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    BookingStatus.CANCELLED -> Color(0xFFF44336).copy(alpha = 0.1f)
                    else -> GenesysTheme.colors.surfaceVariant
                },
                contentColor = when (appointment.status) {
                    BookingStatus.CONFIRMED -> Color(0xFF4CAF50)
                    BookingStatus.PENDING -> Color(0xFFFF9800)
                    BookingStatus.CANCELLED -> Color(0xFFF44336)
                    else -> GenesysTheme.colors.onSurfaceVariant
                }
            ) {
                Text(appointment.status.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
