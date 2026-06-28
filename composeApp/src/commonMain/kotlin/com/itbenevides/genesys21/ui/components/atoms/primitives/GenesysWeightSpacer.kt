package com.itbenevides.genesys21.ui.components.atoms.primitives

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Espaçador flexível para uso dentro de uma GenesysRow.
 * Ocupa o espaço proporcional definido pelo weightValue.
 */
@Composable
fun RowScope.GenesysWeightSpacer(weightValue: Float) {
    Spacer(Modifier.weight(weightValue))
}

/**
 * Espaçador flexível para uso dentro de uma GenesysColumn.
 * Ocupa o espaço proporcional definido pelo weightValue.
 */
@Composable
fun ColumnScope.GenesysWeightSpacer(weightValue: Float) {
    Spacer(Modifier.weight(weightValue))
}
