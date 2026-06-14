package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.layout.GenesysRow
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
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
