package com.itbenevides.genesys21.ui.components.molecules.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.calendar.GenesysTimeChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenesysTimePicker(
    availableSlots: List<String>,
    selectedSlot: String?,
    onSlotSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableSlots.forEach { slot ->
            GenesysTimeChip(
                time = slot,
                isSelected = slot == selectedSlot,
                onClick = { onSlotSelected(slot) }
            )
        }
    }
}
