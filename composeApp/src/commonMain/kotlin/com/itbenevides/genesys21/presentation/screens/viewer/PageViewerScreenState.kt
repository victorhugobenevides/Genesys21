package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product

/**
 * UI State para a tela de Vitrine Pública.
 */
data class PageViewerScreenState(
    val page: Page,
    val filterQuery: String = "",
    val isLoggedIn: Boolean = false,
    val cartCount: Int = 0,
    val isLoading: Boolean = false,
    val allStoreCategories: List<String> = emptyList(),
) {
    val hasProductList: Boolean = page.components.any { it is PageComponent.ProductList }
}

/**
 * UI Intents (Eventos) para a tela de Vitrine Pública.
 */
sealed class PageViewerScreenEvent {
    data class OnFilterQueryChanged(val query: String) : PageViewerScreenEvent()

    data class OnProductClicked(val product: Product) : PageViewerScreenEvent()

    object OnOpenCartClicked : PageViewerScreenEvent()

    object OnOpenHistoryClicked : PageViewerScreenEvent()

    object OnOpenAdminSettingsClicked : PageViewerScreenEvent()

    object OnBackClicked : PageViewerScreenEvent()
}
