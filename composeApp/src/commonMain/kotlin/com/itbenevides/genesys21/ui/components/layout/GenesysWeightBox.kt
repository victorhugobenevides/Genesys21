package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Utilitário para aplicar peso proporcional dentro de uma GenesysRow.
 */
@Composable
fun RowScope.GenesysWeightBox(
    weightValue: Float,
    content: @Composable () -> Unit,
) {
    Box(Modifier.weight(weightValue)) { content() }
}

/**
 * Utilitário para aplicar peso proporcional dentro de uma GenesysColumn.
 */
@Composable
fun ColumnScope.GenesysWeightBox(
    weightValue: Float,
    content: @Composable () -> Unit,
) {
    Box(Modifier.weight(weightValue)) { content() }
}
