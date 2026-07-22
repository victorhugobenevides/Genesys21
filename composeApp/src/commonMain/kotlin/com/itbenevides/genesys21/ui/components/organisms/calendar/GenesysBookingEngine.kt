package com.itbenevides.genesys21.ui.components.organisms.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
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
) {
    val today = remember { kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    var selectedDateState by remember { mutableStateOf(selectedDateTime?.date ?: today) }
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
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        GenesysDatePicker(
            selectedDate = selectedDateState,
            onDateSelected = {
                selectedDateState = it
                selectedTime = null // Reset time on date change
                onDateSelected(it)
            },
        )

        Spacer(Modifier.height(32.dp))

        if (availableSlots.isNotEmpty()) {
            GenesysText(
                text = "Horários Disponíveis",
                style = GenesysTextStyle.Label,
                fontWeight = GenesysFontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
