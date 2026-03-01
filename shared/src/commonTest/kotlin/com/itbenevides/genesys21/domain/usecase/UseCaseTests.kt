package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.mocks.FakeOrderRepository
import com.itbenevides.genesys21.mocks.FakePageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class UseCaseTests {

    private val pageRepository = FakePageRepository()
    private val orderRepository = FakeOrderRepository()
    
    private lateinit var getPagesUseCase: GetPagesUseCase
    private lateinit var savePageUseCase: SavePageUseCase
    private lateinit var deletePageUseCase: DeletePageUseCase
    private lateinit var getPublicPageUseCase: GetPublicPageUseCase
    private lateinit var getPageByDomainUseCase: GetPageByDomainUseCase
    
    private lateinit var submitOrderUseCase: SubmitOrderUseCase
    private lateinit var getOrdersUseCase: GetOrdersUseCase
    private lateinit var getOrderByIdUseCase: GetOrderByIdUseCase
    private lateinit var getCustomerOrdersUseCase: GetCustomerOrdersUseCase
    private lateinit var updateOrderStatusUseCase: UpdateOrderStatusUseCase

    private lateinit var getFirstPublicPageUseCase: GetFirstPublicPageUseCase
    private lateinit var uploadImageUseCase: UploadImageUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var saveCategoryUseCase: SaveCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase

    @BeforeTest
    fun setup() {
        pageRepository.clear()
        orderRepository.clear()
        
        getPagesUseCase = GetPagesUseCase(pageRepository)
        savePageUseCase = SavePageUseCase(pageRepository)
        deletePageUseCase = DeletePageUseCase(pageRepository)
        getPublicPageUseCase = GetPublicPageUseCase(pageRepository)
        getPageByDomainUseCase = GetPageByDomainUseCase(pageRepository)
        getFirstPublicPageUseCase = GetFirstPublicPageUseCase(pageRepository)
        uploadImageUseCase = UploadImageUseCase(pageRepository)
        getCategoriesUseCase = GetCategoriesUseCase(pageRepository)
        saveCategoryUseCase = SaveCategoryUseCase(pageRepository)
        deleteCategoryUseCase = DeleteCategoryUseCase(pageRepository)
        
        submitOrderUseCase = SubmitOrderUseCase(orderRepository)
        getOrdersUseCase = GetOrdersUseCase(orderRepository)
        getOrderByIdUseCase = GetOrderByIdUseCase(orderRepository)
        getCustomerOrdersUseCase = GetCustomerOrdersUseCase(orderRepository)
        updateOrderStatusUseCase = UpdateOrderStatusUseCase(orderRepository)
    }

    // ==================== PAGE USE CASES ====================

    @Test
    fun getPagesUseCase_should_return_list_from_repository() = runTest {
        val testPage = Page(id = "1", title = "Teste", ownerId = "token")
        pageRepository.savePage(testPage, "token", isEditing = false)
        
        val result = getPagesUseCase("token")
        
        assertEquals(1, result.size)
        assertEquals("Teste", result[0].title)
    }

    @Test
    fun getPagesUseCase_should_return_empty_list_when_no_pages() = runTest {
        val result = getPagesUseCase("token")
        assertEquals(0, result.size)
    }

    @Test
    fun getPagesUseCase_should_only_return_pages_for_specific_token() = runTest {
        val page1 = Page(id = "1", title = "Page 1", ownerId = "token1")
        val page2 = Page(id = "2", title = "Page 2", ownerId = "token2")
        pageRepository.savePage(page1, "token1", isEditing = false)
        pageRepository.savePage(page2, "token2", isEditing = false)
        
        val result = getPagesUseCase("token1")
        
        assertEquals(1, result.size)
        assertEquals("Page 1", result[0].title)
    }

    @Test
    fun savePageUseCase_should_call_save_when_isEditing_is_false() = runTest {
        val page = Page(id = "new", title = "Nova Página", ownerId = "token")
        val result = savePageUseCase(page, "token", isEditing = false)
        
        assertTrue(result.isSuccess)
        val pages = getPagesUseCase("token")
        assertEquals(1, pages.size)
    }

    @Test
    fun savePageUseCase_should_call_update_when_isEditing_is_true() = runTest {
        val page = Page(id = "1", title = "Original", ownerId = "token")
        pageRepository.savePage(page, "token", isEditing = false)
        
        val updatedPage = page.copy(title = "Editada")
        val result = savePageUseCase(updatedPage, "token", isEditing = true)
        
        assertTrue(result.isSuccess)
        val pages = getPagesUseCase("token")
        assertEquals("Editada", pages[0].title)
    }

    @Test
    fun deletePageUseCase_should_remove_page_from_repository() = runTest {
        val page = Page(id = "1", title = "To Delete", ownerId = "token")
        pageRepository.savePage(page, "token", isEditing = false)
        
        val result = deletePageUseCase("1", "token")
        
        assertTrue(result.isSuccess)
        val pages = getPagesUseCase("token")
        assertEquals(0, pages.size)
    }

    @Test
    fun getPublicPageUseCase_should_return_page_when_found() = runTest {
        val page = Page(id = "public1", title = "Public Page", ownerId = "token")
        pageRepository.savePage(page, "token", isEditing = false)
        
        val result = getPublicPageUseCase("public1")
        
        assertTrue(result.isSuccess)
        assertEquals("Public Page", result.getOrNull()?.title)
    }

    @Test
    fun getPublicPageUseCase_should_return_failure_when_not_found() = runTest {
        val result = getPublicPageUseCase("nonexistent")
        assertFalse(result.isSuccess)
    }

    @Test
    fun getPageByDomainUseCase_should_return_page_when_domain_matches() = runTest {
        val page = Page(
            id = "1", 
            title = "Domain Page", 
            ownerId = "token",
            customDomain = "mydomain.com"
        )
        pageRepository.savePage(page, "token", isEditing = false)
        
        val result = getPageByDomainUseCase("mydomain.com")
        
        assertTrue(result.isSuccess)
        assertEquals("Domain Page", result.getOrNull()?.title)
    }

    // ==================== ORDER USE CASES ====================

    @Test
    fun submitOrderUseCase_should_create_order_and_return_id() = runTest {
        val order = Order(
            id = "order1",
            userId = "user1",
            items = emptyList(),
            total = 100.0,
            status = OrderStatus.PENDING,
            createdAt = 1234567890L
        )
        
        val result = submitOrderUseCase(order)
        
        assertTrue(result.isSuccess)
        assertEquals("order1", result.getOrNull())
    }

    @Test
    fun getOrdersUseCase_should_return_flow_of_orders() = runTest {
        val order = Order(
            id = "order1",
            userId = "user1",
            items = emptyList(),
            total = 100.0,
            status = OrderStatus.PENDING,
            createdAt = 1234567890L
        )
        orderRepository.addOrder(order)
        
        val orders = getOrdersUseCase("token").first()
        
        assertEquals(1, orders.size)
        assertEquals("order1", orders[0].id)
    }

    @Test
    fun getOrderByIdUseCase_should_return_order_when_found() = runTest {
        val order = Order(
            id = "order1",
            userId = "user1",
            items = emptyList(),
            total = 100.0,
            status = OrderStatus.PENDING,
            createdAt = 1234567890L
        )
        orderRepository.addOrder(order)
        
        val result = getOrderByIdUseCase("order1")
        
        assertTrue(result.isSuccess)
        assertEquals("order1", result.getOrNull()?.id)
    }

    @Test
    fun getOrderByIdUseCase_should_return_failure_when_not_found() = runTest {
        val result = getOrderByIdUseCase("nonexistent")
        assertFalse(result.isSuccess)
    }

    @Test
    fun getCustomerOrdersUseCase_should_return_orders_for_customer() = runTest {
        val order1 = Order(
            id = "order1",
            userId = "user1",
            customerId = "session1",
            items = emptyList(),
            total = 100.0,
            status = OrderStatus.PENDING,
            createdAt = 1234567890L
        )
        val order2 = Order(
            id = "order2",
            userId = "user1",
            customerId = "session2",
            items = emptyList(),
            total = 200.0,
            status = OrderStatus.COMPLETED,
            createdAt = 1234567890L
        )
        orderRepository.addOrder(order1)
        orderRepository.addOrder(order2)
        
        val result = getCustomerOrdersUseCase("session1")
        
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("order1", result.getOrNull()?.get(0)?.id)
    }

    @Test
    fun updateOrderStatusUseCase_should_update_status_when_order_exists() = runTest {
        val order = Order(
            id = "order1",
            userId = "user1",
            items = emptyList(),
            total = 100.0,
            status = OrderStatus.PENDING,
            createdAt = 1234567890L
        )
        orderRepository.addOrder(order)
        
        val result = updateOrderStatusUseCase("token", "order1", OrderStatus.PROCESSING)
        
        assertTrue(result.isSuccess)
        val updatedOrder = getOrderByIdUseCase("order1").getOrNull()
        assertEquals(OrderStatus.PROCESSING, updatedOrder?.status)
    }

    @Test
    fun updateOrderStatusUseCase_should_return_failure_when_order_not_found() = runTest {
        val result = updateOrderStatusUseCase("token", "nonexistent", OrderStatus.PROCESSING)
        assertFalse(result.isSuccess)
    }

    // ==================== ADDITIONAL PAGE USE CASES ====================

    @Test
    fun getFirstPublicPageUseCase_should_return_first_page_when_exists() = runTest {
        val page = Page(id = "1", title = "First Page", ownerId = "")
        pageRepository.savePage(page, "", isEditing = false)
        
        val result = getFirstPublicPageUseCase()
        
        assertNotNull(result)
        assertEquals("First Page", result.title)
}

    @Test
    fun getFirstPublicPageUseCase_should_return_null_when_no_pages() = runTest {
        val result = getFirstPublicPageUseCase()
        assertNull(result)
    }

    @Test
    fun uploadImageUseCase_should_return_url_on_success() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        val fileName = "test.jpg"
        
        val result = uploadImageUseCase(bytes, fileName, "token")
        
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("test.jpg") ?: false)
    }

    // ==================== CATEGORY USE CASES ====================

    @Test
    fun getCategoriesUseCase_should_return_list_from_repository() = runTest {
        val category = Category(id = 1, ownerId = "token", name = "Test Category")
        pageRepository.saveCategory(category, "token")
        
        val result = getCategoriesUseCase("token")
        
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Test Category", result.getOrNull()?.get(0)?.name)
    }

    @Test
    fun saveCategoryUseCase_should_add_category_to_repository() = runTest {
        val category = Category(id = 1, ownerId = "token", name = "New Category")
        
        val result = saveCategoryUseCase(category, "token")
        
        assertTrue(result.isSuccess)
        val categories = getCategoriesUseCase("token").getOrNull()
        assertEquals(1, categories?.size)
    }

    @Test
    fun deleteCategoryUseCase_should_remove_category_from_repository() = runTest {
        val category = Category(id = 1, ownerId = "token", name = "To Delete")
        pageRepository.saveCategory(category, "token")
        
        val result = deleteCategoryUseCase(1, "token")
        
        assertTrue(result.isSuccess)
        val categories = getCategoriesUseCase("token").getOrNull()
        assertEquals(0, categories?.size)
    }
}
