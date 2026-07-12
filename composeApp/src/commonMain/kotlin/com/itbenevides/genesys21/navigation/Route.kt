package com.itbenevides.genesys21.navigation

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product

sealed class Route {
    object Splash : Route()

    object Login : Route()

    object PageList : Route()

    data class PageEditor(val page: Page? = null) : Route()

    data class WhiteLabel(val page: Page) : Route()

    data class PublicViewer(val page: Page) : Route()

    data class ProductDetails(val product: Product, val fromRoute: Route) : Route()

    data class ProductEditor(val page: Page, val product: Product? = null, val componentIndex: Int? = null) : Route()

    data class Cart(val page: Page?) : Route()

    data class OrderTracking(val orderId: String) : Route()

    data class CustomerOrderHistory(val page: Page?) : Route()

    object DesignSystemShowcase : Route()

    object EditorShowcase : Route()
}
