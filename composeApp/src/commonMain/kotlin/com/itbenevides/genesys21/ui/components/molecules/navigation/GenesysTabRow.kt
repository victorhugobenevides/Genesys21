package com.itbenevides.genesys21.ui.components.molecules.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesysTabRow(
    selectedTabIndex: Int,
    tabs: List<GenesysTabData>,
    onTabSelected: (Int) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT
    val shouldScroll = isCompact || tabs.size > 3

    if (shouldScroll) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            divider = {},
        ) {
            TabItems(tabs, selectedTabIndex, onTabSelected)
        }
    } else {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {},
        ) {
            TabItems(tabs, selectedTabIndex, onTabSelected)
        }
    }
}

@Composable
private fun TabItems(
    tabs: List<GenesysTabData>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    tabs.forEachIndexed { index, tab ->
        Tab(
            selected = selectedTabIndex == index,
            onClick = { onTabSelected(index) },
            text = {
                if (tab.badgeCount > 0) {
                    BadgedBox(
                        badge = { Badge { Text(tab.badgeCount.toString()) } },
                    ) {
                        GenesysText(
                            text = tab.label,
                            fontWeight = if (selectedTabIndex == index) GenesysFontWeight.Bold else null,
                            style = GenesysTextStyle.Body,
                        )
                    }
                } else {
                    GenesysText(
                        text = tab.label,
                        fontWeight = if (selectedTabIndex == index) GenesysFontWeight.Bold else null,
                        style = GenesysTextStyle.Body,
                    )
                }
            },
            icon = tab.icon?.let { { Icon(it, null) } },
        )
    }
}

data class GenesysTabData(
    val label: String,
    val icon: ImageVector? = null,
    val badgeCount: Int = 0,
)
