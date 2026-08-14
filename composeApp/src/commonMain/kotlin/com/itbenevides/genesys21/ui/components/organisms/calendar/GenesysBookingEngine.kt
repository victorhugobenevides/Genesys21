package com.itbenevides.genesys21.ui.components.organisms.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.calendar.GenesysDatePicker
import com.itbenevides.genesys21.ui.components.molecules.calendar.GenesysTimePicker
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

    Column(modifier = modifier.fillMaxWidth()) {
        GenesysText(
            text = "Selecione uma Data",
            style = GenesysTextStyle.Label,
            fontWeight = GenesysFontWeight.Bold,
            color = GenesysTheme.colors.brand,
        )
        Spacer(Modifier.height(GenesysTheme.spacing.s))
        GenesysDatePicker(
            selectedDate = selectedDateState,
            onDateSelected = {
                selectedDateState = it
                selectedTime = null // Reset time on date change
                onDateSelected(it)
            },
        )

        Spacer(Modifier.height(GenesysTheme.spacing.xl))

        if (availableSlots.isNotEmpty()) {
            GenesysText(
                text = "Horários Disponíveis",
                style = GenesysTextStyle.Label,
                fontWeight = GenesysFontWeight.Bold,
                color = GenesysTheme.colors.brand,
            )
            Spacer(Modifier.height(GenesysTheme.spacing.s))
            GenesysTimePicker(
                availableSlots = availableSlots,
                selectedSlot = selectedTime,
                onSlotSelected = { timeStr ->
                    selectedTime = timeStr
                    val parts = timeStr.split(":")
                    val localTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                    onDateTimeSelected(LocalDateTime(selectedDateState, localTime))
                },
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
