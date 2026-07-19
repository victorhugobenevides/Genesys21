package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.viewer.CartContent
import com.itbenevides.genesys21.presentation.screens.viewer.CartScreenState
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsContent
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsState
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import org.junit.Rule
import org.junit.Test

class ResponsivenessSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi(deviceConfig = DeviceConfig.PIXEL_5)

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

    private val samplePage = Page(id = "test", storeId = "store-1", title = "My Store")

    @Test
    fun testProductDetailsMobile() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.PIXEL_5)
        paparazzi.genesysSnapshot {
            ProductDetailsContent(
                state = ProductDetailsState(product = sampleProduct),
                backendUrl = "",
                onEvent = {},
            )
        }
    }

    @Test
    fun testProductDetailsDesktop() {
        // Simulating Desktop width
        paparazzi.unsafeUpdateConfig(DeviceConfig.NEXUS_10.copy(screenWidth = 1200))
        paparazzi.genesysSnapshot {
            ProductDetailsContent(
                state = ProductDetailsState(product = sampleProduct),
                backendUrl = "",
                onEvent = {},
            )
        }
    }

    @Test
    fun testCartMobile() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.PIXEL_5)
        paparazzi.genesysSnapshot {
            CartContent(
                state = CartScreenState(total = 899.90),
                backendUrl = "",
                onEvent = {},
            )
        }
    }

    @Test
    fun testCartDesktop() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.NEXUS_10.copy(screenWidth = 1200))
        paparazzi.genesysSnapshot {
            CartContent(
                state = CartScreenState(total = 899.90),
                backendUrl = "",
                onEvent = {},
            )
        }
    }
}
