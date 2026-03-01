package com.itbenevides.genesys21.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CategoryTest {

    @Test
    fun `default category should have null id and empty optional fields`() {
        val category = Category(ownerId = "owner", name = "Category")

        assertNull(category.id)
        assertEquals("owner", category.ownerId)
        assertEquals("Category", category.name)
        assertNull(category.icon)
        assertNull(category.color)
    }

    @Test
    fun `category should contain provided values`() {
        val category = Category(
            id = 1,
            ownerId = "owner",
            name = "Category",
            icon = "icon",
            color = "#FFFFFF"
        )

        assertEquals(1, category.id)
        assertEquals("owner", category.ownerId)
        assertEquals("Category", category.name)
        assertEquals("icon", category.icon)
        assertEquals("#FFFFFF", category.color)
    }

    @Test
    fun `category copy should update values`() {
        val original = Category(id = 1, ownerId = "owner", name = "Original")
        val updated = original.copy(name = "Updated")

        assertEquals(1, updated.id)
        assertEquals("owner", updated.ownerId)
        assertEquals("Updated", updated.name)
    }
}
