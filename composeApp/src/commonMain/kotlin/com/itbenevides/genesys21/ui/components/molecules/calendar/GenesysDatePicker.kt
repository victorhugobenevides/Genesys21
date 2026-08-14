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
import com.itbenevides.genesys21.ui.components.atoms.calendar.GenesysCalendarDay
import com.itbenevides.genesys21.ui.theme.GenesysTheme
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
                modifier = Modifier.size(GenesysTheme.spacing.xl)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", modifier = Modifier.size(GenesysTheme.spacing.m))
            }

            Text(
                text = "${currentMonth.month.name.take(3)} ${currentMonth.year}",
                style = GenesysTheme.typography.body,
                fontWeight = FontWeight.Bold,
            )

            IconButton(
                onClick = { currentMonth = nextMonthDate },
                modifier = Modifier.size(GenesysTheme.spacing.xl)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", modifier = Modifier.size(GenesysTheme.spacing.m))
            }
        }

        Spacer(Modifier.height(GenesysTheme.spacing.xs))

        // Weekdays Header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "T", "Q", "Q", "S", "S", "D").forEach { day ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        style = GenesysTheme.typography.label,
                        color = GenesysTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(GenesysTheme.spacing.xxs))

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
