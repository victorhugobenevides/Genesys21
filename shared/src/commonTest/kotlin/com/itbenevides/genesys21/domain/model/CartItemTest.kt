package com.itbenevides.genesys21.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CartItemTest {

    @Test
    fun `cart item should contain product and quantity`() {
        val product = Product(id = "p1", name = "Test", price = 10.0)
        val cartItem = CartItem(product = product, quantity = 2)

        assertEquals(product, cartItem.product)
        assertEquals(2, cartItem.quantity)
    }

    @Test
    fun `cart item copy should update quantity`() {
        val product = Product(id = "p1", name = "Test", price = 10.0)
        val original = CartItem(product = product, quantity = 1)
        val updated = original.copy(quantity = 5)

        assertEquals(5, updated.quantity)
        assertEquals(product, updated.product)
    }
}
