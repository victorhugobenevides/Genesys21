package com.itbenevides.genesys21.screenshot

import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.viewer.CartContent
import com.itbenevides.genesys21.presentation.screens.viewer.CartScreenState
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsContent
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsState
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelContent
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelState
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

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
    fun testProductDetailsPhone() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.PIXEL_5)
        paparazzi.genesysSnapshot(widthOverride = 393.dp) {
            ProductDetailsContent(
                state = ProductDetailsState(product = sampleProduct),
                backendUrl = "",
                onEvent = {}
            )
        }
    }

    @Test
    fun testProductDetailsDesktop() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.NEXUS_10.copy(screenWidth = 1200))
        paparazzi.genesysSnapshot(widthOverride = 1200.dp) {
            ProductDetailsContent(
                state = ProductDetailsState(product = sampleProduct),
                backendUrl = "",
                onEvent = {}
            )
        }
    }

    @Test
    fun testCartPhone() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.PIXEL_5)
        paparazzi.genesysSnapshot(widthOverride = 393.dp) {
            CartContent(
                state = CartScreenState(
                    cartItems = listOf(com.itbenevides.genesys21.domain.model.CartItem(sampleProduct, 1)),
                    total = 2999.0,
                    customerName = "Victor Hugo",
                    customerPhone = "11999999999"
                ),
                backendUrl = "",
                onEvent = {}
            )
        }
    }

    @Test
    fun testCartDesktop() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.NEXUS_10.copy(screenWidth = 1200))
        paparazzi.genesysSnapshot(widthOverride = 1200.dp) {
            CartContent(
                state = CartScreenState(
                    cartItems = listOf(com.itbenevides.genesys21.domain.model.CartItem(sampleProduct, 1)),
                    total = 2999.0,
                    customerName = "Victor Hugo",
                    customerPhone = "11999999999"
                ),
                backendUrl = "",
                onEvent = {}
            )
        }
    }

    @Test
    fun testEditorPhone() {
        val mockViewModel = mockk<PageViewModel>(relaxed = true)
        every { mockViewModel.isLoading } returns MutableStateFlow(false)
        every { mockViewModel.services } returns MutableStateFlow(emptyList())

        paparazzi.unsafeUpdateConfig(DeviceConfig.PIXEL_5)
        paparazzi.genesysSnapshot(widthOverride = 393.dp) {
            WhiteLabelContent(
                state = WhiteLabelState(
                    page = samplePage.copy(components = listOf(
                        com.itbenevides.genesys21.domain.model.PageComponent.Header("Editor Responsivo"),
                        com.itbenevides.genesys21.domain.model.PageComponent.Text("Edite em qualquer lugar.")
                    )),
                    editingComponentIndex = 0
                ),
                viewModel = mockViewModel,
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

    @Test
    fun testEditorDesktop() {
        val mockViewModel = mockk<PageViewModel>(relaxed = true)
        every { mockViewModel.isLoading } returns MutableStateFlow(false)
        every { mockViewModel.services } returns MutableStateFlow(emptyList())

        paparazzi.unsafeUpdateConfig(DeviceConfig.NEXUS_10.copy(screenWidth = 1200))
        paparazzi.genesysSnapshot(widthOverride = 1200.dp) {
            WhiteLabelContent(
                state = WhiteLabelState(
                    page = samplePage.copy(components = listOf(
                        com.itbenevides.genesys21.domain.model.PageComponent.Header("Editor Responsivo"),
                        com.itbenevides.genesys21.domain.model.PageComponent.Text("Edite em qualquer lugar.")
                    )),
                    editingComponentIndex = 0
                ),
                viewModel = mockViewModel,
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
