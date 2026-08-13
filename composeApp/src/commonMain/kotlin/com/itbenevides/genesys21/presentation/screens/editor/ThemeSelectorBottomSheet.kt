package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun ThemeSelectorBottomSheet(
    currentTheme: PageThemeConfig,
    onThemeSelected: (PageThemeConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    GenesysBottomSheet(
        onDismiss = onDismiss,
        title = GenesysStrings.EditorThemes,
    ) {
        GenesysColumn(usePadding = true) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
            ) {
                items(PageThemeConfig.entries.filter { it != PageThemeConfig.DEFAULT }) { theme ->
                    ThemeOptionItem(
                        theme = theme,
                        isSelected = theme == currentTheme,
                        onClick = { onThemeSelected(theme) },
                    )
                }
            }
            GenesysSpacer(GenesysSpacing.ExtraLarge)
        }
    }
}

@Composable
private fun ThemeOptionItem(
    theme: PageThemeConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val themeColor = getThemePreviewColor(theme)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(themeColor)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                )
            }
        }

        GenesysSpacer(GenesysSpacing.Small)

        GenesysText(
            text = theme.name,
            style = GenesysTextStyle.Label,
            fontWeight = if (isSelected) GenesysFontWeight.Bold else GenesysFontWeight.Normal,
        )
    }
}

private fun getThemePreviewColor(theme: PageThemeConfig): Color {
    return when (theme) {
        PageThemeConfig.ELEGANCE -> Color(0xFF1C1C1E)
        PageThemeConfig.VIBRANT -> Color(0xFF007AFF)
        PageThemeConfig.NATURE -> Color(0xFF2D4F1E)
        PageThemeConfig.MONO -> Color(0xFF000000)
        PageThemeConfig.MIDNIGHT -> Color(0xFF000000)
        PageThemeConfig.CANDY -> Color(0xFFFF2D55)
        else -> Color.Gray
    }
}
