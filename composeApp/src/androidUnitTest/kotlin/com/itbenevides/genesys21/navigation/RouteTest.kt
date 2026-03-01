package com.itbenevides.genesys21.navigation

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import org.junit.Assert.*
import org.junit.Test

class RouteTest {

    @Test
    fun `Route Splash should be data object`() {
        val route = Route.Splash
        assertTrue(route is Route.Splash)
    }

    @Test
    fun `Route Login should be data object`() {
        val route = Route.Login
        assertTrue(route is Route.Login)
    }

    @Test
    fun `Route PageList should be data object`() {
        val route = Route.PageList
        assertTrue(route is Route.PageList)
    }

    @Test
    fun `Route PageEditor should contain page parameter`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val route = Route.PageEditor(page)
        
        assertTrue(route is Route.PageEditor)
        assertEquals(page, (route as Route.PageEditor).page)
    }

    @Test
    fun `Route PageEditor should accept null page`() {
        val route = Route.PageEditor(null)
        
        assertTrue(route is Route.PageEditor)
        assertNull((route as Route.PageEditor).page)
    }

    @Test
    fun `Route WhiteLabel should contain page`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val route = Route.WhiteLabel(page)
        
        assertTrue(route is Route.WhiteLabel)
        assertEquals(page, (route as Route.WhiteLabel).page)
    }

    @Test
    fun `Route PublicViewer should contain page`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val route = Route.PublicViewer(page)
        
        assertTrue(route is Route.PublicViewer)
        assertEquals(page, (route as Route.PublicViewer).page)
    }

    @Test
    fun `Route ProductDetails should contain product and fromRoute`() {
        val product = Product(id = "p1", name = "Product", price = 10.0)
        val fromRoute = Route.PageList
        val route = Route.ProductDetails(product, fromRoute)
        
        assertTrue(route is Route.ProductDetails)
        assertEquals(product, (route as Route.ProductDetails).product)
        assertEquals(fromRoute, route.fromRoute)
    }

    @Test
    fun `Route Cart should contain nullable page`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val routeWithPage = Route.Cart(page)
        val routeWithoutPage = Route.Cart(null)
        
        assertEquals(page, (routeWithPage as Route.Cart).page)
        assertNull((routeWithoutPage as Route.Cart).page)
    }

    @Test
    fun `Route OrderTracking should contain orderId`() {
        val route = Route.OrderTracking("order123")
        
        assertTrue(route is Route.OrderTracking)
        assertEquals("order123", (route as Route.OrderTracking).orderId)
    }

    @Test
    fun `Route CustomerOrderHistory should be data object`() {
        val route = Route.CustomerOrderHistory
        assertTrue(route is Route.CustomerOrderHistory)
    }

    @Test
    fun `Route ProductEditor should contain page product and componentIndex`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val product = Product(id = "p1", name = "Product", price = 10.0)
        val route = Route.ProductEditor(page, product, 0)
        
        assertTrue(route is Route.ProductEditor)
        assertEquals(page, (route as Route.ProductEditor).page)
        assertEquals(product, route.product)
        assertEquals(0, route.componentIndex)
    }

    @Test
    fun `Route ProductEditor should accept null product and componentIndex`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val route = Route.ProductEditor(page, null, null)
        
        assertNull((route as Route.ProductEditor).product)
        assertNull(route.componentIndex)
    }

    @Test
    fun `Route instances with same values should be equal`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val route1 = Route.WhiteLabel(page)
        val route2 = Route.WhiteLabel(page)
        
        assertEquals(route1, route2)
    }

    @Test
    fun `Route Splash singleton should be same instance`() {
        val route1 = Route.Splash
        val route2 = Route.Splash
        
        assertSame(route1, route2)
    }

    @Test
    fun `Route Login singleton should be same instance`() {
        val route1 = Route.Login
        val route2 = Route.Login
        
        assertSame(route1, route2)
    }

    @Test
    fun `Route PageList singleton should be same instance`() {
        val route1 = Route.PageList
        val route2 = Route.PageList
        
        assertSame(route1, route2)
    }

    @Test
    fun `Route CustomerOrderHistory singleton should be same instance`() {
        val route1 = Route.CustomerOrderHistory
        val route2 = Route.CustomerOrderHistory
        
        assertSame(route1, route2)
    }
}
