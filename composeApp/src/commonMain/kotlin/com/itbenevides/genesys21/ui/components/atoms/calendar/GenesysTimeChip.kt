package com.itbenevides.genesys21.ui.components.atoms.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*

@Composable
fun GenesysTimeChip(
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) GenesysTheme.colors.brand else Color.Transparent
    val borderColor = if (isSelected) GenesysTheme.colors.brand else GenesysTheme.colors.outline
    val textColor = if (isSelected) GenesysTheme.colors.onBrand else GenesysTheme.colors.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GenesysTheme.config.cornerRadius))
            .background(backgroundColor)
            .border(GenesysTheme.spacing.xxxs, borderColor, RoundedCornerShape(GenesysTheme.config.cornerRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = GenesysTheme.spacing.m, vertical = GenesysTheme.spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        GenesysText(
            text = time,
            style = GenesysTextStyle.Body,
            fontWeight = if (isSelected) GenesysFontWeight.Bold else null,
            color = textColor
        )
    }
}
