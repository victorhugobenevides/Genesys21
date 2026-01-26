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
 * Focado puramente em layout semântico. Proporções devem ser tratadas via
 * GenesysWeightBox para garantir estabilidade absoluta e evitar recursão no compilador WasmJs.
 */
@Composable
fun GenesysColumn(
    modifier: Modifier = Modifier,
    usePadding: Boolean = true,
    useScroll: Boolean = false,
    horizontalAlignment: GenesysAlignment = GenesysAlignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxWidth: Dp? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val alignment = when (horizontalAlignment) {
        GenesysAlignment.Start -> Alignment.Start
        GenesysAlignment.Center -> Alignment.CenterHorizontally
        GenesysAlignment.End -> Alignment.End
    }

    var columnModifier: Modifier = modifier
    
    if (maxWidth != null) {
        columnModifier = columnModifier.widthIn(max = maxWidth)
    } else {
        columnModifier = columnModifier.fillMaxWidth()
    }

    if (usePadding) {
        columnModifier = columnModifier.padding(GenesysDimens.SpacingLarge)
    }
    
    if (useScroll) {
        columnModifier = columnModifier.verticalScroll(rememberScrollState())
    }

    Column(
        modifier = columnModifier,
        horizontalAlignment = alignment,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
