package com.itbenevides.genesys21.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PageSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    @Test
    fun testPolymorphicSerialization() {
        val page =
            Page(
                id = "test-1",
                title = "Test Page",
                components =
                    listOf(
                        PageComponent.Header(title = "Welcome"),
                        PageComponent.ProfileHeader(imageUrl = "img", name = "User"),
                        PageComponent.ProductGrid(productIds = listOf("p1", "p2")),
                        PageComponent.SocialLinks(instagram = "insta"),
                    ),
            )

        val serialized = json.encodeToString(page)
        println(serialized)

        // Verify some keywords are present in the JSON
        assertTrue(serialized.contains("Welcome"))
        assertTrue(serialized.contains("PageComponent.Header"))
        assertTrue(serialized.contains("PageComponent.ProfileHeader"))
        assertTrue(serialized.contains("PageComponent.ProductGrid"))
        assertTrue(serialized.contains("insta"))

        val deserialized = json.decodeFromString<Page>(serialized)
        assertEquals(page.id, deserialized.id)
        assertEquals(page.components.size, deserialized.components.size)
        assertTrue(deserialized.components[0] is PageComponent.Header)
        assertTrue(deserialized.components[1] is PageComponent.ProfileHeader)
        assertTrue(deserialized.components[2] is PageComponent.ProductGrid)
    }

    @Test
    fun testTransientFieldsAreNotSerialized() {
        val header =
            PageComponent.Header(
                title = "Title",
                customLabel = "Hidden Label",
            )

        val serialized = json.encodeToString<PageComponent>(header)

        // customLabel is @Transient, so it should NOT be in the JSON
        assertTrue(!serialized.contains("customLabel"))
        assertTrue(!serialized.contains("Hidden Label"))
    }

    @Test
    fun testReorderingLogic() {
        val c1 = PageComponent.Header("First")
        val c2 = PageComponent.Header("Second")
        val page = Page("id", "title", components = listOf(c1, c2))

        // Simulate Move Down (0 to 1)
        val list = page.components.toMutableList()
        val temp = list[0]
        list[0] = list[1]
        list[1] = temp
        val updatedPage = page.copy(components = list)

        assertEquals("Second", (updatedPage.components[0] as PageComponent.Header).title)
        assertEquals("First", (updatedPage.components[1] as PageComponent.Header).title)
    }
}
