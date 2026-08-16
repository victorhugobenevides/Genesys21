package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorContent
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorState
import com.itbenevides.genesys21.presentation.screens.editor.ProductEditorContent
import com.itbenevides.genesys21.presentation.screens.editor.ProductEditorState
import com.itbenevides.genesys21.presentation.screens.editor.ServiceEditorContent
import com.itbenevides.genesys21.presentation.screens.editor.ServiceSelectionScreen
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
import com.itbenevides.genesys21.presentation.screens.profile.ProfileScreen
import com.itbenevides.genesys21.presentation.screens.viewer.CustomerOrderHistoryScreen
import com.itbenevides.genesys21.presentation.screens.viewer.OrderTrackingScreen
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.compose.koinInject

class ScreensSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

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
    fun testLoginScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            LoginScreen(viewModel = koinInject(), onLoginSuccess = {})
        }
    }

    @Test
    fun testAdminDashboardResponsive() {
        val sampleSuperAdmin = com.itbenevides.genesys21.domain.model.UserProfile(
            id = "admin-1",
            email = "victorkoto@gmail.com",
            name = "Victor SuperAdmin",
            role = com.itbenevides.genesys21.domain.model.UserRole.SUPERADMIN,
            permissions = com.itbenevides.genesys21.domain.model.UserPermission.entries.toSet()
        )

        paparazzi.genesysResponsiveSnapshot(mockUserProfile = sampleSuperAdmin) {
            PageListScreen(
                viewModel = koinInject(),
                onAddPage = {},
                onEditPage = {},
                onViewPage = {},
                onLogout = {},
                onShowcase = {}
            )
        }
    }

    @Test
    fun testProfileScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ProfileScreen(viewModel = koinInject(), router = koinInject())
        }
    }

    @Test
    fun testProductEditorScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
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
    fun testPageEditorScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            PageEditorContent(
                state = PageEditorState(id = "p1", title = "Minha Loja", isEditing = true),
                onEvent = {}
            )
        }
    }

    @Test
    fun testServiceEditorScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
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

    @Test
    fun testServiceSelectionScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ServiceSelectionScreen(
                viewModel = koinInject(),
                selectedIds = emptyList(),
                onConfirm = {},
                onBack = {},
                onAddNewService = {}
            )
        }
    }

    @Test
    fun testOrderTrackingScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            OrderTrackingScreen(orderId = "order-123", status = "success", onBack = {})
        }
    }

    @Test
    fun testCustomerOrderHistoryScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            CustomerOrderHistoryScreen(onBack = {}, onOrderClick = {})
        }
    }

    @Test
    fun testTemplateCatalogScreenResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            com.itbenevides.genesys21.presentation.screens.editor.TemplateCatalogScreen(
                viewModel = koinInject(),
                onBack = {},
                onTemplateSelected = {}
            )
        }
    }
}
