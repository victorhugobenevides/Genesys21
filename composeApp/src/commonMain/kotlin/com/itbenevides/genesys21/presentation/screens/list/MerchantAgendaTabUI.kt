package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.calendar.GenesysDatePicker
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
import kotlin.time.Clock
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantAgendaTabUI(
    state: PageListState,
    viewModel: PageViewModel,
    onEvent: (PageListEvent) -> Unit,
) {
    val appointments by viewModel.appointments.collectAsState()
    val pages by viewModel.pages.collectAsState()
    val merchantId = pages.firstOrNull()?.ownerId ?: "admin"
    val availability by viewModel.availability.collectAsState()

    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val selectedDate = state.selectedDate ?: today

    var showAvailabilityDialog by remember { mutableStateOf(false) }
    var selectedAppointmentForEdit by remember { mutableStateOf<Appointment?>(null) }

    LaunchedEffect(selectedDate, merchantId) {
        viewModel.loadAppointments(selectedDate, merchantId)
        viewModel.loadAvailability(merchantId)
    }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysSpacer(GenesysSpacing.Large)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                GenesysText(text = "Agenda de Serviços", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
                GenesysText(
                    text = "Gerencie seus horários e agendamentos.",
                    style = GenesysTextStyle.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GenesysIconButton(
                icon = GenesysIcons.Settings,
                onClick = { showAvailabilityDialog = true }
            )
        }
        GenesysSpacer(GenesysSpacing.Large)

        GenesysCard(modifier = Modifier.fillMaxWidth()) {
            GenesysColumn(usePadding = true) {
                GenesysDatePicker(
                    selectedDate = selectedDate,
                    onDateSelected = { onEvent(PageListEvent.OnDateSelected(it)) },
                )
            }
        }

        GenesysSpacer(GenesysSpacing.Large)

        GenesysText(
            text = "Agendamentos para ${selectedDate.dayOfMonth}/${selectedDate.monthNumber}/${selectedDate.year}",
            style = GenesysTextStyle.Label,
            fontWeight = GenesysFontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        GenesysSpacer(GenesysSpacing.Medium)

        if (appointments.isEmpty() && !state.isLoading) {
            GenesysEmptyState(
                icon = GenesysIcons.Schedule,
                title = "Nenhum agendamento",
                description = "Não há clientes agendados para esta data.",
            )
        } else {
            appointments.sortedBy { it.startTime }.forEach { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    onClick = { selectedAppointmentForEdit = appointment }
                )
                GenesysSpacer(GenesysSpacing.Small)
            }
        }
    }

    if (showAvailabilityDialog) {
        AvailabilitySettingsDialog(
            initialAvailability = availability ?: MerchantAvailability(merchantId = merchantId),
            onDismiss = { showAvailabilityDialog = false },
            onSave = {
                viewModel.saveAvailability(it)
                showAvailabilityDialog = false
            }
        )
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
            GenesysLoadingButton(text = "Salvar Alterações", onClick = {
                onSave(appointment.copy(
                    notes = currentNotes,
                    status = status
                ))
            })
        },
        dismissButton = {
            if (appointment.status != BookingStatus.CANCELLED) {
                GenesysLoadingButton(
                    text = "Cancelar Agendamento",
                    containerColor = MaterialTheme.colorScheme.error,
                    onClick = onCancel
                )
            }
        }
    ) {
        GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 600.dp), useScroll = true) {
            GenesysText(text = "Cliente: ${appointment.customerName}", fontWeight = GenesysFontWeight.Bold)
            GenesysText(text = "Telefone: ${appointment.customerPhone}", style = GenesysTextStyle.Label)

            GenesysSpacer(GenesysSpacing.Medium)

            // Status Selector
            GenesysText(text = "Status Atual", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BookingStatus.entries.forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            GenesysSpacer(GenesysSpacing.Large)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Medium)

            // Timeline of Notes
            GenesysText(text = "Notas e Histórico", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysSpacing.Small)

            if (currentNotes.isEmpty()) {
                GenesysText(text = "Nenhuma nota adicionada.", style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                currentNotes.forEach { note ->
                    NoteItem(note)
                    GenesysSpacer(GenesysSpacing.Small)
                }
            }

            GenesysSpacer(GenesysSpacing.Medium)

            // Add New Note Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        label = { Text("Nova nota...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPrivateNote, onCheckedChange = { isPrivateNote = it })
                        Text("Nota Interna (Privada)", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            enabled = newNoteContent.isNotBlank(),
                            onClick = {
                                val note = BookingNote(
                                    id = "", // Server generates
                                    content = newNoteContent,
                                    createdAt = Clock.System.now().toEpochMilliseconds(),
                                    authorName = "Estabelecimento",
                                    isPrivate = isPrivateNote
                                )
                                currentNotes = currentNotes + note
                                newNoteContent = ""
                            }
                        ) {
                            Text("Adicionar")
                        }
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
                if (note.isPrivate) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(note.authorName, style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(note.content, style = MaterialTheme.typography.bodySmall)
        if (note.isPrivate) {
            Text("🔒 Privada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun AvailabilitySettingsDialog(
    initialAvailability: MerchantAvailability,
    onDismiss: () -> Unit,
    onSave: (MerchantAvailability) -> Unit
) {
    var availabilityState by remember { mutableStateOf(initialAvailability) }

    GenesysDialog(
        onDismissRequest = onDismiss,
        title = "Configurar Disponibilidade",
        confirmButton = {
            GenesysLoadingButton(text = "Salvar", onClick = { onSave(availabilityState) })
        }
    ) {
        GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 500.dp), useScroll = true) {
            GenesysText(text = "Defina os dias e intervalos de horário que você atende.", style = GenesysTextStyle.Body)
            GenesysSpacer(GenesysSpacing.Medium)

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
                            Text(day, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                            GenesysSpacer(GenesysSpacing.Small)
                            dayConfig.slots.forEachIndexed { slotIndex, slot ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = slot.startTime,
                                        onValueChange = { newTime ->
                                            val newSlots = dayConfig.slots.toMutableList()
                                            newSlots[slotIndex] = slot.copy(startTime = newTime)
                                            val newList = availabilityState.weeklyConfig.toMutableList()
                                            newList.removeAll { it.dayOfWeek == dayOfWeekNumber }
                                            newList.add(dayConfig.copy(slots = newSlots))
                                            availabilityState = availabilityState.copy(weeklyConfig = newList)
                                        },
                                        label = { Text("Início") },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("08:00") }
                                    )
                                    GenesysSpacer(GenesysSpacing.Small)
                                    OutlinedTextField(
                                        value = slot.endTime,
                                        onValueChange = { newTime ->
                                            val newSlots = dayConfig.slots.toMutableList()
                                            newSlots[slotIndex] = slot.copy(endTime = newTime)
                                            val newList = availabilityState.weeklyConfig.toMutableList()
                                            newList.removeAll { it.dayOfWeek == dayOfWeekNumber }
                                            newList.add(dayConfig.copy(slots = newSlots))
                                            availabilityState = availabilityState.copy(weeklyConfig = newList)
                                        },
                                        label = { Text("Fim") },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("18:00") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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
        GenesysRow(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GenesysColumn(usePadding = false, modifier = Modifier.width(60.dp)) {
                GenesysText(text = timeStr, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysColumn(modifier = Modifier.weight(1f), usePadding = false) {
                GenesysText(text = appointment.customerName, fontWeight = GenesysFontWeight.Bold)
                GenesysText(text = "Serviço ID: ${appointment.serviceId}", style = GenesysTextStyle.Label)

                val publicNotesCount = appointment.notes.count { !it.isPrivate }
                val privateNotesCount = appointment.notes.count { it.isPrivate }

                if (appointment.notes.isNotEmpty()) {
                    GenesysText(
                        text = "Notas: $publicNotesCount públicas, $privateNotesCount privadas",
                        style = GenesysTextStyle.Label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = when (appointment.status) {
                    BookingStatus.CONFIRMED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    BookingStatus.PENDING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    BookingStatus.CANCELLED -> Color(0xFFF44336).copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = when (appointment.status) {
                    BookingStatus.CONFIRMED -> Color(0xFF4CAF50)
                    BookingStatus.PENDING -> Color(0xFFFF9800)
                    BookingStatus.CANCELLED -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                Text(appointment.status.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
