package com.itbenevides.genesys21.navigation

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product

sealed class Route {
    data object Splash : Route()
    data object Login : Route()
    data object PageList : Route()
    data class PageEditor(val page: Page?) : Route()
    data class WhiteLabel(val page: Page) : Route()
    data class PublicViewer(val page: Page) : Route()
    data class ProductDetails(val product: Product, val fromRoute: Route) : Route()
    data class Cart(val page: Page?) : Route()
    data class OrderTracking(val orderId: String) : Route()
    data object CustomerOrderHistory : Route()
    data class ProductEditor(val page: Page, val product: Product?, val componentIndex: Int?) : Route()
}
