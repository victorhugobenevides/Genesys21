package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product

interface PageRepository {
    suspend fun getPages(token: String): List<Page>
    suspend fun getPublicPage(id: String): Result<Page>
    suspend fun getPageByDomain(domain: String): Result<Page>
    suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit>
    suspend fun deletePage(id: String, token: String): Result<Unit>
    suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String>
    
    suspend fun getAllProducts(token: String): Result<List<Product>>
    suspend fun saveProduct(product: Product, token: String): Result<Unit>
    suspend fun deleteProduct(id: String, token: String): Result<Unit>
    
    suspend fun getCategories(token: String): Result<List<Category>>
    suspend fun saveCategory(category: Category, token: String): Result<Unit>
    suspend fun deleteCategory(id: Int, token: String): Result<Unit>
}
