package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Dados para cada aba do GenesysTabRow.
 */
data class GenesysTabData(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun GenesysTabRow(
    selectedTabIndex: Int,
    tabs: List<GenesysTabData>,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        tabs.forEachIndexed { index, tabData ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(tabData.icon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(tabData.label)
                        if (tabData.badgeCount > 0) {
                            Spacer(Modifier.width(4.dp))
                            Badge { Text(tabData.badgeCount.toString()) }
                        }
                    }
                }
            )
        }
    }
}
