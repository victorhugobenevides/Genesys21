package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import org.junit.Assert.*
import org.junit.Test

class WhiteLabelStateTest {

    @Test
    fun `default state should have correct initial values`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val state = WhiteLabelState(page = page)

        assertEquals(page, state.page)
        assertFalse(state.isLoading)
        assertFalse(state.isUploading)
        assertTrue(state.availableProducts.isEmpty())
        assertTrue(state.allAvailableCategories.isEmpty())
        assertFalse(state.showCatalog)
        assertFalse(state.showThemeSelector)
        assertFalse(state.showPageSettings)
        assertNull(state.editingComponentIndex)
        assertNull(state.pendingNewComponent)
        assertEquals("", state.filterQuery)
        assertTrue(state.userPages.isEmpty())
    }

    @Test
    fun `state copy should update values correctly`() {
        val page = Page(id = "1", ownerId = "user", title = "Test", components = emptyList())
        val original = WhiteLabelState(page = page)
        
        val updated = original.copy(
            isLoading = true,
            showCatalog = true,
            filterQuery = "test"
        )

        assertTrue(updated.isLoading)
        assertTrue(updated.showCatalog)
        assertEquals("test", updated.filterQuery)
        assertEquals(page, updated.page) // unchanged
    }

    @Test
    fun `WhiteLabelEvent OnPageUpdated should contain new page`() {
        val newPage = Page(id = "2", ownerId = "user", title = "Updated", components = emptyList())
        val event = WhiteLabelEvent.OnPageUpdated(newPage)
        
        assertEquals(newPage, event.newPage)
    }

    @Test
    fun `WhiteLabelEvent singletons should be same instance`() {
        val event1 = WhiteLabelEvent.OnPublishClicked
        val event2 = WhiteLabelEvent.OnPublishClicked
        assertSame(event1, event2)

        val event3 = WhiteLabelEvent.OnBackClicked
        val event4 = WhiteLabelEvent.OnBackClicked
        assertSame(event3, event4)
    }

    @Test
    fun `WhiteLabelEvent OnEditProductClicked should contain product and index`() {
        val product = Product(id = "p1", name = "Test", price = 10.0)
        val event = WhiteLabelEvent.OnEditProductClicked(product, 5)
        
        assertEquals(product, event.product)
        assertEquals(5, event.componentIndex)
    }

    @Test
    fun `WhiteLabelEvent OnShowCatalogChanged should contain show value`() {
        val event = WhiteLabelEvent.OnShowCatalogChanged(true)
        assertTrue(event.show)
    }

    @Test
    fun `WhiteLabelEvent OnShowThemeSelectorChanged should contain show value`() {
        val event = WhiteLabelEvent.OnShowThemeSelectorChanged(true)
        assertTrue(event.show)
    }

    @Test
    fun `WhiteLabelEvent OnShowPageSettingsChanged should contain show value`() {
        val event = WhiteLabelEvent.OnShowPageSettingsChanged(true)
        assertTrue(event.show)
    }

    @Test
    fun `WhiteLabelEvent OnEditingComponentIndexChanged should contain index`() {
        val event = WhiteLabelEvent.OnEditingComponentIndexChanged(3)
        assertEquals(3, event.index)
    }

    @Test
    fun `WhiteLabelEvent OnFilterQueryChanged should contain query`() {
        val event = WhiteLabelEvent.OnFilterQueryChanged("search term")
        assertEquals("search term", event.query)
    }

    @Test
    fun `WhiteLabelEvent OnImageUploadStarted should contain isUploading`() {
        val event = WhiteLabelEvent.OnImageUploadStarted(true)
        assertTrue(event.isUploading)
    }

    @Test
    fun `WhiteLabelEvent component actions should contain index`() {
        val deleteEvent = WhiteLabelEvent.OnDeleteComponent(2)
        assertEquals(2, deleteEvent.index)

        val duplicateEvent = WhiteLabelEvent.OnDuplicateComponent(3)
        assertEquals(3, duplicateEvent.index)

        val moveUpEvent = WhiteLabelEvent.OnMoveComponentUp(4)
        assertEquals(4, moveUpEvent.index)

        val moveDownEvent = WhiteLabelEvent.OnMoveComponentDown(5)
        assertEquals(5, moveDownEvent.index)
    }
}
