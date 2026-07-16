package com.itbenevides.genesys21.ui.components.molecules.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.calendar.GenesysCalendarDay
import kotlinx.datetime.*

@Composable
fun GenesysDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
) {
    var currentMonth by remember { mutableStateOf(LocalDate(selectedDate.year, selectedDate.month, 1)) }

    val nextMonthDate = currentMonth.plus(1, DateTimeUnit.MONTH)
    val daysInMonth = nextMonthDate.minus(1, DateTimeUnit.DAY).day

    val firstDayOfWeek = currentMonth.dayOfWeek.isoDayNumber // 1 (Mon) to 7 (Sun)
    val paddingDays = firstDayOfWeek - 1

    Column(modifier = modifier.fillMaxWidth()) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH)
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
            }

            Text(
                text = "${currentMonth.month.name} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            IconButton(onClick = {
                currentMonth = nextMonthDate
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Weekdays Header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Days Grid
        val totalCells = daysInMonth + paddingDays
        val rows = (totalCells + 6) / 7

        repeat(rows) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { colIndex ->
                    val cellIndex = rowIndex * 7 + colIndex
                    val dayNumber = cellIndex - paddingDays + 1

                    Box(Modifier.weight(1f)) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = LocalDate(currentMonth.year, currentMonth.month, dayNumber)
                            val isEnabled = date >= minDate
                            val isToday = date == kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                            GenesysCalendarDay(
                                day = dayNumber,
                                isSelected = date == selectedDate,
                                isToday = isToday,
                                isEnabled = isEnabled,
                                onClick = { onDateSelected(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}
