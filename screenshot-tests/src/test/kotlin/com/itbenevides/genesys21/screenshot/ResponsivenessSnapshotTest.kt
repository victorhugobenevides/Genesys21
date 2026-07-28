package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.viewer.CartContent
import com.itbenevides.genesys21.presentation.screens.viewer.CartScreenState
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsContent
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsState
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import org.junit.Rule
import org.junit.Test

class ResponsivenessSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    private val sampleProduct =
        Product(
            id = "1",
            storeId = "store-1",
            name = "SmartWatch Genesys Pro",
            price = 899.90,
            imageUrls = listOf("https://images.unsplash.com/photo-1544117518-30dd5f2f309e?q=80&w=800"),
            description = "High-performance smartwatch with health tracking and elegant design.",
            stock = 15,
        )

    @Test
    fun testProductDetailsResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ProductDetailsContent(
                state = ProductDetailsState(product = sampleProduct),
                backendUrl = "",
                onEvent = {},
            )
        }
    }

    @Test
    fun testCartResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            CartContent(
                state = CartScreenState(total = 899.90),
                store = null,
                backendUrl = "",
                onEvent = {},
            )
        }
    }
}
