package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.domain.model.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Testes de integração para SqlitePageRepository.
 * Usa banco SQLite temporário em arquivo para persistência real.
 */
class SqlitePageRepositoryIntegrationTest {
    private lateinit var repository: SqlitePageRepository
    private lateinit var database: Database
    private val testDbPath = "build/test-db-${System.currentTimeMillis()}.db"

    @BeforeTest
    fun setup() {
        // Criar banco em arquivo (não em memória) para persistência entre operações
        database = Database.connect("jdbc:sqlite:$testDbPath", "org.sqlite.JDBC")
        
        // Configurar DatabaseFactory para usar nosso banco de teste
        DatabaseFactory.configureTestDatabase(database)
        
        // Criar tabelas
        transaction(database) {
            SchemaUtils.create(
                com.itbenevides.genesys21.data.database.PagesTable,
                com.itbenevides.genesys21.data.database.ProductsTable,
                com.itbenevides.genesys21.data.database.CategoriesTable,
                com.itbenevides.genesys21.data.database.ProductImagesTable
            )
        }
        
        repository = SqlitePageRepository(Json { ignoreUnknownKeys = true })
    }

    @AfterTest
    fun tearDown() {
        // Limpar banco de teste
        DatabaseFactory.configureTestDatabase(null)
        Path(testDbPath).deleteIfExists()
    }

    @Test
    fun testCreateAndGetPage() = runBlocking {
        val page = Page(
            id = "page-1",
            title = "Test Page",
            ownerId = "user-1",
            customDomain = "test-domain",
            whatsapp = "5511999999999",
            theme = PageThemeConfig.ROYAL,
            components = emptyList()
        )
        
        val result = repository.savePage(page, "user-1", isEditing = false)
        assertTrue(result.isSuccess)
        
        val pages = repository.getPages("user-1")
        assertEquals(1, pages.size)
        assertEquals("page-1", pages.first().id)
        assertEquals("Test Page", pages.first().title)
    }

    @Test
    fun testUpdatePage() = runBlocking {
        // Criar
        val page = Page(
            id = "page-2",
            title = "Original Title",
            ownerId = "user-1",
            customDomain = "domain-2",
            theme = PageThemeConfig.ROYAL,
            components = emptyList()
        )
        repository.savePage(page, "user-1", isEditing = false)
        
        // Atualizar
        val updated = page.copy(title = "Updated Title", theme = PageThemeConfig.OCEAN)
        val result = repository.savePage(updated, "user-1", isEditing = true)
        assertTrue(result.isSuccess)
        
        // Verificar
        val fetched = repository.getPublicPage("page-2").getOrNull()
        assertNotNull(fetched)
        assertEquals("Updated Title", fetched.title)
        assertEquals(PageThemeConfig.OCEAN, fetched.theme)
    }

    @Test
    fun testDeletePage() = runBlocking {
        // Criar
        val page = Page(
            id = "page-3",
            title = "To Delete",
            ownerId = "user-1",
            theme = PageThemeConfig.ROYAL,
            components = emptyList()
        )
        repository.savePage(page, "user-1", isEditing = false)
        
        // Verificar que existe
        assertEquals(1, repository.getPages("user-1").size)
        
        // Deletar
        val result = repository.deletePage("page-3", "user-1")
        assertTrue(result.isSuccess)
        
        // Verificar que não existe mais
        assertEquals(0, repository.getPages("user-1").size)
    }

    @Test
    fun testGetPublicPageNotFound() = runBlocking {
        val result = repository.getPublicPage("nonexistent")
        assertTrue(result.isFailure)
    }

    @Test
    fun testGetPageByDomain() = runBlocking {
        val page = Page(
            id = "page-4",
            title = "Domain Page",
            ownerId = "user-1",
            customDomain = "my-domain",
            theme = PageThemeConfig.FOREST,
            components = emptyList()
        )
        repository.savePage(page, "user-1", isEditing = false)
        
        val result = repository.getPageByDomain("my-domain")
        assertTrue(result.isSuccess)
        assertEquals("page-4", result.getOrNull()?.id)
        assertEquals(PageThemeConfig.FOREST, result.getOrNull()?.theme)
    }

    @Test
    fun testGetPageByDomainNotFound() = runBlocking {
        val result = repository.getPageByDomain("nonexistent-domain")
        assertTrue(result.isFailure)
    }

    @Test
    fun testCreateAndGetProduct() = runBlocking {
        val product = Product(
            id = "prod-1",
            name = "Test Product",
            price = 99.99,
            description = "A test product",
            stock = 10,
            categoryId = null
        )
        
        val result = repository.saveProduct(product, "user-1")
        assertTrue(result.isSuccess)
        
        val products = repository.getAllProducts("user-1").getOrNull()
        assertNotNull(products)
        assertEquals(1, products.size)
        assertEquals("prod-1", products.first().id)
    }

    @Test
    fun testUpdateProduct() = runBlocking {
        // Criar
        val product = Product(
            id = "prod-2",
            name = "Original Name",
            price = 10.0,
            stock = 5
        )
        repository.saveProduct(product, "user-1")
        
        // Atualizar
        val updated = product.copy(name = "Updated Name", price = 20.0, stock = 15)
        val result = repository.saveProduct(updated, "user-1")
        assertTrue(result.isSuccess)
        
        // Verificar
        val products = repository.getAllProducts("user-1").getOrNull()
        assertNotNull(products)
        assertEquals("Updated Name", products.first().name)
        assertEquals(20.0, products.first().price)
    }

    @Test
    fun testDeleteProduct() = runBlocking {
        // Criar
        val product = Product(
            id = "prod-3",
            name = "To Delete",
            price = 5.0
        )
        repository.saveProduct(product, "user-1")
        assertEquals(1, repository.getAllProducts("user-1").getOrNull()?.size)
        
        // Deletar
        val result = repository.deleteProduct("prod-3", "user-1")
        assertTrue(result.isSuccess)
        
        // Verificar
        assertEquals(0, repository.getAllProducts("user-1").getOrNull()?.size)
    }

    @Test
    fun testCreateAndGetCategory() = runBlocking {
        val category = Category(
            id = null,
            name = "Test Category",
            ownerId = "user-1"
        )
        
        val result = repository.saveCategory(category, "user-1")
        assertTrue(result.isSuccess)
        
        val categories = repository.getCategories("user-1").getOrNull()
        assertNotNull(categories)
        assertEquals(1, categories.size)
        assertEquals("Test Category", categories.first().name)
    }

    @Test
    fun testUpdateCategory() = runBlocking {
        // Criar
        val category = Category(id = null, name = "Original", ownerId = "user-1")
        repository.saveCategory(category, "user-1")
        val createdId = repository.getCategories("user-1").getOrNull()?.first()?.id
        
        // Atualizar
        val updated = Category(id = createdId, name = "Updated", ownerId = "user-1")
        val result = repository.saveCategory(updated, "user-1")
        assertTrue(result.isSuccess)
        
        // Verificar
        val categories = repository.getCategories("user-1").getOrNull()
        assertEquals("Updated", categories?.first()?.name)
    }

    @Test
    fun testDeleteCategory() = runBlocking {
        // Criar
        val category = Category(id = null, name = "To Delete", ownerId = "user-1")
        repository.saveCategory(category, "user-1")
        val createdId = repository.getCategories("user-1").getOrNull()?.first()?.id!!
        
        // Deletar
        val result = repository.deleteCategory(createdId, "user-1")
        assertTrue(result.isSuccess)
        
        // Verificar
        assertEquals(0, repository.getCategories("user-1").getOrNull()?.size)
    }

    @Test
    fun testGetPagesMultipleUsers() = runBlocking {
        // Criar páginas para diferentes usuários
        repository.savePage(
            Page("page-u1", "User 1 Page", "user-1", theme = PageThemeConfig.ROYAL, components = emptyList()),
            "user-1", isEditing = false
        )
        repository.savePage(
            Page("page-u2", "User 2 Page", "user-2", theme = PageThemeConfig.OCEAN, components = emptyList()),
            "user-2", isEditing = false
        )
        
        // Verificar isolamento
        assertEquals(1, repository.getPages("user-1").size)
        assertEquals(1, repository.getPages("user-2").size)
        assertEquals("User 1 Page", repository.getPages("user-1").first().title)
        assertEquals("User 2 Page", repository.getPages("user-2").first().title)
    }

    @Test
    fun testDeletePageWrongOwner() = runBlocking {
        // Criar página para user-1
        repository.savePage(
            Page("page-owner", "Owner Page", "user-1", theme = PageThemeConfig.ROYAL, components = emptyList()),
            "user-1", isEditing = false
        )
        
        // Tentar deletar como user-2
        repository.deletePage("page-owner", "user-2")
        
        // Verificar que ainda existe
        assertEquals(1, repository.getPages("user-1").size)
    }

    @Test
    fun testGetPublicPageWithComponents() = runBlocking {
        val page = Page(
            id = "page-comp",
            title = "Page with Components",
            ownerId = "user-1",
            theme = PageThemeConfig.ROYAL,
            components = listOf(
                PageComponent.Header(title = "Welcome"),
                PageComponent.Typography(text = "Hello World")
            )
        )
        repository.savePage(page, "user-1", isEditing = false)
        
        val fetched = repository.getPublicPage("page-comp").getOrNull()
        assertNotNull(fetched)
        assertEquals(2, fetched.components.size)
    }

    @Test
    fun testSaveProductWithCategory() = runBlocking {
        // Criar categoria
        repository.saveCategory(Category(id = null, name = "Electronics", ownerId = "user-1"), "user-1")
        val categoryId = repository.getCategories("user-1").getOrNull()?.first()?.id
        
        // Criar produto com categoria
        val product = Product(
            id = "prod-cat",
            name = "Laptop",
            price = 1000.0,
            categoryId = categoryId
        )
        repository.saveProduct(product, "user-1")
        
        // Verificar
        val products = repository.getAllProducts("user-1").getOrNull()
        assertNotNull(products)
        assertEquals(categoryId, products.first().categoryId)
    }

    @Test
    fun testUploadImageNotSupported() = runBlocking {
        val result = repository.uploadImage(ByteArray(0), "test.jpg", "user-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun testGetEmptyPagesList() = runBlocking {
        val pages = repository.getPages("new-user")
        assertTrue(pages.isEmpty())
    }

    @Test
    fun testGetEmptyProductsList() = runBlocking {
        val products = repository.getAllProducts("new-user").getOrNull()
        assertNotNull(products)
        assertTrue(products.isEmpty())
    }

    @Test
    fun testGetEmptyCategoriesList() = runBlocking {
        val categories = repository.getCategories("new-user").getOrNull()
        assertNotNull(categories)
        assertTrue(categories.isEmpty())
    }

    @Test
    fun testUpdateNonExistentPage() = runBlocking {
        val page = Page(
            id = "nonexistent",
            title = "Doesn't exist",
            ownerId = "user-1",
            theme = PageThemeConfig.ROYAL,
            components = emptyList()
        )
        // Atualizar página inexistente (não deve falhar, apenas não atualizar nada)
        val result = repository.savePage(page, "user-1", isEditing = true)
        assertTrue(result.isSuccess)
        
        // Verificar que não criou
        assertEquals(0, repository.getPages("user-1").size)
    }

    @Test
    fun testMultiplePagesForUser() = runBlocking {
        // Criar várias páginas
        repeat(5) { i ->
            repository.savePage(
                Page("page-$i", "Page $i", "user-1", theme = PageThemeConfig.ROYAL, components = emptyList()),
                "user-1", isEditing = false
            )
        }
        
        val pages = repository.getPages("user-1")
        assertEquals(5, pages.size)
    }

    @Test
    fun testMultipleProductsForUser() = runBlocking {
        // Criar vários produtos
        repeat(10) { i ->
            repository.saveProduct(
                Product("prod-$i", "Product $i", price = (i * 10.0)),
                "user-1"
            )
        }
        
        val products = repository.getAllProducts("user-1").getOrNull()
        assertNotNull(products)
        assertEquals(10, products.size)
    }

    @Test
    fun testProductWithZeroPrice() = runBlocking {
        val product = Product(id = "prod-free", name = "Free Item", price = 0.0)
        val result = repository.saveProduct(product, "user-1")
        assertTrue(result.isSuccess)
        
        val fetched = repository.getAllProducts("user-1").getOrNull()?.first()
        assertEquals(0.0, fetched?.price)
    }

    @Test
    fun testProductWithNegativePrice() = runBlocking {
        val product = Product(id = "prod-neg", name = "Negative", price = -10.0)
        val result = repository.saveProduct(product, "user-1")
        assertTrue(result.isSuccess)
        
        val fetched = repository.getAllProducts("user-1").getOrNull()?.first()
        assertEquals(-10.0, fetched?.price)
    }

    @Test
    fun testPageWithAllThemes() = runBlocking {
        PageThemeConfig.entries.forEach { theme ->
            val page = Page(
                id = "page-${theme.name}",
                title = "${theme.name} Page",
                ownerId = "user-1",
                theme = theme,
                components = emptyList()
            )
            repository.savePage(page, "user-1", isEditing = false)
        }
        
        val pages = repository.getPages("user-1")
        assertEquals(PageThemeConfig.entries.size, pages.size)
    }
}
