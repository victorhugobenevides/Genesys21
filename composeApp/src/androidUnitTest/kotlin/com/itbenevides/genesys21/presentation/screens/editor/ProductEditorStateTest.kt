package com.itbenevides.genesys21.presentation.screens.editor

import com.itbenevides.genesys21.domain.model.Product
import org.junit.Assert.*
import org.junit.Test

class ProductEditorStateTest {

    @Test
    fun `default state should have correct initial values`() {
        val state = ProductEditorState()

        assertEquals("", state.name)
        assertEquals("", state.price)
        assertTrue(state.imageUrls.isEmpty())
        assertEquals("", state.description)
        assertNull(state.categoryId)
        assertEquals("", state.categoryName)
        assertEquals("0", state.stock)
        assertFalse(state.isLoading)
        assertFalse(state.isUploading)
        assertFalse(state.isEditing)
    }

    @Test
    fun `initial with null product should create new product state`() {
        val state = ProductEditorState.initial(null)

        assertFalse(state.isEditing)
        assertEquals("", state.name)
        assertEquals("", state.price)
        assertFalse(state.canSave) // name is blank
    }

    @Test
    fun `initial with existing product should create editing state`() {
        val product = Product(
            id = "p1",
            name = "Test Product",
            price = 99.99,
            description = "Description",
            categoryId = 1,
            categoryName = "Category",
            stock = 10
        )
        val state = ProductEditorState.initial(product)

        assertTrue(state.isEditing)
        assertEquals("Test Product", state.name)
        assertEquals("99,99", state.price) // dot replaced with comma
        assertEquals("Description", state.description)
        assertEquals(1, state.categoryId)
        assertEquals("Category", state.categoryName)
        assertEquals("10", state.stock)
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave should be true when name is not blank and not uploading or loading`() {
        val state = ProductEditorState(name = "Valid Name")
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave should be false when name is blank`() {
        val state = ProductEditorState(name = "")
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave should be false when isUploading is true`() {
        val state = ProductEditorState(name = "Valid Name", isUploading = true)
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave should be false when isLoading is true`() {
        val state = ProductEditorState(name = "Valid Name", isLoading = true)
        assertFalse(state.canSave)
    }

    @Test
    fun `state copy should update values correctly`() {
        val original = ProductEditorState()
        
        val updated = original.copy(
            name = "Updated Name",
            price = "50,00",
            stock = "20",
            isLoading = true
        )

        assertEquals("Updated Name", updated.name)
        assertEquals("50,00", updated.price)
        assertEquals("20", updated.stock)
        assertTrue(updated.isLoading)
    }

    @Test
    fun `ProductEditorEvent data classes should contain correct values`() {
        val nameEvent = ProductEditorEvent.OnNameChanged("New Name")
        assertEquals("New Name", nameEvent.name)

        val priceEvent = ProductEditorEvent.OnPriceChanged("100,00")
        assertEquals("100,00", priceEvent.price)

        val descEvent = ProductEditorEvent.OnDescriptionChanged("New Description")
        assertEquals("New Description", descEvent.description)

        val categoryEvent = ProductEditorEvent.OnCategoryChanged(5, "New Category")
        assertEquals(5, categoryEvent.categoryId)
        assertEquals("New Category", categoryEvent.categoryName)

        val stockEvent = ProductEditorEvent.OnStockChanged("100")
        assertEquals("100", stockEvent.stock)

        val removePhotoEvent = ProductEditorEvent.OnRemovePhotoClicked("http://example.com/image.jpg")
        assertEquals("http://example.com/image.jpg", removePhotoEvent.url)
    }

    @Test
    fun `ProductEditorEvent singletons should be same instance`() {
        val event1 = ProductEditorEvent.OnAddPhotoClicked
        val event2 = ProductEditorEvent.OnAddPhotoClicked
        assertSame(event1, event2)

        val event3 = ProductEditorEvent.OnSaveClicked
        val event4 = ProductEditorEvent.OnSaveClicked
        assertSame(event3, event4)
    }
}
