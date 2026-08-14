package com.itbenevides.genesys21.ui.components.atoms.buttons

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysFab(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = GenesysTheme.colors.brand,
        contentColor = GenesysTheme.colors.onBrand,
        shape = CircleShape,
    ) {
        Icon(icon, contentDescription)
    }
}
