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
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.ui.components.atoms.calendar.GenesysCalendarDay
import kotlinx.datetime.*

@Composable
fun GenesysDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
) {
    var currentMonth by remember { mutableStateOf(LocalDate(selectedDate.year, selectedDate.month, 1)) }

    val nextMonthDate = currentMonth.plus(1, DateTimeUnit.MONTH)
    val daysInMonth = nextMonthDate.minus(1, DateTimeUnit.DAY).dayOfMonth

    val firstDayOfWeek = currentMonth.dayOfWeek.isoDayNumber // 1 (Mon) to 7 (Sun)
    val paddingDays = firstDayOfWeek - 1

    Column(modifier = modifier.fillMaxWidth()) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", modifier = Modifier.size(20.dp))
            }

            Text(
                text = "${currentMonth.month.name.take(3)} ${currentMonth.year}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )

            IconButton(
                onClick = { currentMonth = nextMonthDate },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Weekdays Header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "T", "Q", "Q", "S", "S", "D").forEach { day ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

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
                            val isToday = date == kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

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
