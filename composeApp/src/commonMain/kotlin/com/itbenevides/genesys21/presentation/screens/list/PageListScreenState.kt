package com.itbenevides.genesys21.presentation.screens.list

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.PageTemplateType

/**
 * UI State: Tudo o que a lista de administração pode exibir.
 */
data class PageListState(
    val pages: List<Page> = emptyList(),
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val pendingOrdersCount: Int = 0,
    val searchQuery: String = "",
    val selectedStatusFilter: OrderStatus? = null,
    val showCreateDialog: Boolean = false,
    val showGlobalSettings: Boolean = false,
    val showRenameDialog: Boolean = false,
    val pageToRename: Page? = null,
    val newPageTitle: String = ""
)

/**
 * UI Events: Todas as interações possíveis na administração.
 */
sealed class PageListEvent {
    data class OnTabSelected(val index: Int) : PageListEvent()
    data class OnSearchQueryChanged(val query: String) : PageListEvent()
    data class OnStatusFilterSelected(val status: OrderStatus?) : PageListEvent()
    object OnCreatePageClicked : PageListEvent()
    data class OnNewPageTitleChanged(val title: String) : PageListEvent()
    
    // Usa o PageTemplateType centralizado do domínio
    data class OnConfirmCreatePage(val templateType: PageTemplateType) : PageListEvent()
    
    object OnDismissCreateDialog : PageListEvent()
    object OnGlobalSettingsClicked : PageListEvent()
    object OnDismissGlobalSettings : PageListEvent()
    data class OnConfirmGlobalSettings(val domain: String, val whatsapp: String) : PageListEvent()
    
    data class OnRenamePageClicked(val page: Page) : PageListEvent()
    object OnDismissRenameDialog : PageListEvent()
    data class OnConfirmRenamePage(val newTitle: String) : PageListEvent()

    data class OnDeletePageClicked(val pageId: String) : PageListEvent()
    data class OnUpdateOrderStatus(val orderId: String, val newStatus: OrderStatus) : PageListEvent()
    object OnLogoutClicked : PageListEvent()
    
    data class OnExportPageClicked(val page: Page) : PageListEvent()
    object OnExportAllClicked : PageListEvent()
    data class OnImportPageClicked(val json: String) : PageListEvent()
}
