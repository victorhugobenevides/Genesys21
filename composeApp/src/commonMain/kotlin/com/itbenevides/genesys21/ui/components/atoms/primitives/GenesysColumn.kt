package com.itbenevides.genesys21.ui.components.atoms.primitives

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    content: @Composable ColumnScope.() -> Unit,
) {
    val alignment =
        when (horizontalAlignment) {
            GenesysAlignment.Start -> Alignment.Start
            GenesysAlignment.Center -> Alignment.CenterHorizontally
            GenesysAlignment.End -> Alignment.End
        }

    // Se estivermos em um contexto de peso (dentro de outra Column), aplicamos o peso aqui
    // Nota: weight só pode ser usado em ColumnScope. Como este é o container externo,
    // quem chama o GenesysColumn deve aplicar o weight se necessário.
    val finalBaseModifier = modifier

    BoxWithConstraints(modifier = finalBaseModifier) {
        val horizontalPadding = if (this@BoxWithConstraints.maxWidth < 600.dp) GenesysDimens.SpacingMedium else GenesysDimens.SpacingLarge

        val columnModifier = Modifier
            .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier.fillMaxWidth())
            .then(if (useScroll) Modifier.fillMaxHeight().verticalScroll(rememberScrollState()) else Modifier)
            .then(
                if (usePadding) {
                    Modifier.padding(horizontal = horizontalPadding, vertical = GenesysDimens.SpacingLarge)
                } else {
                    Modifier
                }
            )

        Column(
            modifier = columnModifier,
            horizontalAlignment = alignment,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
