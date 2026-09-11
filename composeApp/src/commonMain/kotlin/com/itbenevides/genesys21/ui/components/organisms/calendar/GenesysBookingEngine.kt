package com.itbenevides.genesys21.ui.components.organisms.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.calendar.GenesysDatePicker
import com.itbenevides.genesys21.ui.components.molecules.calendar.GenesysTimePicker
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import kotlinx.datetime.*

@Composable
fun GenesysBookingEngine(
    selectedDateTime: LocalDateTime?,
    availableSlots: List<String>,
    onDateSelected: (LocalDate) -> Unit,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate? = null,
) {
    val currentToday = remember { today ?: kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    var selectedDateState by remember { mutableStateOf(selectedDateTime?.date ?: currentToday) }
    var selectedTime by remember {
        mutableStateOf(
            selectedDateTime?.time?.let {
                "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
            },
        )
    }

    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    if (isCompact) {
        Column(modifier = modifier.fillMaxWidth()) {
            DateSection(selectedDateState) {
                selectedDateState = it
                selectedTime = null
                onDateSelected(it)
            }
            Spacer(Modifier.height(GenesysTheme.spacing.xl))
            TimeSection(availableSlots, selectedTime) { timeStr ->
                selectedTime = timeStr
                val parts = timeStr.split(":")
                val localTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                onDateTimeSelected(LocalDateTime(selectedDateState, localTime))
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.l)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DateSection(selectedDateState) {
                    selectedDateState = it
                    selectedTime = null
                    onDateSelected(it)
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                TimeSection(availableSlots, selectedTime) { timeStr ->
                    selectedTime = timeStr
                    val parts = timeStr.split(":")
                    val localTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                    onDateTimeSelected(LocalDateTime(selectedDateState, localTime))
                }
            }
        }
    }
}

@Composable
private fun DateSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Column {
        GenesysText(
            text = "Selecione uma Data",
            style = GenesysTextStyle.Label,
            fontWeight = GenesysFontWeight.Bold,
            color = GenesysTheme.colors.brand,
        )
        Spacer(Modifier.height(GenesysTheme.spacing.s))
        GenesysDatePicker(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
        )
    }
}

@Composable
private fun TimeSection(
    availableSlots: List<String>,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit
) {
    Column {
        GenesysText(
            text = "Horários Disponíveis",
            style = GenesysTextStyle.Label,
            fontWeight = GenesysFontWeight.Bold,
            color = GenesysTheme.colors.brand,
        )
        Spacer(Modifier.height(GenesysTheme.spacing.s))
        if (availableSlots.isNotEmpty()) {
            GenesysTimePicker(
                availableSlots = availableSlots,
                selectedSlot = selectedTime,
                onSlotSelected = onTimeSelected,
            )
        } else {
            GenesysText(
                text = "Não há horários disponíveis para esta data.",
                style = GenesysTextStyle.Body,
                color = GenesysTheme.colors.onSurfaceVariant,
            )
        }
    }
}
