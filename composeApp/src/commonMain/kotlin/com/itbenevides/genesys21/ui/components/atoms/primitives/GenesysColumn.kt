package com.itbenevides.genesys21.ui.components.atoms.primitives

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

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
    weightValue: Float = 0f, // Deprecated, will be ignored
    content: @Composable ColumnScope.() -> Unit,
) {
    val alignment =
        when (horizontalAlignment) {
            GenesysAlignment.Start -> Alignment.Start
            GenesysAlignment.Center -> Alignment.CenterHorizontally
            GenesysAlignment.End -> Alignment.End
        }

    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    val horizontalPadding = if (isCompact) GenesysDimens.SpacingMedium else GenesysDimens.SpacingLarge

    // BUG FIX: Se useScroll for true, a Column DEVE preencher o tamanho máximo para que o scroll interno funcione.
    // Caso contrário, ela cresce infinitamente e o container pai (como o Box do Scaffold) a corta.
    val columnModifier = Modifier
        .then(if (useScroll) Modifier.fillMaxSize().verticalScroll(rememberScrollState()) else Modifier.fillMaxWidth())
        .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier)
        .then(
            if (usePadding) {
                Modifier.padding(horizontal = horizontalPadding, vertical = GenesysDimens.SpacingLarge)
            } else {
                Modifier
            }
        )

    Column(
        modifier = modifier.then(columnModifier),
        horizontalAlignment = alignment,
        verticalArrangement = verticalArrangement,
        content = content,
    )
}
