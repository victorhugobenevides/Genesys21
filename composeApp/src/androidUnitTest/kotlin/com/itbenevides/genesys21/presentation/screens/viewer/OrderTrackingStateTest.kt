package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import org.junit.Assert.*
import org.junit.Test

class OrderTrackingStateTest {

    @Test
    fun `default state should have correct initial values`() {
        val state = OrderTrackingState()

        assertNull(state.order)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `state with order should contain order data`() {
        val order = Order(
            id = "order123",
            userId = "user1",
            items = emptyList(),
            total = 150.0,
            status = OrderStatus.PENDING,
            createdAt = 1234567890L
        )
        val state = OrderTrackingState(order = order)

        assertEquals(order, state.order)
        assertEquals("order123", state.order?.id)
        assertEquals(OrderStatus.PENDING, state.order?.status)
    }

    @Test
    fun `state copy should update values correctly`() {
        val order = Order(
            id = "order123",
            userId = "user1",
            items = emptyList(),
            total = 150.0,
            status = OrderStatus.PENDING,
            createdAt = 1234567890L
        )
        val original = OrderTrackingState()
        
        val updated = original.copy(
            order = order,
            isLoading = true,
            error = "Some error"
        )

        assertEquals(order, updated.order)
        assertTrue(updated.isLoading)
        assertEquals("Some error", updated.error)
    }

    @Test
    fun `state copy can clear error`() {
        val original = OrderTrackingState(error = "Error")
        
        val updated = original.copy(error = null)

        assertNull(updated.error)
    }

    @Test
    fun `OrderTrackingEvent OnTrackOrder should contain orderId`() {
        val event = OrderTrackingEvent.OnTrackOrder("order456")
        assertEquals("order456", event.orderId)
    }

    @Test
    fun `OrderTrackingEvent singletons should be same instance`() {
        val event1 = OrderTrackingEvent.OnCopyOrderIdClicked
        val event2 = OrderTrackingEvent.OnCopyOrderIdClicked
        assertSame(event1, event2)

        val event3 = OrderTrackingEvent.OnBackClicked
        val event4 = OrderTrackingEvent.OnBackClicked
        assertSame(event3, event4)
    }
}
