package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Product
import org.junit.Assert.*
import org.junit.Test

class ProductDetailsStateTest {

    @Test
    fun `default state should have correct initial values`() {
        val product = Product(id = "p1", name = "Test", price = 10.0)
        val state = ProductDetailsState(product = product)

        assertEquals(product, state.product)
        assertFalse(state.isAddingToCart)
        assertFalse(state.showSuccessDialog)
        assertNull(state.error)
    }

    @Test
    fun `state copy should update values correctly`() {
        val product = Product(id = "p1", name = "Test", price = 10.0)
        val original = ProductDetailsState(product = product)
        
        val updated = original.copy(
            isAddingToCart = true,
            showSuccessDialog = true,
            error = "Some error"
        )

        assertTrue(updated.isAddingToCart)
        assertTrue(updated.showSuccessDialog)
        assertEquals("Some error", updated.error)
        assertEquals(product, updated.product) // unchanged
    }

    @Test
    fun `state copy can clear error`() {
        val product = Product(id = "p1", name = "Test", price = 10.0)
        val original = ProductDetailsState(product = product, error = "Error")
        
        val updated = original.copy(error = null)

        assertNull(updated.error)
    }

    @Test
    fun `ProductDetailsEvent singletons should be same instance`() {
        val event1 = ProductDetailsEvent.OnAddToCartClicked
        val event2 = ProductDetailsEvent.OnAddToCartClicked
        assertSame(event1, event2)

        val event3 = ProductDetailsEvent.OnDismissSuccessDialog
        val event4 = ProductDetailsEvent.OnDismissSuccessDialog
        assertSame(event3, event4)

        val event5 = ProductDetailsEvent.OnViewCartClicked
        val event6 = ProductDetailsEvent.OnViewCartClicked
        assertSame(event5, event6)

        val event7 = ProductDetailsEvent.OnContinueShoppingClicked
        val event8 = ProductDetailsEvent.OnContinueShoppingClicked
        assertSame(event7, event8)

        val event9 = ProductDetailsEvent.OnBackClicked
        val event10 = ProductDetailsEvent.OnBackClicked
        assertSame(event9, event10)
    }
}
