package com.itbenevides.genesys21.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class OrderTest {

    @Test
    fun order_should_create_with_default_values() {
        val order = Order(
            id = "order1",
            userId = "user1",
            items = emptyList(),
            total = 100.0,
            createdAt = 1234567890L
        )

        assertEquals("order1", order.id)
        assertEquals("user1", order.userId)
        assertEquals(OrderStatus.PENDING, order.status)
        assertEquals(PageThemeConfig.ROYAL, order.theme)
        assertEquals(null, order.customerId)
        assertEquals(null, order.customerName)
        assertEquals(null, order.customerPhone)
    }

    @Test
    fun order_should_create_with_all_values() {
        val items = listOf(CartItem(Product(id = "p1", name = "Test", price = 10.0), 2))
        val order = Order(
            id = "order2",
            userId = "user2",
            customerId = "customer1",
            customerName = "Victor",
            customerPhone = "11999999999",
            items = items,
            total = 20.0,
            status = OrderStatus.COMPLETED,
            createdAt = 1234567890L,
            whatsappContact = "5511999999999",
            theme = PageThemeConfig.OCEAN
        )

        assertEquals("order2", order.id)
        assertEquals("user2", order.userId)
        assertEquals("customer1", order.customerId)
        assertEquals("Victor", order.customerName)
        assertEquals("11999999999", order.customerPhone)
        assertEquals(items, order.items)
        assertEquals(20.0, order.total)
        assertEquals(OrderStatus.COMPLETED, order.status)
        assertEquals("5511999999999", order.whatsappContact)
        assertEquals(PageThemeConfig.OCEAN, order.theme)
    }
}

class OrderStatusTest {

    @Test
    fun orderStatus_should_have_all_values() {
        val statuses = OrderStatus.entries

        assertEquals(6, statuses.size)
        assertTrue(statuses.contains(OrderStatus.PENDING))
        assertTrue(statuses.contains(OrderStatus.PAYMENT_PENDING))
        assertTrue(statuses.contains(OrderStatus.PROCESSING))
        assertTrue(statuses.contains(OrderStatus.COMPLETED))
        assertTrue(statuses.contains(OrderStatus.CANCELLED))
        assertTrue(statuses.contains(OrderStatus.FAILED))
    }

    @Test
    fun orderStatus_should_have_correct_ordinal() {
        assertEquals(0, OrderStatus.PENDING.ordinal)
        assertEquals(1, OrderStatus.PAYMENT_PENDING.ordinal)
        assertEquals(2, OrderStatus.PROCESSING.ordinal)
        assertEquals(3, OrderStatus.COMPLETED.ordinal)
        assertEquals(4, OrderStatus.CANCELLED.ordinal)
        assertEquals(5, OrderStatus.FAILED.ordinal)
    }
}
