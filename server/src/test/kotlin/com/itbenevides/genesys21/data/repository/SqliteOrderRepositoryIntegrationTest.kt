package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.TestDatabaseFactory
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SqliteOrderRepositoryIntegrationTest {
    private lateinit var repository: SqliteOrderRepository

    @BeforeTest
    fun setup() {
        TestDatabaseFactory.init()
        repository = SqliteOrderRepository()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanup()
    }

    @Test
    fun testCreateAndGetOrder() = runBlocking {
        val order = Order(
            id = "order-1",
            userId = "user-1",
            customerId = "cust-1",
            customerName = "Customer",
            customerPhone = "5511999999999",
            items = listOf(
                CartItem(
                    product = Product(id = "prod-1", name = "Product", price = 10.0),
                    quantity = 1
                )
            ),
            total = 10.0,
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )

        val result = repository.createOrder(order)
        assertTrue(result.isSuccess)

        val fetched = repository.getOrderById("order-1").getOrNull()
        assertNotNull(fetched)
        assertEquals("order-1", fetched.id)
        assertEquals(1, fetched.items.size)
    }

    @Test
    fun testGetOrdersFlow() = runBlocking {
        val order = Order(
            id = "order-2",
            userId = "user-1",
            customerId = "cust-1",
            customerName = "Customer",
            customerPhone = "5511999999999",
            items = emptyList(),
            total = 0.0,
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        repository.createOrder(order)

        val orders = repository.getOrders("user-1").first()
        assertEquals(1, orders.size)
    }

    @Test
    fun testGetCustomerOrders() = runBlocking {
        val order = Order(
            id = "order-3",
            userId = "user-1",
            customerId = "cust-xyz",
            customerName = "Customer",
            customerPhone = "5511999999999",
            items = emptyList(),
            total = 0.0,
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        repository.createOrder(order)

        val orders = repository.getCustomerOrders("cust-xyz").getOrNull()
        assertNotNull(orders)
        assertEquals(1, orders.size)
    }

    @Test
    fun testUpdateOrderStatus() = runBlocking {
        val order = Order(
            id = "order-4",
            userId = "user-1",
            customerId = "cust-1",
            customerName = "Customer",
            customerPhone = "5511999999999",
            items = emptyList(),
            total = 0.0,
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        repository.createOrder(order)

        val result = repository.updateOrderStatus("user-1", "order-4", OrderStatus.COMPLETED)
        assertTrue(result.isSuccess)

        val updated = repository.getOrderById("order-4").getOrNull()
        assertEquals(OrderStatus.COMPLETED, updated?.status)
    }
}
