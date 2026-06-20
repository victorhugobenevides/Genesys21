package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Product

/**
 * UI State para a tela de Detalhes do Produto.
 */
data class ProductDetailsState(
    val product: Product,
    val isAddingToCart: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val error: String? = null,
)

/**
 * UI Intents (Eventos) para a tela de Detalhes do Produto.
 */
sealed class ProductDetailsEvent {
    object OnAddToCartClicked : ProductDetailsEvent()

    object OnDismissSuccessDialog : ProductDetailsEvent()

    object OnViewCartClicked : ProductDetailsEvent()

    object OnContinueShoppingClicked : ProductDetailsEvent()

    object OnContactSellerClicked : ProductDetailsEvent()

    object OnBackClicked : ProductDetailsEvent()
}
