package com.itbenevides.genesys21.ui.components.atoms.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*

@Composable
fun GenesysCalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        isSelected -> GenesysTheme.colors.brand
        isToday -> GenesysTheme.colors.brand.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> GenesysTheme.colors.onBrand
        !isEnabled -> GenesysTheme.colors.onSurface.copy(alpha = 0.3f)
        isToday -> GenesysTheme.colors.brand
        else -> GenesysTheme.colors.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        GenesysText(
            text = day.toString(),
            style = GenesysTextStyle.Body,
            fontWeight = if (isSelected || isToday) GenesysFontWeight.Bold else null,
            color = textColor
        )
    }
}
