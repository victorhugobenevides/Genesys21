package com.itbenevides.genesys21.presentation

import com.itbenevides.genesys21.MainDispatcherRule
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.util.Analytics
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPagesUseCase = mockk<GetPagesUseCase>(relaxed = true)
    private val savePageUseCase = mockk<SavePageUseCase>(relaxed = true)
    private val deletePageUseCase = mockk<DeletePageUseCase>(relaxed = true)
    private val getPublicPageUseCase = mockk<GetPublicPageUseCase>(relaxed = true)
    private val getPageByDomainUseCase = mockk<GetPageByDomainUseCase>(relaxed = true)
    private val getFirstPublicPageUseCase = mockk<GetFirstPublicPageUseCase>(relaxed = true)
    private val uploadImageUseCase = mockk<UploadImageUseCase>(relaxed = true)
    private val getOrdersUseCase = mockk<GetOrdersUseCase>(relaxed = true)
    private val getCustomerOrdersUseCase = mockk<GetCustomerOrdersUseCase>(relaxed = true)
    private val getOrderByIdUseCase = mockk<GetOrderByIdUseCase>(relaxed = true)
    private val submitOrderUseCase = mockk<SubmitOrderUseCase>(relaxed = true)
    private val updateOrderStatusUseCase = mockk<UpdateOrderStatusUseCase>(relaxed = true)
    private val getCategoriesUseCase = mockk<GetCategoriesUseCase>(relaxed = true)
    private val saveCategoryUseCase = mockk<SaveCategoryUseCase>(relaxed = true)
    private val deleteCategoryUseCase = mockk<DeleteCategoryUseCase>(relaxed = true)

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val cartRepository = mockk<CartRepository>(relaxed = true)
    private val customerRepository = mockk<CustomerRepository>(relaxed = true)
    private val pageDraftRepository = mockk<PageDraftRepository>(relaxed = true)
    private val orderRepository = mockk<OrderRepository>(relaxed = true)

    private val cartItemsFlow = MutableStateFlow<List<CartItem>>(emptyList())

    @Before
    fun setup() {
        mockkObject(Analytics)
        every { Analytics.logEvent(any(), any()) } returns Unit
        every { Analytics.trackPageView(any()) } returns Unit
        every { Analytics.logException(any(), any(), any()) } returns Unit
        
        every { cartRepository.cartItems } returns cartItemsFlow
        every { cartRepository.getSessionId() } returns "session_123"
        every { customerRepository.customerName } returns MutableStateFlow("Victor")
        every { customerRepository.customerPhone } returns MutableStateFlow("11999999999")
        
        coEvery { authRepository.getCurrentUserToken() } returns "token_123"
    }

    private fun createViewModel() = PageViewModel(
        getPagesUseCase, savePageUseCase, deletePageUseCase, getPublicPageUseCase,
        getPageByDomainUseCase, getFirstPublicPageUseCase, uploadImageUseCase,
        getOrdersUseCase, getCustomerOrdersUseCase, getOrderByIdUseCase,
        submitOrderUseCase, updateOrderStatusUseCase, authRepository,
        cartRepository, customerRepository, pageDraftRepository,
        getCategoriesUseCase, saveCategoryUseCase, deleteCategoryUseCase, orderRepository
    )

    @Test
    fun `addToCart deve atualizar o carrinho quando houver estoque`() = runTest {
        val viewModel = createViewModel()
        val product = Product(id = "p1", name = "Test", price = 10.0, stock = 5)
        coEvery { cartRepository.addToCart(any()) } returns Result.success(Unit)

        assertTrue(viewModel.addToCart(product))
        coVerify { cartRepository.addToCart(any()) }
    }

    @Test
    fun `loadPages deve atualizar lista de paginas`() = runTest {
        val pages = listOf(Page(id = "1", ownerId = "u1", title = "Loja", components = emptyList()))
        coEvery { getPagesUseCase(any()) } returns pages
        
        val viewModel = createViewModel()
        viewModel.loadPages()
        advanceUntilIdle()

        assertEquals(pages, viewModel.pages.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `handleError deve preencher o estado de erro`() = runTest {
        coEvery { getPagesUseCase(any()) } throws Exception("Erro de teste")
        
        val viewModel = createViewModel()
        viewModel.loadPages()
        advanceUntilIdle()

        assertNotNull(viewModel.currentError.value)
        assertEquals("Falha ao carregar páginas", viewModel.currentError.value?.title)
    }

    @Test
    fun `updateCartQuantity deve chamar o repositorio`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateCartQuantity("p1", 5)
        advanceUntilIdle()
        coVerify { cartRepository.updateQuantity("p1", 5) }
    }

    @Test
    fun `removeFromCart deve chamar o repositorio`() = runTest {
        val viewModel = createViewModel()
        coEvery { cartRepository.removeFromCart(any()) } returns Result.success(Unit)
        
        viewModel.removeFromCart("p1")
        advanceUntilIdle()
        
        coVerify { cartRepository.removeFromCart("p1") }
    }

    @Test
    fun `savePage deve chamar savePageUseCase com isEditing false`() = runTest {
        val viewModel = createViewModel()
        val page = Page(id = "new", title = "Test Page", ownerId = "token", components = emptyList())
        coEvery { savePageUseCase(any(), any(), any()) } returns Result.success(Unit)
        coEvery { getPagesUseCase(any()) } returns listOf(page)
        
        viewModel.savePage(page, isEditing = false) {}
        advanceUntilIdle()
        
        coVerify { savePageUseCase(page, "token_123", false) }
    }

    @Test
    fun `savePage deve chamar savePageUseCase com isEditing true`() = runTest {
        val viewModel = createViewModel()
        val page = Page(id = "existing", title = "Updated Page", ownerId = "token", components = emptyList())
        coEvery { savePageUseCase(any(), any(), any()) } returns Result.success(Unit)
        coEvery { getPagesUseCase(any()) } returns listOf(page)
        
        viewModel.savePage(page, isEditing = true) {}
        advanceUntilIdle()
        
        coVerify { savePageUseCase(page, "token_123", true) }
    }

    @Test
    fun `deletePage deve chamar deletePageUseCase`() = runTest {
        val viewModel = createViewModel()
        coEvery { deletePageUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { getPagesUseCase(any()) } returns emptyList()
        
        viewModel.deletePage("page123") {}
        advanceUntilIdle()
        
        coVerify { deletePageUseCase("page123", "token_123") }
    }

    @Test
    fun `loadCategories deve chamar getCategoriesUseCase`() = runTest {
        coEvery { getCategoriesUseCase(any()) } returns Result.success(emptyList())
        
        val viewModel = createViewModel()
        viewModel.loadCategories()
        advanceUntilIdle()
        
        coVerify { getCategoriesUseCase("token_123") }
    }

    @Test
    fun `saveCategory deve chamar saveCategoryUseCase`() = runTest {
        val viewModel = createViewModel()
        val category = Category(id = 1, ownerId = "token", name = "New Category")
        coEvery { saveCategoryUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { getCategoriesUseCase(any()) } returns Result.success(listOf(category))
        
        viewModel.saveCategory(category)
        advanceUntilIdle()
        
        coVerify { saveCategoryUseCase(category, "token_123") }
    }

    @Test
    fun `deleteCategory deve chamar deleteCategoryUseCase`() = runTest {
        val viewModel = createViewModel()
        coEvery { deleteCategoryUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { getCategoriesUseCase(any()) } returns Result.success(emptyList())
        
        viewModel.deleteCategory(1)
        advanceUntilIdle()
        
        coVerify { deleteCategoryUseCase(1, "token_123") }
    }

    @Test
    fun `loadOrders deve atualizar lista de pedidos`() = runTest {
        val orders = listOf(
            Order(
                id = "order1",
                userId = "user1",
                items = emptyList(),
                total = 100.0,
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
        )
        coEvery { getOrdersUseCase(any()) } returns kotlinx.coroutines.flow.flowOf(orders)
        
        val viewModel = createViewModel()
        viewModel.loadOrders()
        advanceUntilIdle()
        
        assertEquals(1, viewModel.orders.value.size)
        assertEquals("order1", viewModel.orders.value[0].id)
    }

    @Test
    fun `clearError deve limpar o estado de erro`() = runTest {
        coEvery { getPagesUseCase(any()) } throws Exception("Test error")
        
        val viewModel = createViewModel()
        viewModel.loadPages()
        advanceUntilIdle()
        
        assertNotNull(viewModel.currentError.value)
        
        viewModel.clearError()
        
        assertNull(viewModel.currentError.value)
    }

    @Test
    fun `initial state should have empty lists`() {
        val viewModel = createViewModel()
        
        assertTrue(viewModel.pages.value.isEmpty())
        assertTrue(viewModel.orders.value.isEmpty())
        assertTrue(viewModel.categories.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.currentError.value)
    }
}
