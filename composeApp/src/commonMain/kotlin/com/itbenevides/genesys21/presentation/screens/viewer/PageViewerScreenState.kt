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
    val isLoading: Boolean = false
) {
    // Extrai automaticamente as categorias dos produtos presentes na página
    val categories: List<String> = page.components
        .filterIsInstance<PageComponent.ProductList>()
        .flatMap { it.products }
        .map { it.category }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

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
    object OnAdminSettingsClicked : PageViewerScreenEvent()
    object OnBackClicked : PageViewerScreenEvent()
}
