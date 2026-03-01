package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Product
import org.junit.Assert.*
import org.junit.Test

class CartScreenStateTest {

    @Test
    fun `default state should have empty values`() {
        val state = CartScreenState()

        assertTrue(state.cartItems.isEmpty())
        assertEquals(0.0, state.total, 0.001)
        assertEquals("", state.customerName)
        assertEquals("", state.customerPhone)
        assertFalse(state.isLoading)
        assertFalse(state.isCreatingCheckout)
        assertNull(state.paymentUrl)
    }

    @Test
    fun `isCheckoutEnabled should be false when cart is empty`() {
        val state = CartScreenState(
            cartItems = emptyList(),
            customerName = "John Doe",
            customerPhone = "11999999999"
        )

        assertFalse(state.isCheckoutEnabled)
    }

    @Test
    fun `isCheckoutEnabled should be false when customer name is blank`() {
        val state = CartScreenState(
            cartItems = listOf(CartItem(Product(id = "1", name = "Product", price = 10.0), quantity = 1)),
            customerName = "",
            customerPhone = "11999999999"
        )

        assertFalse(state.isCheckoutEnabled)
    }

    @Test
    fun `isCheckoutEnabled should be false when customer phone is too short`() {
        val state = CartScreenState(
            cartItems = listOf(CartItem(Product(id = "1", name = "Product", price = 10.0), quantity = 1)),
            customerName = "John Doe",
            customerPhone = "1234567"
        )

        assertFalse(state.isCheckoutEnabled)
    }

    @Test
    fun `isCheckoutEnabled should be false when isLoading is true`() {
        val state = CartScreenState(
            cartItems = listOf(CartItem(Product(id = "1", name = "Product", price = 10.0), quantity = 1)),
            customerName = "John Doe",
            customerPhone = "11999999999",
            isLoading = true
        )

        assertFalse(state.isCheckoutEnabled)
    }

    @Test
    fun `isCheckoutEnabled should be true with valid data`() {
        val state = CartScreenState(
            cartItems = listOf(CartItem(Product(id = "1", name = "Product", price = 10.0), quantity = 1)),
            customerName = "John Doe",
            customerPhone = "11999999999",
            isLoading = false
        )

        assertTrue(state.isCheckoutEnabled)
    }

    @Test
    fun `isCheckoutEnabled should be true with phone exactly 8 characters`() {
        val state = CartScreenState(
            cartItems = listOf(CartItem(Product(id = "1", name = "Product", price = 10.0), quantity = 1)),
            customerName = "John Doe",
            customerPhone = "12345678"
        )

        assertTrue(state.isCheckoutEnabled)
    }

    @Test
    fun `state copy should preserve values when not specified`() {
        val original = CartScreenState(
            cartItems = listOf(CartItem(Product(id = "1", name = "Product", price = 10.0), quantity = 1)),
            total = 10.0,
            customerName = "John Doe",
            customerPhone = "11999999999"
        )

        val copy = original.copy(isLoading = true)

        assertEquals(original.cartItems, copy.cartItems)
        assertEquals(original.total, copy.total, 0.001)
        assertEquals(original.customerName, copy.customerName)
        assertEquals(original.customerPhone, copy.customerPhone)
        assertTrue(copy.isLoading)
    }

    @Test
    fun `CartScreenEvent subclasses should contain correct data`() {
        val updateEvent = CartScreenEvent.OnUpdateQuantity("product1", 5)
        assertEquals("product1", updateEvent.productId)
        assertEquals(5, updateEvent.newQuantity)

        val removeEvent = CartScreenEvent.OnRemoveItem("product2")
        assertEquals("product2", removeEvent.productId)

        val nameEvent = CartScreenEvent.OnCustomerNameChanged("Jane Doe")
        assertEquals("Jane Doe", nameEvent.name)

        val phoneEvent = CartScreenEvent.OnCustomerPhoneChanged("11888888888")
        assertEquals("11888888888", phoneEvent.phone)
    }
}
