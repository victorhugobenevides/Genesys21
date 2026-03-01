package com.itbenevides.genesys21.presentation.screens.editor

import com.itbenevides.genesys21.domain.model.Page
import org.junit.Assert.*
import org.junit.Test

class PageEditorScreenStateTest {

    @Test
    fun `default state should have correct initial values`() {
        val state = PageEditorState()

        assertEquals("", state.id)
        assertEquals("", state.title)
        assertFalse(state.isEditing)
        assertFalse(state.isLoading)
        assertFalse(state.canSave)
    }

    @Test
    fun `initial with null page should create new page state`() {
        val state = PageEditorState.initial(null)

        assertFalse(state.isEditing)
        assertEquals("", state.title)
        assertFalse(state.canSave)
        assertTrue(state.id.isNotBlank()) // Should generate random ID
    }

    @Test
    fun `initial with existing page should create editing state`() {
        val page = Page(id = "existing123", ownerId = "user", title = "Existing Page", components = emptyList())
        val state = PageEditorState.initial(page)

        assertTrue(state.isEditing)
        assertEquals("existing123", state.id)
        assertEquals("Existing Page", state.title)
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave is a field that should be set correctly`() {
        val state = PageEditorState(title = "Valid Title", canSave = true)
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave should be false when title is blank`() {
        val state = PageEditorState(title = "")
        assertFalse(state.canSave)
    }

    @Test
    fun `state copy should update values correctly`() {
        val original = PageEditorState()
        
        val updated = original.copy(
            title = "Updated Title",
            isLoading = true,
            canSave = true
        )

        assertEquals("Updated Title", updated.title)
        assertTrue(updated.isLoading)
        assertTrue(updated.canSave)
        assertEquals("", updated.id) // unchanged
    }

    @Test
    fun `PageEditorEvent OnTitleChanged should contain new title`() {
        val event = PageEditorEvent.OnTitleChanged("New Title")
        assertEquals("New Title", event.newTitle)
    }

    @Test
    fun `PageEditorEvent singletons should be same instance`() {
        val event1 = PageEditorEvent.OnSaveClicked
        val event2 = PageEditorEvent.OnSaveClicked
        assertSame(event1, event2)

        val event3 = PageEditorEvent.OnBackClicked
        val event4 = PageEditorEvent.OnBackClicked
        assertSame(event3, event4)
    }
}
