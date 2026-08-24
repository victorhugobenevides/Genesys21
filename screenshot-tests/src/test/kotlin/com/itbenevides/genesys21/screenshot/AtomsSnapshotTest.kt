package com.itbenevides.genesys21.screenshot

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysBadge
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.GenesysTextStyle
import org.junit.Rule
import org.junit.Test

class AtomsSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testTypographyResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GenesysText(text = "Headline Large", style = GenesysTextStyle.Headline)
                GenesysText(text = "Title Medium", style = GenesysTextStyle.Title)
                GenesysText(text = "Body Text", style = GenesysTextStyle.Body)
                GenesysText(text = "Label Small", style = GenesysTextStyle.Label)
                GenesysText(text = "Error Message", style = GenesysTextStyle.Error)
            }
        }
    }

    @Test
    fun testBadgesResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenesysBadge(label = "New", color = Color(0xFF2CB1FF))
                GenesysBadge(label = "Sale", color = Color(0xFFD81159), showDot = false)
                GenesysBadge(label = "In Stock", color = Color(0xFF4CAF50))
            }
        }
    }

    @Test
    fun testIconButtonsResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenesysIconButton(icon = Icons.Default.ShoppingCart, onClick = {})
                GenesysIconButton(icon = Icons.Default.Favorite, onClick = {}, tint = Color.Red)
                GenesysIconButton(icon = Icons.Default.Add, onClick = {})
            }
        }
    }
}
