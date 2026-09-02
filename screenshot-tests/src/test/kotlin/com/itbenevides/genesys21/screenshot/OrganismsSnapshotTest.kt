package com.itbenevides.genesys21.screenshot

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import com.itbenevides.genesys21.ui.components.organisms.product.GenesysProductList
import com.itbenevides.genesys21.ui.components.organisms.status.GenesysTrackingTimeline
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test

class OrganismsSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val sampleProduct =
        Product(
            id = "1",
            storeId = "store-1",
            name = "SmartWatch Genesys Pro",
            price = 899.90,
            imageUrls = listOf("https://images.unsplash.com/photo-1544117518-30dd5f2f309e?q=80&w=800"),
            description = "High-performance smartwatch.",
            stock = 15,
        )

    @Test
    fun testProductListGrid() {
        genesysResponsiveSnapshot(paparazzi) {
            GenesysProductList(
                products = listOf(sampleProduct, sampleProduct.copy(id = "2"), sampleProduct.copy(id = "3")),
                isHorizontal = false,
            )
        }
    }

    @Test
    fun testProductListHorizontal() {
        genesysResponsiveSnapshot(paparazzi) {
            GenesysProductList(
                products = listOf(sampleProduct, sampleProduct.copy(id = "2"), sampleProduct.copy(id = "3")),
                isHorizontal = true,
            )
        }
    }

    @Test
    fun testTrackingTimeline() {
        genesysResponsiveSnapshot(paparazzi) {
            Box(Modifier.padding(16.dp)) {
                GenesysTrackingTimeline(currentStatus = OrderStatus.PROCESSING)
            }
        }
    }

    @Test
    fun testBookingEngine() {
        val fixedDate = LocalDate(2026, 7, 28)
        val fixedDateTime = LocalDateTime(fixedDate, LocalTime(9, 0))

        genesysResponsiveSnapshot(paparazzi) {
            com.itbenevides.genesys21.ui.components.organisms.calendar.GenesysBookingEngine(
                selectedDateTime = fixedDateTime,
                availableSlots = listOf("09:00", "11:00", "15:00"),
                onDateSelected = {},
                onDateTimeSelected = {},
                today = fixedDate
            )
        }
    }

    @Test
    fun testGridComponent() {
        val grid = com.itbenevides.genesys21.domain.model.PageComponent.Grid(
            columns = 2,
            title = "Layout em Grade",
            items = listOf(
                com.itbenevides.genesys21.domain.model.PageComponent.GridItem(
                    components = listOf(com.itbenevides.genesys21.domain.model.PageComponent.Text("Célula 1"))
                ),
                com.itbenevides.genesys21.domain.model.PageComponent.GridItem(
                    components = listOf(com.itbenevides.genesys21.domain.model.PageComponent.Text("Célula 2"))
                )
            )
        )
        genesysResponsiveSnapshot(paparazzi) {
            com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer(
                component = grid,
                storeId = "store-1",
                onProductClick = {},
                onServiceClick = {}
            )
        }
    }
}
