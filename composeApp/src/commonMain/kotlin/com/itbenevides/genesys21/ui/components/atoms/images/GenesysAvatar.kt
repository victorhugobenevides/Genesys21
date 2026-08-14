package com.itbenevides.genesys21.ui.components.atoms.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysAvatar(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = GenesysTheme.colors.brand.copy(alpha = 0.1f),
    iconTint: Color = GenesysTheme.colors.brand,
) {
    Box(
        modifier =
            modifier
                .size(GenesysDimens.IconHuge)
                .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(GenesysDimens.IconLarge),
        )
    }
}
