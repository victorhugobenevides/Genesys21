package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Container horizontal padronizado do Design System.
 */
@Composable
fun GenesysRow(
    modifier: Modifier = Modifier,
    fillWidth: Boolean = true,
    usePadding: Boolean = false,
    useHorizontalScroll: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    GenesysRowContent(
        modifier = modifier,
        fillWidth = fillWidth,
        usePadding = usePadding,
        useHorizontalScroll = useHorizontalScroll,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * Implementação real da Row para evitar recursão infinita no compilador WasmJs.
 */
@Composable
internal fun GenesysRowContent(
    modifier: Modifier = Modifier,
    fillWidth: Boolean = true,
    usePadding: Boolean = false,
    useHorizontalScroll: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    var rowModifier: Modifier = modifier
    
    if (fillWidth) {
        rowModifier = rowModifier.fillMaxWidth()
    } else {
        rowModifier = rowModifier.wrapContentWidth()
    }

    if (usePadding) {
        rowModifier = rowModifier.padding(horizontal = GenesysDimens.SpacingLarge)
    }

    if (useHorizontalScroll) {
        rowModifier = rowModifier.horizontalScroll(rememberScrollState())
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * Extensão para RowScope que aplica o peso proporcional.
 */
@Composable
fun RowScope.GenesysRow(
    modifier: Modifier = Modifier,
    fillWidth: Boolean = true,
    usePadding: Boolean = false,
    useHorizontalScroll: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    weightValue: Float = 0f,
    content: @Composable RowScope.() -> Unit
) {
    val weightModifier = if (weightValue > 0f) Modifier.weight(weightValue) else Modifier
    GenesysRowContent(
        modifier = weightModifier.then(modifier),
        fillWidth = fillWidth,
        usePadding = usePadding,
        useHorizontalScroll = useHorizontalScroll,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * Extensão para ColumnScope que aplica o peso proporcional.
 */
@Composable
fun ColumnScope.GenesysRow(
    modifier: Modifier = Modifier,
    fillWidth: Boolean = true,
    usePadding: Boolean = false,
    useHorizontalScroll: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    weightValue: Float = 0f,
    content: @Composable RowScope.() -> Unit
) {
    val weightModifier = if (weightValue > 0f) Modifier.weight(weightValue) else Modifier
    GenesysRowContent(
        modifier = weightModifier.then(modifier),
        fillWidth = fillWidth,
        usePadding = usePadding,
        useHorizontalScroll = useHorizontalScroll,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}
