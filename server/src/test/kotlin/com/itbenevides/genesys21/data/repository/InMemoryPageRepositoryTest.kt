package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.*
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * Testes para o InMemoryPageRepository.
 */
class InMemoryPageRepositoryTest {

    private lateinit var repository: InMemoryPageRepository

    @BeforeTest
    fun setup() {
        repository = InMemoryPageRepository()
    }

    @Test
    fun testGetPagesReturnsOnlyOwnerPages() = runBlocking {
        val owner1 = "user-1"
        val owner2 = "user-2"

        repository.savePage(
            Page(id = "page-1", title = "Page 1", ownerId = owner1),
            token = owner1,
            isEditing = false
        )
        repository.savePage(
            Page(id = "page-2", title = "Page 2", ownerId = owner2),
            token = owner2,
            isEditing = false
        )

        val pages = repository.getPages(owner1)
        assertEquals(1, pages.size)
        assertEquals("Page 1", pages.first().title)
    }

    @Test
    fun testGetPublicPageReturnsPage() = runBlocking {
        val page = Page(id = "public-1", title = "Public Page", ownerId = "user-1")
        repository.savePage(page, token = "user-1", isEditing = false)

        val result = repository.getPublicPage("public-1")
        assertTrue(result.isSuccess)
        assertEquals("Public Page", result.getOrNull()?.title)
    }

    @Test
    fun testGetPublicPageReturnsFailureWhenNotFound() = runBlocking {
        val result = repository.getPublicPage("non-existent")
        assertTrue(result.isFailure)
    }

    @Test
    fun testGetPageByDomain() = runBlocking {
        val page = Page(
            id = "page-1",
            title = "Page with Domain",
            ownerId = "user-1",
            customDomain = "example.com"
        )
        repository.savePage(page, token = "user-1", isEditing = false)

        val result = repository.getPageByDomain("example.com")
        assertTrue(result.isSuccess)
        assertEquals("page-1", result.getOrNull()?.id)
    }

    @Test
    fun testSavePageCreatesNewPage() = runBlocking {
        val page = Page(id = "new-page", title = "New Page", ownerId = null)
        
        val result = repository.savePage(page, token = "user-1", isEditing = false)
        assertTrue(result.isSuccess)
        
        val saved = repository.getPublicPage("new-page").getOrNull()
        assertNotNull(saved)
        assertEquals("user-1", saved.ownerId)
    }

    @Test
    fun testSavePageUpdatesExistingPage() = runBlocking {
        val page = Page(id = "edit-page", title = "Original", ownerId = "user-1")
        repository.savePage(page, token = "user-1", isEditing = false)

        val updated = page.copy(title = "Updated")
        val result = repository.savePage(updated, token = "user-1", isEditing = true)
        
        assertTrue(result.isSuccess)
        val saved = repository.getPublicPage("edit-page").getOrNull()
        assertEquals("Updated", saved?.title)
    }

    @Test
    fun testSavePageThrowsWhenUnauthorized() = runBlocking {
        val page = Page(id = "page-1", title = "Page", ownerId = "user-1")
        repository.savePage(page, token = "user-1", isEditing = false)

        val result = repository.savePage(page.copy(ownerId = "user-2"), token = "user-3", isEditing = true)
        assertTrue(result.isFailure)
    }

    @Test
    fun testDeletePageRemovesPage() = runBlocking {
        val page = Page(id = "delete-me", title = "To Delete", ownerId = "user-1")
        repository.savePage(page, token = "user-1", isEditing = false)

        val result = repository.deletePage("delete-me", token = "user-1")
        assertTrue(result.isSuccess)

        val pages = repository.getPages("user-1")
        assertEquals(0, pages.size)
    }

    @Test
    fun testDeletePageThrowsWhenNotOwner() = runBlocking {
        val page = Page(id = "page-1", title = "Page", ownerId = "user-1")
        repository.savePage(page, token = "user-1", isEditing = false)

        val result = repository.deletePage("page-1", token = "user-2")
        assertTrue(result.isFailure)
    }

    @Test
    fun testProductCRUD() = runBlocking {
        val token = "user-1"

        // Create
        val product = Product(id = "p1", name = "Product 1", price = 10.0)
        repository.saveProduct(product, token)

        val products = repository.getAllProducts(token).getOrNull()
        assertEquals(1, products?.size)
        assertEquals("Product 1", products?.first()?.name)

        // Update
        repository.saveProduct(product.copy(price = 20.0), token)
        val updated = repository.getAllProducts(token).getOrNull()
        assertEquals(20.0, updated?.first()?.price)

        // Delete
        repository.deleteProduct("p1", token)
        val afterDelete = repository.getAllProducts(token).getOrNull()
        assertEquals(0, afterDelete?.size)
    }

    @Test
    fun testCategoryCRUD() = runBlocking {
        val token = "user-1"

        // Create
        val category = Category(id = null, name = "Electronics", ownerId = token)
        repository.saveCategory(category, token)

        val categories = repository.getCategories(token).getOrNull()
        assertEquals(1, categories?.size)
        assertTrue(categories?.first()?.id!! > 0)

        // Update
        val catId = categories.first().id
        repository.saveCategory(Category(id = catId, name = "Gadgets", ownerId = token), token)
        val updated = repository.getCategories(token).getOrNull()
        assertEquals("Gadgets", updated?.first()?.name)

        // Delete
        repository.deleteCategory(catId!!, token)
        val afterDelete = repository.getCategories(token).getOrNull()
        assertEquals(0, afterDelete?.size)
    }

    @Test
    fun testCategoryUnauthorizedThrowsException() = runBlocking {
        repository.saveCategory(Category(id = null, name = "Test", ownerId = "owner-1"), "owner-1")
        
        val result = repository.saveCategory(Category(id = 1, name = "Test", ownerId = "owner-1"), "owner-2")
        assertTrue(result.isFailure)
    }

    @Test
    fun testSavePageWithOwnerMismatch() = runBlocking {
        val page = Page(id = "page-1", title = "Page", ownerId = "user-1")
        repository.savePage(page, token = "user-1", isEditing = false)
        
        // Tentar salvar com ownerId diferente do token
        val result = repository.savePage(
            page.copy(ownerId = "user-2"), 
            token = "user-1", 
            isEditing = true
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun testSavePageCreatesWithNullOwner() = runBlocking {
        val page = Page(id = "new-owner-page", title = "New Page", ownerId = null)
        
        val result = repository.savePage(page, token = "user-1", isEditing = false)
        assertTrue(result.isSuccess)
        
        val saved = repository.getPages("user-1")
        assertEquals(1, saved.size)
        assertEquals("user-1", saved[0].ownerId)
    }

    @Test
    fun testUpdateNonExistentPage() = runBlocking {
        val page = Page(id = "non-existent", title = "Page", ownerId = "user-1")
        
        val result = repository.savePage(page, token = "user-1", isEditing = true)
        assertTrue(result.isFailure)
    }

    @Test
    fun testGetPageByDomainNotFound() = runBlocking {
        val result = repository.getPageByDomain("nonexistent.com")
        assertTrue(result.isFailure)
    }

    @Test
    fun testDeletePageNotFound() = runBlocking {
        val result = repository.deletePage("non-existent", token = "user-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun testGetAllProductsEmpty() = runBlocking {
        val products = repository.getAllProducts("user-1").getOrNull()
        assertNotNull(products)
        assertEquals(0, products.size)
    }

    @Test
    fun testProductUpdateWithoutCreate() = runBlocking {
        // Tentar atualizar produto que não existe
        val product = Product(id = "new-prod", name = "New Product", price = 15.0)
        repository.saveProduct(product, "user-1")
        
        val products = repository.getAllProducts("user-1").getOrNull()
        assertEquals(1, products?.size)
    }

    @Test
    fun testDeleteProductNotFound() = runBlocking {
        val result = repository.deleteProduct("non-existent", "user-1")
        // Deve retornar sucesso mesmo se não encontrou (removeIf retorna false)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testGetCategoriesEmpty() = runBlocking {
        val categories = repository.getCategories("user-1").getOrNull()
        assertNotNull(categories)
        assertEquals(0, categories.size)
    }

    @Test
    fun testSaveCategoryWithIdNotFound() = runBlocking {
        val category = Category(id = 999, name = "Test", ownerId = "user-1")
        val result = repository.saveCategory(category, "user-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun testDeleteCategoryNotFound() = runBlocking {
        val result = repository.deleteCategory(999, "user-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun testDeleteCategoryUnauthorized() = runBlocking {
        repository.saveCategory(Category(id = null, name = "Test", ownerId = "user-1"), "user-1")
        val catId = repository.getCategories("user-1").getOrNull()?.first()?.id
        
        val result = repository.deleteCategory(catId!!, "user-2")
        assertTrue(result.isFailure)
    }

    @Test
    fun testMultipleCategoriesAutoIncrement() = runBlocking {
        val cat1 = Category(id = null, name = "Cat 1", ownerId = "user-1")
        val cat2 = Category(id = null, name = "Cat 2", ownerId = "user-1")
        val cat3 = Category(id = null, name = "Cat 3", ownerId = "user-1")
        
        repository.saveCategory(cat1, "user-1")
        repository.saveCategory(cat2, "user-1")
        repository.saveCategory(cat3, "user-1")
        
        val categories = repository.getCategories("user-1").getOrNull()
        assertEquals(3, categories?.size)
        
        // Verificar que IDs são diferentes e crescentes
        val ids = categories?.mapNotNull { it.id }?.sorted()
        assertTrue(ids!![0] < ids[1])
        assertTrue(ids[1] < ids[2])
    }

    @Test
    fun testGetPagesEmpty() = runBlocking {
        val pages = repository.getPages("user-1")
        assertEquals(0, pages.size)
    }

    @Test
    fun testCategoryOwnerIsolation() = runBlocking {
        repository.saveCategory(Category(id = null, name = "User 1 Cat", ownerId = "user-1"), "user-1")
        repository.saveCategory(Category(id = null, name = "User 2 Cat", ownerId = "user-2"), "user-2")
        
        val user1Cats = repository.getCategories("user-1").getOrNull()
        val user2Cats = repository.getCategories("user-2").getOrNull()
        
        assertEquals(1, user1Cats?.size)
        assertEquals(1, user2Cats?.size)
        assertEquals("User 1 Cat", user1Cats?.first()?.name)
        assertEquals("User 2 Cat", user2Cats?.first()?.name)
    }

    @Test
    fun testUpdatePageWithDifferentOwnerInBody() = runBlocking {
        val page = Page(id = "page-1", title = "Page", ownerId = "user-1")
        repository.savePage(page, token = "user-1", isEditing = false)
        
        // Tentar atualizar passando ownerId diferente no body
        val result = repository.savePage(
            page.copy(ownerId = "user-2", title = "Hacked"), 
            token = "user-1", 
            isEditing = true
        )
        // Deve falhar porque ownerId no body não matcha com token
        assertTrue(result.isFailure)
    }

    @Test
    fun testSaveNewPageWithExistingIdDifferentOwner() = runBlocking {
        val page1 = Page(id = "same-id", title = "Page 1", ownerId = "user-1")
        repository.savePage(page1, token = "user-1", isEditing = false)
        
        // Tentar criar página com mesmo ID mas owner diferente
        val page2 = Page(id = "same-id", title = "Page 2", ownerId = "user-2")
        val result = repository.savePage(page2, token = "user-2", isEditing = false)
        assertTrue(result.isSuccess)
        
        // Cada usuário deve ver sua própria página
        val user1Pages = repository.getPages("user-1")
        val user2Pages = repository.getPages("user-2")
        
        assertEquals(1, user1Pages.size)
        assertEquals(1, user2Pages.size)
        assertEquals("Page 1", user1Pages[0].title)
        assertEquals("Page 2", user2Pages[0].title)
    }

    @Test
    fun testUploadImageReturnsFailure() = runBlocking {
        val result = repository.uploadImage(
            bytes = byteArrayOf(1, 2, 3), 
            fileName = "test.jpg", 
            token = "user-1"
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnsupportedOperationException)
    }

    @Test
    fun testCategoryUpdatePreservesOwner() = runBlocking {
        repository.saveCategory(Category(id = null, name = "Original", ownerId = "user-1"), "user-1")
        val cat = repository.getCategories("user-1").getOrNull()?.first()
        
        // Atualizar mantendo mesmo owner
        repository.saveCategory(
            Category(id = cat?.id, name = "Updated", ownerId = "user-1"), 
            "user-1"
        )
        
        val updated = repository.getCategories("user-1").getOrNull()?.first()
        assertEquals("Updated", updated?.name)
        assertEquals("user-1", updated?.ownerId)
    }
}
