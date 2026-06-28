package com.itbenevides.genesys21.ui.components.molecules.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.GenesysDimens

@Composable
fun GenesysQuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GenesysRow(modifier = modifier.wrapContentWidth()) {
        QuantityCircleButton(
            icon = GenesysIcons.Remove,
            onClick = onDecrease,
        )

        GenesysSpacer(GenesysSpacing.Small)

        GenesysText(
            text = quantity.toString(),
            fontWeight = GenesysFontWeight.ExtraBold,
        )

        GenesysSpacer(GenesysSpacing.Small)

        QuantityCircleButton(
            icon = GenesysIcons.Add,
            onClick = onIncrease,
            isPrimary = true,
        )
    }
}

@Composable
private fun QuantityCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(GenesysDimens.IconHuge),
        shape = CircleShape,
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(GenesysDimens.IconSmall),
            )
        }
    }
}
