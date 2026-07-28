package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.viewer.CartContent
import com.itbenevides.genesys21.presentation.screens.viewer.CartScreenState
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsContent
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsState
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelContent
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelState
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import org.junit.Rule
import org.junit.Test
import org.koin.compose.koinInject

class AdaptiveLayoutsSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    private val sampleProduct = Product(
        id = "1",
        storeId = "store-1",
        name = "Smartphone Genesys X",
        price = 2999.0,
        imageUrls = listOf("https://picsum.photos/800/800"),
        description = "Premium device with AI capabilities.",
        stock = 10
    )

    private val samplePage = Page(id = "test-page", storeId = "store-1", title = "Minha Vitrine")

    @Test
    fun testProductDetailsResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ProductDetailsContent(
                state = ProductDetailsState(product = sampleProduct),
                backendUrl = "",
                onEvent = {}
            )
        }
    }

    @Test
    fun testCartResponsive() {
        val sampleService = com.itbenevides.genesys21.domain.model.BookingService(
            id = "s1", storeId = "store-1", name = "Corte de Cabelo", price = 50.0, durationMinutes = 30
        )
        val sampleAppointment = com.itbenevides.genesys21.domain.model.Appointment(
            id = "a1", storeId = "store-1", serviceId = "s1", customerName = "Victor", customerPhone = "11999999999",
            startTime = kotlinx.datetime.Instant.fromEpochMilliseconds(1735689600000), // Fixed date for snapshots
            endTime = kotlinx.datetime.Instant.fromEpochMilliseconds(1735691400000)
        )

        paparazzi.genesysResponsiveSnapshot {
            CartContent(
                state = CartScreenState(
                    cartItems = listOf(
                        com.itbenevides.genesys21.domain.model.CartItem(product = sampleProduct, quantity = 1),
                        com.itbenevides.genesys21.domain.model.CartItem(service = sampleService, appointment = sampleAppointment, quantity = 1)
                    ),
                    total = 3049.0,
                    customerName = "Victor Hugo",
                    customerPhone = "11999999999",
                    paymentMethod = com.itbenevides.genesys21.domain.model.PaymentMethod.APP
                ),
                store = null,
                backendUrl = "",
                onEvent = {}
            )
        }
    }

    @Test
    fun testEditorResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            WhiteLabelContent(
                state = WhiteLabelState(
                    page = samplePage.copy(components = listOf(
                        com.itbenevides.genesys21.domain.model.PageComponent.Header("Editor Responsivo"),
                        com.itbenevides.genesys21.domain.model.PageComponent.Text("Edite em qualquer lugar.")
                    )),
                    editingComponentIndex = 0
                ),
                viewModel = koinInject(),
                onEvent = {},
                originalPage = samplePage,
                displayCategories = emptyList(),
                allProducts = emptyList(),
                onManageCategories = {},
                onPickImage = {},
                onDiscardClicked = {}
            )
        }
    }
}
