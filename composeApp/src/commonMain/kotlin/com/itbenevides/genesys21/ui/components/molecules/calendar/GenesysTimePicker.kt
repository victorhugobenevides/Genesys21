package com.itbenevides.genesys21.ui.components.molecules.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.atoms.calendar.GenesysTimeChip
import com.itbenevides.genesys21.ui.theme.GenesysTheme

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
        horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.xs)
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
