package com.itbenevides.genesys21.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ProductTest {

    @Test
    fun product_should_create_with_default_values() {
        val product = Product()

        assertEquals("", product.id)
        assertEquals("", product.name)
        assertEquals(0.0, product.price)
        assertEquals(emptyList<String>(), product.imageUrls)
        assertEquals("", product.description)
        assertEquals(null, product.categoryId)
        assertEquals(null, product.categoryName)
        assertEquals(0, product.stock)
    }

    @Test
    fun product_should_create_with_all_values() {
        val product = Product(
            id = "p1",
            name = "Test Product",
            price = 99.99,
            imageUrls = listOf("url1", "url2"),
            description = "A great product",
            categoryId = 1,
            categoryName = "Electronics",
            stock = 10
        )

        assertEquals("p1", product.id)
        assertEquals("Test Product", product.name)
        assertEquals(99.99, product.price)
        assertEquals(listOf("url1", "url2"), product.imageUrls)
        assertEquals("A great product", product.description)
        assertEquals(1, product.categoryId)
        assertEquals("Electronics", product.categoryName)
        assertEquals(10, product.stock)
    }

    @Test
    fun product_imageUrl_should_return_first_image() {
        val product = Product(
            id = "p1",
            name = "Test",
            price = 10.0,
            imageUrls = listOf("first.jpg", "second.jpg")
        )

        assertEquals("first.jpg", product.imageUrl)
    }

    @Test
    fun product_imageUrl_should_return_empty_when_no_images() {
        val product = Product(
            id = "p1",
            name = "Test",
            price = 10.0,
            imageUrls = emptyList()
        )

        assertEquals("", product.imageUrl)
    }
}

class PageThemeConfigTest {

    @Test
    fun pageThemeConfig_should_have_all_themes() {
        val themes = PageThemeConfig.entries

        assertEquals(23, themes.size)
        assertTrue(themes.contains(PageThemeConfig.ROYAL))
        assertTrue(themes.contains(PageThemeConfig.OCEAN))
        assertTrue(themes.contains(PageThemeConfig.FOREST))
        assertTrue(themes.contains(PageThemeConfig.CANDY))
        assertTrue(themes.contains(PageThemeConfig.SUNSET))
        assertTrue(themes.contains(PageThemeConfig.DARK_MODE))
        assertTrue(themes.contains(PageThemeConfig.DEFAULT))
    }
}

class PageTemplateTypeTest {

    @Test
    fun pageTemplateType_should_have_all_types() {
        val types = PageTemplateType.entries

        assertEquals(4, types.size)
        assertTrue(types.contains(PageTemplateType.EMPTY))
        assertTrue(types.contains(PageTemplateType.STORE))
        assertTrue(types.contains(PageTemplateType.BIO))
        assertTrue(types.contains(PageTemplateType.LANDING))
    }
}

class StepItemTest {

    @Test
    fun stepItem_should_create_with_default_values() {
        val step = StepItem()

        assertEquals("", step.title)
        assertEquals("", step.description)
    }

    @Test
    fun stepItem_should_create_with_values() {
        val step = StepItem(
            title = "Step 1",
            description = "Description of step 1"
        )

        assertEquals("Step 1", step.title)
        assertEquals("Description of step 1", step.description)
    }
}
