package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Container vertical padronizado do Design System.
 */
@Composable
fun GenesysColumn(
    modifier: Modifier = Modifier,
    usePadding: Boolean = true,
    useScroll: Boolean = false,
    horizontalAlignment: GenesysAlignment = GenesysAlignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxWidth: Dp? = null,
    weightValue: Float = 0f,
    content: @Composable ColumnScope.() -> Unit
) {
    val alignment = when (horizontalAlignment) {
        GenesysAlignment.Start -> Alignment.Start
        GenesysAlignment.Center -> Alignment.CenterHorizontally
        GenesysAlignment.End -> Alignment.End
    }

    // Correção: Uso de weight fora de um escopo ColumnScope deve ser condicional
    val columnModifier = if (maxWidth != null) {
        modifier.widthIn(max = maxWidth)
    } else {
        modifier.fillMaxWidth()
    }

    val finalModifier = columnModifier
        .then(if (usePadding) Modifier.padding(GenesysDimens.SpacingLarge) else Modifier)
        .then(if (useScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)

    // Nota: O peso deve ser aplicado pelo pai que chama este componente se necessário
    Column(
        modifier = finalModifier,
        horizontalAlignment = alignment,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
