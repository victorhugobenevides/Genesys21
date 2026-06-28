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
    BoxWithConstraints {
        val alignment =
            when (horizontalAlignment) {
                GenesysAlignment.Start -> Alignment.Start
                GenesysAlignment.Center -> Alignment.CenterHorizontally
                GenesysAlignment.End -> Alignment.End
            }

        val columnModifier =
            if (maxWidth != null) {
                modifier.widthIn(max = maxWidth)
            } else {
                modifier.fillMaxWidth()
            }

        // Responsividade: Reduz padding lateral em telas pequenas (mobile)
        val finalModifier =
            columnModifier
                .then(
                    if (usePadding) {
                        val horizontalPadding = if (this@BoxWithConstraints.maxWidth < 600.dp) GenesysDimens.SpacingMedium else GenesysDimens.SpacingLarge
                        Modifier.padding(horizontal = horizontalPadding, vertical = GenesysDimens.SpacingLarge)
                    } else {
                        Modifier
                    },
                )
                .then(if (useScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)

        Column(
            modifier = finalModifier,
            horizontalAlignment = alignment,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
