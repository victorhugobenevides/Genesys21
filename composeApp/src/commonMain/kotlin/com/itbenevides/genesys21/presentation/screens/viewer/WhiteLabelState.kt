package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.Product

/**
 * UI State para a tela de Editor Administrativo (WhiteLabel).
 */
data class WhiteLabelState(
    val page: Page,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false, // ADICIONADO: Controle de upload de imagem
    val availableProducts: List<Product> = emptyList(),
    val allAvailableCategories: List<String> = emptyList(),
    val showCatalog: Boolean = false,
    val showThemeSelector: Boolean = false,
    val showPageSettings: Boolean = false,
    val editingComponentIndex: Int? = null,
    val pendingNewComponent: PageComponent? = null,
    val filterQuery: String = "",
    val userPages: List<Page> = emptyList()
)

/**
 * UI Intents (Eventos) para a tela de Editor Administrativo.
 */
sealed class WhiteLabelEvent {
    data class OnPageUpdated(val newPage: Page) : WhiteLabelEvent()
    object OnPublishClicked : WhiteLabelEvent()
    object OnBackClicked : WhiteLabelEvent()
    data class OnEditProductClicked(val product: Product?, val componentIndex: Int?) : WhiteLabelEvent()
    
    // UI Toggles
    data class OnShowCatalogChanged(val show: Boolean) : WhiteLabelEvent()
    data class OnShowThemeSelectorChanged(val show: Boolean) : WhiteLabelEvent()
    data class OnShowPageSettingsChanged(val show: Boolean) : WhiteLabelEvent()
    data class OnEditingComponentIndexChanged(val index: Int?) : WhiteLabelEvent()
    data class OnPendingNewComponentChanged(val component: PageComponent?) : WhiteLabelEvent()
    data class OnFilterQueryChanged(val query: String) : WhiteLabelEvent()
    
    // Upload de Imagem
    data class OnImageUploadStarted(val isUploading: Boolean) : WhiteLabelEvent()
    
    // Ações de Componentes
    data class OnDeleteComponent(val index: Int) : WhiteLabelEvent()
    data class OnDuplicateComponent(val index: Int) : WhiteLabelEvent()
    data class OnMoveComponentUp(val index: Int) : WhiteLabelEvent()
    data class OnMoveComponentDown(val index: Int) : WhiteLabelEvent()
}
