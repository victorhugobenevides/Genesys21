package com.itbenevides.genesys21.presentation.screens.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.atoms.typography.*

@Composable
fun AdminSidebar(
    items: List<AdminMenuItem>,
    selectedItemId: Int,
    onItemClick: (AdminMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(GenesysTheme.colors.surface)
            .padding(vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Logo / App Name
        Box(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            GenesysText(
                text = "Genesys Console",
                style = GenesysTextStyle.Title,
                fontWeight = GenesysFontWeight.ExtraBold,
                color = GenesysTheme.colors.brand
            )
        }

        Spacer(Modifier.height(16.dp))

        val grouped = items.groupBy { it.category }

        AdminMenuCategory.entries.forEach { category ->
            val categoryItems = grouped[category] ?: emptyList()
            if (categoryItems.isNotEmpty()) {
                CategoryHeader(category.label)
                categoryItems.forEach { item ->
                    SidebarItem(
                        item = item,
                        isSelected = item.id == selectedItemId,
                        onClick = { onItemClick(item) }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CategoryHeader(label: String) {
    GenesysText(
        text = label,
        style = GenesysTextStyle.Label,
        color = GenesysTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
        fontWeight = GenesysFontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun SidebarItem(
    item: AdminMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) GenesysTheme.colors.brandContainer.copy(alpha = 0.3f) else Color.Transparent
    val contentColor = if (isSelected) GenesysTheme.colors.brand else GenesysTheme.colors.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(16.dp))
        Box(Modifier.weight(1f)) {
            GenesysText(
                text = item.label,
                style = GenesysTextStyle.Body,
                fontWeight = if (isSelected) GenesysFontWeight.Bold else GenesysFontWeight.Normal,
                color = contentColor
            )
        }

        if (item.badgeCount > 0) {
            Badge { Text(item.badgeCount.toString()) }
        }
    }
}
