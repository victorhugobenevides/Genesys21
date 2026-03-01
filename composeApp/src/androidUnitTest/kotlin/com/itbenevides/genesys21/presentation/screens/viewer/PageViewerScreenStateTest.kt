package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import org.junit.Assert.*
import org.junit.Test

class PageViewerScreenStateTest {

    @Test
    fun `default state should have correct initial values`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val state = PageViewerScreenState(page = page)

        assertEquals(page, state.page)
        assertEquals("", state.filterQuery)
        assertFalse(state.isLoggedIn)
        assertEquals(0, state.cartCount)
        assertFalse(state.isLoading)
        assertTrue(state.allStoreCategories.isEmpty())
    }

    @Test
    fun `hasProductList should return false when no ProductList component`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val state = PageViewerScreenState(page = page)

        assertFalse(state.hasProductList)
    }

    @Test
    fun `hasProductList should return true when ProductList component exists`() {
        val productListComponent = PageComponent.ProductList(
            customLabel = "Products",
            products = emptyList()
        )
        val page = Page(
            id = "1", 
            ownerId = "user", 
            title = "Test", 
            components = listOf(productListComponent)
        )
        val state = PageViewerScreenState(page = page)

        assertTrue(state.hasProductList)
    }

    @Test
    fun `state copy should update values correctly`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val original = PageViewerScreenState(page = page)
        
        val updated = original.copy(
            filterQuery = "search",
            isLoggedIn = true,
            cartCount = 5
        )

        assertEquals("search", updated.filterQuery)
        assertTrue(updated.isLoggedIn)
        assertEquals(5, updated.cartCount)
        assertEquals(page, updated.page) // unchanged
    }

    @Test
    fun `PageViewerScreenEvent OnFilterQueryChanged should contain query`() {
        val event = PageViewerScreenEvent.OnFilterQueryChanged("search term")
        assertEquals("search term", event.query)
    }

    @Test
    fun `PageViewerScreenEvent OnProductClicked should contain product`() {
        val product = Product(id = "p1", name = "Test", price = 10.0)
        val event = PageViewerScreenEvent.OnProductClicked(product)
        
        assertEquals(product, event.product)
    }

    @Test
    fun `PageViewerScreenEvent singletons should be same instance`() {
        val event1 = PageViewerScreenEvent.OnOpenCartClicked
        val event2 = PageViewerScreenEvent.OnOpenCartClicked
        assertSame(event1, event2)

        val event3 = PageViewerScreenEvent.OnOpenHistoryClicked
        val event4 = PageViewerScreenEvent.OnOpenHistoryClicked
        assertSame(event3, event4)

        val event5 = PageViewerScreenEvent.OnOpenAdminSettingsClicked
        val event6 = PageViewerScreenEvent.OnOpenAdminSettingsClicked
        assertSame(event5, event6)

        val event7 = PageViewerScreenEvent.OnBackClicked
        val event8 = PageViewerScreenEvent.OnBackClicked
        assertSame(event7, event8)
    }
}
