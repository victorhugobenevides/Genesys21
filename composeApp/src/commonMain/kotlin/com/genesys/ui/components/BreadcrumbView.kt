package com.genesys.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.genesys.ui.theme.Dimensions

/**
 * Breadcrumb item data
 */
data class BreadcrumbItem(
    val label: String,
    val onClick: () -> Unit
)

/**
 * Breadcrumb navigation component
 * 
 * Features:
 * - Shows navigation hierarchy
 * - Clickable items (except last)
 * - Automatic text truncation
 * - Separator icons
 * - Accessibility support
 * 
 * Usage:
 * ```kotlin
 * BreadcrumbView(
 *     items = listOf(
 *         BreadcrumbItem("Home") { navController.popBackStack() },
 *         BreadcrumbItem("Settings") { navController.navigate("settings") },
 *         BreadcrumbItem("Profile") { }
 *     )
 * )
 * ```
 */
@Composable
fun BreadcrumbView(
    items: List<BreadcrumbItem>,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 4
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.spacing_lg, vertical = Dimensions.spacing_md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayItems = if (items.size > maxVisibleItems) {
            listOf(items.first()) + items.takeLast(maxVisibleItems - 1)
        } else {
            items
        }
        
        displayItems.forEachIndexed { index, item ->
            val isLast = index == displayItems.lastIndex
            
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isLast) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.primary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .then(
                        if (!isLast) {
                            Modifier.clickable(onClick = item.onClick)
                        } else Modifier
                    )
            )
            
            if (!isLast) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(16.dp)
                        .padding(horizontal = Dimensions.spacing_xs),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}