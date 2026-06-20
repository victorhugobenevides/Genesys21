package com.itbenevides.genesys21.util

interface Analytics {
    fun logEvent(
        name: String,
        params: Map<String, Any> = emptyMap(),
    )

    fun trackPageView(pageName: String)

    fun recordError(
        title: String,
        throwable: Throwable? = null,
        extraParams: Map<String, String> = emptyMap(),
    )
}

// Funções de Funil Unificadas
fun Analytics.trackViewProduct(
    id: String,
    name: String,
    price: Double,
) {
    logEvent(
        "view_item",
        mapOf(
            "item_id" to id,
            "item_name" to name,
            "price" to price,
        ),
    )
}

fun Analytics.trackAddToCart(
    id: String,
    name: String,
    price: Double,
    quantity: Int = 1,
) {
    logEvent(
        "add_to_cart",
        mapOf(
            "item_id" to id,
            "item_name" to name,
            "price" to price,
            "quantity" to quantity,
        ),
    )
}

fun Analytics.trackInitiateCheckout(total: Double) {
    logEvent("begin_checkout", mapOf("value" to total))
}

fun Analytics.trackPurchase(
    orderId: String,
    total: Double,
) {
    logEvent(
        "purchase",
        mapOf(
            "transaction_id" to orderId,
            "value" to total,
            "currency" to "BRL",
        ),
    )
}

// Singleton global para acesso simplificado
expect val AnalyticsManager: Analytics
