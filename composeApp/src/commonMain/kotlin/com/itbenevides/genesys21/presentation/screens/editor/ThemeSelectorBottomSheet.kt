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
import com.itbenevides.genesys21.ui.components.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
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
        PageThemeConfig.ROYAL -> Color(0xFF14213D)
        PageThemeConfig.OCEAN -> Color(0xFF00ADB5)
        PageThemeConfig.FOREST -> Color(0xFF283618)
        PageThemeConfig.CANDY -> Color(0xFFD81159)
        PageThemeConfig.SUNSET -> Color(0xFFE76F51)
        PageThemeConfig.BERRY -> Color(0xFF6A0572)
        PageThemeConfig.MINIMAL -> Color(0xFF000000)
        PageThemeConfig.VINTAGE -> Color(0xFF8B5E3C)
        PageThemeConfig.NORDIC -> Color(0xFF4A90E2)
        PageThemeConfig.COFFEE -> Color(0xFF6F4E37)
        PageThemeConfig.SOFT_LAVENDER -> Color(0xFF967BB6)
        PageThemeConfig.SKY_BLUE -> Color(0xFF039BE5)
        PageThemeConfig.MINT_GREEN -> Color(0xFF00C853)
        PageThemeConfig.PEACH -> Color(0xFFFF8A65)
        PageThemeConfig.LEMON -> Color(0xFFFBC02D)
        PageThemeConfig.RADARANI -> Color(0xFF2CB1FF)
        PageThemeConfig.DARK_MODE -> Color(0xFFBB86FC)
        PageThemeConfig.MIDNIGHT -> Color(0xFF1A1A2E)
        PageThemeConfig.NEON -> Color(0xFF39FF14)
        PageThemeConfig.DEEP_SPACE -> Color(0xFF00D1FF)
        PageThemeConfig.LUXURY_GOLD -> Color(0xFFD4AF37)
        else -> Color.Gray
    }
}
