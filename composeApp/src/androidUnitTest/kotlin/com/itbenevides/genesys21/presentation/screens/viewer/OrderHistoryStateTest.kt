package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import org.junit.Assert.*
import org.junit.Test

class OrderHistoryStateTest {

    @Test
    fun `default state should have empty orders and not loading`() {
        val state = OrderHistoryState()

        assertTrue(state.orders.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `state should contain orders when initialized with data`() {
        val orders = listOf(
            Order(
                id = "1",
                userId = "user1",
                customerName = "John",
                items = emptyList(),
                total = 100.0,
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis()
            ),
            Order(
                id = "2",
                userId = "user1",
                customerName = "Jane",
                items = emptyList(),
                total = 200.0,
                status = OrderStatus.COMPLETED,
                createdAt = System.currentTimeMillis()
            )
        )
        val state = OrderHistoryState(orders = orders, isLoading = false)

        assertEquals(2, state.orders.size)
        assertEquals("1", state.orders[0].id)
        assertEquals("2", state.orders[1].id)
    }

    @Test
    fun `state copy should update loading state`() {
        val original = OrderHistoryState(orders = emptyList(), isLoading = false)
        val updated = original.copy(isLoading = true)

        assertTrue(updated.isLoading)
        assertEquals(original.orders, updated.orders)
    }

    @Test
    fun `OrderHistoryEvent OnOrderClicked should contain order data`() {
        val order = Order(
            id = "123",
            userId = "user1",
            customerName = "Test",
            items = emptyList(),
            total = 50.0,
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        val event = OrderHistoryEvent.OnOrderClicked(order)

        assertEquals(order, event.order)
        assertEquals("123", event.order.id)
    }

    @Test
    fun `OrderHistoryEvent OnBackClicked should be singleton`() {
        val event1 = OrderHistoryEvent.OnBackClicked
        val event2 = OrderHistoryEvent.OnBackClicked

        assertSame(event1, event2)
    }
}
