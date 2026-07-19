package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorContent
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorState
import com.itbenevides.genesys21.presentation.screens.editor.ProductEditorContent
import com.itbenevides.genesys21.presentation.screens.editor.ProductEditorState
import com.itbenevides.genesys21.presentation.screens.editor.ServiceEditorContent
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.junit.Rule
import org.junit.Test
import org.koin.compose.koinInject

class ScreensSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val samplePage = Page(id = "p1", storeId = "s1", title = "Minha Vitrine")
    private val sampleProduct = Product(
        id = "prod1",
        storeId = "s1",
        name = "Produto de Teste",
        price = 99.90,
        imageUrls = listOf("https://picsum.photos/400/400"),
        description = "Descrição detalhada do produto de teste.",
        stock = 10
    )
    private val sampleService = BookingService(
        id = "serv1",
        storeId = "s1",
        name = "Serviço de Teste",
        price = 150.0,
        durationMinutes = 60
    )

    @Test
    fun testProductEditorScreen() {
        paparazzi.genesysSnapshot {
            val mockViewModel: PageViewModel = koinInject()
            val state = remember { mutableStateOf(ProductEditorState.initial(sampleProduct)) }

            ProductEditorContent(
                viewModel = mockViewModel,
                page = samplePage,
                product = sampleProduct,
                onSave = {},
                onBack = {},
                state = state.value,
                onStateChange = { state.value = it },
                onPickImage = {}
            )
        }
    }

    @Test
    fun testPageEditorScreen() {
        paparazzi.genesysSnapshot {
            PageEditorContent(
                state = PageEditorState(id = "p1", title = "Minha Loja", isEditing = true),
                onEvent = {}
            )
        }
    }

    @Test
    fun testServiceEditorScreen() {
        paparazzi.genesysSnapshot {
            val mockViewModel: PageViewModel = koinInject()
            ServiceEditorContent(
                viewModel = mockViewModel,
                service = sampleService,
                onSave = {},
                onBack = {},
                imageUrls = sampleService.imageUrls,
                onImageUrlsChange = {},
                isUploading = false,
                onPickImage = {}
            )
        }
    }
}
