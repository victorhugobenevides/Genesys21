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
    fun `loadPublicPage should return page when success`() = runTest {
        val page = Page(id = "public1", ownerId = "user", title = "Public", components = emptyList())
        coEvery { getPublicPageUseCase("public1") } returns Result.success(page)
        
        val viewModel = createViewModel()
        val result = viewModel.loadPublicPage("public1")
        
        assertEquals(page, result)
    }

    @Test
    fun `loadPublicPage should return null and set error when failure`() = runTest {
        coEvery { getPublicPageUseCase("public1") } returns Result.failure(Exception("Not found"))
        
        val viewModel = createViewModel()
        val result = viewModel.loadPublicPage("public1")
        
        assertNull(result)
        assertNotNull(viewModel.currentError.value)
    }

    @Test
    fun `loadPageByDomain should return page when success`() = runTest {
        val page = Page(id = "domain1", ownerId = "user", title = "Domain", components = emptyList())
        coEvery { getPageByDomainUseCase("example.com") } returns Result.success(page)
        
        val viewModel = createViewModel()
        val result = viewModel.loadPageByDomain("example.com")
        
        assertEquals(page, result)
    }

    @Test
    fun `loadFirstPublicPage should update pages list`() = runTest {
        val page = Page(id = "first", ownerId = "", title = "First", components = emptyList())
        coEvery { getFirstPublicPageUseCase() } returns page
        
        val viewModel = createViewModel()
        val result = viewModel.loadFirstPublicPage()
        
        assertEquals(page, result)
        assertEquals(listOf(page), viewModel.pages.value)
    }

    @Test
    fun `uploadImage should call use case and invoke onSuccess`() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        val fileName = "image.jpg"
        var uploadedUrl: String? = null
        
        coEvery { uploadImageUseCase(bytes, fileName, any()) } returns Result.success("/url/image.jpg")
        
        val viewModel = createViewModel()
        viewModel.uploadImage(bytes, fileName) { uploadedUrl = it }
        advanceUntilIdle()
        
        assertEquals("/url/image.jpg", uploadedUrl)
        coVerify { uploadImageUseCase(bytes, fileName, "token_123") }
    }

    @Test
    fun `updateOrderStatus should call use case`() = runTest {
        val viewModel = createViewModel()
        coEvery { updateOrderStatusUseCase(any(), any(), any()) } returns Result.success(Unit)
        
        viewModel.updateOrderStatus("order1", OrderStatus.PROCESSING)
        advanceUntilIdle()
        
        coVerify { updateOrderStatusUseCase("token_123", "order1", OrderStatus.PROCESSING) }
    }

    @Test
    fun `addToCart should return false when stock is zero`() {
        val viewModel = createViewModel()
        val product = Product(id = "p1", name = "Test", price = 10.0, stock = 0)

        assertFalse(viewModel.addToCart(product))
    }

    @Test
    fun `loadCustomerOrders should update customerOrders`() = runTest {
        val orders = listOf(
            Order(
                id = "order1",
                userId = "user1",
                items = emptyList(),
                total = 100.0,
                status = OrderStatus.PENDING,
                createdAt = 1234567890L
            )
        )
        coEvery { getCustomerOrdersUseCase(any()) } returns Result.success(orders)

        val viewModel = createViewModel()
        viewModel.loadCustomerOrders()
        advanceUntilIdle()

        assertEquals(orders, viewModel.customerOrders.value)
    }

    @Test
    fun `trackOrder should set trackedOrder when found`() = runTest {
        val order = Order(
            id = "order1",
            userId = "user1",
            items = emptyList(),
            total = 100.0,
            status = OrderStatus.COMPLETED,
            createdAt = 1234567890L
        )
        coEvery { getOrderByIdUseCase("order1") } returns Result.success(order)

        val viewModel = createViewModel()
        viewModel.trackOrder("order1")
        advanceUntilIdle()

        assertEquals(order, viewModel.trackedOrder.value)
    }

    @Test
    fun `trackOrder should set error when order not found`() = runTest {
        coEvery { getOrderByIdUseCase("missing") } returns Result.failure(Exception("Not found"))

        val viewModel = createViewModel()
        viewModel.trackOrder("missing")
        advanceUntilIdle()

        assertNotNull(viewModel.currentError.value)
    }

    @Test
    fun `submitOrder should set error when cart is empty`() = runTest {
        val viewModel = createViewModel()
        val page = Page(id = "page1", ownerId = "owner", title = "Test", components = emptyList())

        viewModel.submitOrder(page)
        advanceUntilIdle()

        assertNotNull(viewModel.currentError.value)
        coVerify(exactly = 0) { submitOrderUseCase(any()) }
    }

    @Test
    fun `submitOrder should call use case and clear cart on success`() = runTest {
        val viewModel = createViewModel()
        val page = Page(id = "page1", ownerId = "owner", title = "Test", components = emptyList())
        cartItemsFlow.value = listOf(CartItem(Product(id = "p1", name = "Test", price = 10.0), 1))
        coEvery { submitOrderUseCase(any()) } returns Result.success("ORD-123")
        var completedId: String? = null

        viewModel.submitOrder(page) { completedId = it }
        advanceUntilIdle()

        assertEquals("ORD-123", completedId)
        coVerify { cartRepository.clearCart() }
    }

    @Test
    fun `createMercadoPagoCheckout should set paymentUrl on success`() = runTest {
        val viewModel = createViewModel()
        val page = Page(id = "page1", ownerId = "owner", title = "Test", components = emptyList())
        cartItemsFlow.value = listOf(CartItem(Product(id = "p1", name = "Test", price = 10.0), 1))
        coEvery { orderRepository.createMercadoPagoCheckout(any(), any()) } returns Result.success("https://mp")

        viewModel.createMercadoPagoCheckout(page)
        advanceUntilIdle()

        assertEquals("https://mp", viewModel.paymentUrl.value)
    }

    @Test
    fun `saveCustomerName should call repository`() = runTest {
        val viewModel = createViewModel()
        viewModel.saveCustomerName("Victor")
        advanceUntilIdle()
        
        coVerify { customerRepository.saveName("Victor") }
    }

    @Test
    fun `saveCustomerPhone should call repository`() = runTest {
        val viewModel = createViewModel()
        viewModel.saveCustomerPhone("11999999999")
        advanceUntilIdle()
        
        coVerify { customerRepository.savePhone("11999999999") }
    }

    @Test
    fun `clearPaymentUrl should set paymentUrl to null`() = runTest {
        val viewModel = createViewModel()
        val page = Page(id = "page1", ownerId = "owner", title = "Test", components = emptyList())
        cartItemsFlow.value = listOf(CartItem(Product(id = "p1", name = "Test", price = 10.0), 1))
        coEvery { orderRepository.createMercadoPagoCheckout(any(), any()) } returns Result.success("https://mp")

        viewModel.createMercadoPagoCheckout(page)
        advanceUntilIdle()
        assertEquals("https://mp", viewModel.paymentUrl.value)

        viewModel.clearPaymentUrl()
        assertNull(viewModel.paymentUrl.value)
    }

    @Test
    fun `saveDraft should call repository`() = runTest {
        val viewModel = createViewModel()
        val page = Page(id = "page1", ownerId = "user", title = "Draft", components = emptyList())
        
        viewModel.saveDraft(page)
        
        coVerify { pageDraftRepository.saveDraft(page) }
    }

    @Test
    fun `clearDraft should call repository`() = runTest {
        val viewModel = createViewModel()
        
        viewModel.clearDraft("page1")
        
        coVerify { pageDraftRepository.clearDraft("page1") }
    }

    @Test
    fun `signIn should call authRepository and loadPages on success`() = runTest {
        val viewModel = createViewModel()
        val pages = listOf(Page(id = "1", ownerId = "u1", title = "Loja", components = emptyList()))
        
        coEvery { authRepository.signIn("email@test.com", "pass123") } returns Result.success("token_123")
        coEvery { getPagesUseCase("token_123") } returns pages
        
        var successCalled = false
        var failureMessage: String? = null
        
        viewModel.signIn("email@test.com", "pass123", 
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it }
        )
        advanceUntilIdle()
        
        assertTrue(successCalled)
        assertNull(failureMessage)
        coVerify { authRepository.signIn("email@test.com", "pass123") }
        coVerify { getPagesUseCase("token_123") }
    }

    @Test
    fun `signIn should call onFailure when authentication fails`() = runTest {
        val viewModel = createViewModel()
        
        coEvery { authRepository.signIn("wrong@email.com", "wrong") } returns Result.failure(Exception("Invalid credentials"))
        
        var successCalled = false
        var failureMessage: String? = null
        
        viewModel.signIn("wrong@email.com", "wrong",
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it }
        )
        advanceUntilIdle()
        
        assertFalse(successCalled)
        assertNotNull(failureMessage)
    }

    @Test
    fun `signOut should call authRepository`() = runTest {
        val viewModel = createViewModel()
        coEvery { authRepository.signOut() } returns Result.success(Unit)
        
        viewModel.signOut()
        advanceUntilIdle()
        
        coVerify { authRepository.signOut() }
    }

    @Test
    fun `getCurrentUserToken should return token from repository`() = runTest {
        val viewModel = createViewModel()
        
        val token = viewModel.getCurrentUserToken()
        
        assertEquals("token_123", token)
    }

    @Test
    fun `getDraft should return page from repository`() = runTest {
        val viewModel = createViewModel()
        val page = Page(id = "page1", ownerId = "user", title = "Draft", components = emptyList())
        coEvery { pageDraftRepository.getDraft("page1") } returns page
        
        val result = viewModel.getDraft("page1")
        
        assertEquals(page, result)
        coVerify { pageDraftRepository.getDraft("page1") }
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
