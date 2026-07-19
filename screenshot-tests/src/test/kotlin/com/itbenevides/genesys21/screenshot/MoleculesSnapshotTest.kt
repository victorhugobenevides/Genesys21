package com.itbenevides.genesys21.screenshot

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.viewer.ProductCard
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysSearchBar
import org.junit.Rule
import org.junit.Test

class MoleculesSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val sampleProduct =
        Product(
            id = "1",
            storeId = "store-1",
            name = "Smartphone Genesys X",
            price = 2999.0,
            imageUrls = listOf("https://via.placeholder.com/300"),
            description = "O melhor smartphone da categoria",
            stock = 5,
        )

    @Test
    fun testProductCard() {
        paparazzi.genesysSnapshot {
            Box(Modifier.padding(16.dp).width(200.dp)) {
                ProductCard(product = sampleProduct)
            }
        }
    }

    @Test
    fun testSearchBar() {
        paparazzi.genesysSnapshot {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                GenesysSearchBar(
                    value = "",
                    onValueChange = {},
                    placeholder = "Search products...",
                )
                Spacer(Modifier.height(16.dp))
                GenesysSearchBar(
                    value = "Samsung",
                    onValueChange = {},
                    placeholder = "Search products...",
                )
            }
        }
    }

    @Test
    fun testGenesysCard() {
        paparazzi.genesysSnapshot {
            GenesysCard(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                androidx.compose.material3.Text("This is a Genesys Card with custom content.")
            }
        }
    }

    @Test
    fun testInputs() {
        paparazzi.genesysSnapshot {
            Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                com.itbenevides.genesys21.ui.components.molecules.input.GenesysColorField(
                    value = "#6200EE",
                    onValueChange = {},
                    label = "Color Field"
                )
                com.itbenevides.genesys21.ui.components.molecules.input.GenesysDropdownField(
                    value = "Option 1",
                    onValueChange = {},
                    label = "Dropdown Field",
                    options = listOf("Option 1", "Option 2", "Option 3")
                )
            }
        }
    }
}
