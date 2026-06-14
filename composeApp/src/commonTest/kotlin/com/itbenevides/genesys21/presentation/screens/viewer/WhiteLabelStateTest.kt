package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WhiteLabelStateTest {

    private val emptyPage = Page(id = "1", title = "Test", components = emptyList())

    @Test
    fun initialState_isCorrect() {
        val state = WhiteLabelState(page = emptyPage)
        
        assertEquals(emptyPage, state.page)
        assertFalse(state.isLoading)
        assertFalse(state.showCatalog)
        assertEquals(null, state.editingComponentIndex)
    }

    @Test
    fun copyState_updatesCorrectly() {
        val state = WhiteLabelState(page = emptyPage)
        val updatedPage = emptyPage.copy(title = "Updated")
        
        val newState = state.copy(
            page = updatedPage,
            isLoading = true,
            editingComponentIndex = 5
        )
        
        assertEquals("Updated", newState.page.title)
        assertTrue(newState.isLoading)
        assertEquals(5, newState.editingComponentIndex)
    }
}
