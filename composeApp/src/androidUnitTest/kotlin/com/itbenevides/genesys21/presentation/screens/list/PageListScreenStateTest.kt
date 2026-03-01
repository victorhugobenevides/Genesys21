package com.itbenevides.genesys21.presentation.screens.list

import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageTemplateType
import org.junit.Assert.*
import org.junit.Test

class PageListScreenStateTest {

    @Test
    fun `default state should have empty lists and default values`() {
        val state = PageListState()

        assertTrue(state.pages.isEmpty())
        assertTrue(state.orders.isEmpty())
        assertFalse(state.isLoading)
        assertEquals(0, state.selectedTab)
        assertEquals(0, state.pendingOrdersCount)
        assertEquals("", state.searchQuery)
        assertNull(state.selectedStatusFilter)
        assertFalse(state.showCreateDialog)
        assertFalse(state.showGlobalSettings)
        assertFalse(state.showRenameDialog)
        assertNull(state.pageToRename)
        assertEquals("", state.newPageTitle)
    }

    @Test
    fun `state should contain pages when initialized with data`() {
        val pages = listOf(
            Page(id = "1", ownerId = "u1", title = "Page 1", components = emptyList()),
            Page(id = "2", ownerId = "u1", title = "Page 2", components = emptyList())
        )
        val state = PageListState(pages = pages)

        assertEquals(2, state.pages.size)
        assertEquals("Page 1", state.pages[0].title)
    }

    @Test
    fun `state copy should update selected tab`() {
        val original = PageListState(selectedTab = 0)
        val updated = original.copy(selectedTab = 1)

        assertEquals(1, updated.selectedTab)
    }

    @Test
    fun `state copy should update search query`() {
        val original = PageListState(searchQuery = "")
        val updated = original.copy(searchQuery = "test query")

        assertEquals("test query", updated.searchQuery)
    }

    @Test
    fun `state copy should update status filter`() {
        val original = PageListState(selectedStatusFilter = null)
        val updated = original.copy(selectedStatusFilter = OrderStatus.PENDING)

        assertEquals(OrderStatus.PENDING, updated.selectedStatusFilter)
    }

    @Test
    fun `state copy should update dialog visibility`() {
        val original = PageListState(showCreateDialog = false)
        val updated = original.copy(showCreateDialog = true)

        assertTrue(updated.showCreateDialog)
    }

    @Test
    fun `state copy should update rename dialog data`() {
        val page = Page(id = "1", ownerId = "u1", title = "Old Title", components = emptyList())
        val original = PageListState()
        val updated = original.copy(
            showRenameDialog = true,
            pageToRename = page,
            newPageTitle = "New Title"
        )

        assertTrue(updated.showRenameDialog)
        assertEquals(page, updated.pageToRename)
        assertEquals("New Title", updated.newPageTitle)
    }

    @Test
    fun `PageListEvent OnTabSelected should contain index`() {
        val event = PageListEvent.OnTabSelected(2)
        assertEquals(2, event.index)
    }

    @Test
    fun `PageListEvent OnSearchQueryChanged should contain query`() {
        val event = PageListEvent.OnSearchQueryChanged("search term")
        assertEquals("search term", event.query)
    }

    @Test
    fun `PageListEvent OnStatusFilterSelected should contain status`() {
        val event = PageListEvent.OnStatusFilterSelected(OrderStatus.COMPLETED)
        assertEquals(OrderStatus.COMPLETED, event.status)
    }

    @Test
    fun `PageListEvent OnNewPageTitleChanged should contain title`() {
        val event = PageListEvent.OnNewPageTitleChanged("New Page")
        assertEquals("New Page", event.title)
    }

    @Test
    fun `PageListEvent OnConfirmCreatePage should contain template type`() {
        val event = PageListEvent.OnConfirmCreatePage(PageTemplateType.EMPTY)
        assertEquals(PageTemplateType.EMPTY, event.templateType)
    }

    @Test
    fun `PageListEvent OnRenamePageClicked should contain page`() {
        val page = Page(id = "1", ownerId = "u1", title = "Test", components = emptyList())
        val event = PageListEvent.OnRenamePageClicked(page)
        assertEquals(page, event.page)
    }

    @Test
    fun `PageListEvent OnDeletePageClicked should contain pageId`() {
        val event = PageListEvent.OnDeletePageClicked("page123")
        assertEquals("page123", event.pageId)
    }

    @Test
    fun `PageListEvent OnUpdateOrderStatus should contain orderId and status`() {
        val event = PageListEvent.OnUpdateOrderStatus("order456", OrderStatus.PROCESSING)
        assertEquals("order456", event.orderId)
        assertEquals(OrderStatus.PROCESSING, event.newStatus)
    }

    @Test
    fun `PageListEvent singleton objects should be same instance`() {
        val event1 = PageListEvent.OnCreatePageClicked
        val event2 = PageListEvent.OnCreatePageClicked
        assertSame(event1, event2)

        val event3 = PageListEvent.OnDismissCreateDialog
        val event4 = PageListEvent.OnDismissCreateDialog
        assertSame(event3, event4)
    }
}
