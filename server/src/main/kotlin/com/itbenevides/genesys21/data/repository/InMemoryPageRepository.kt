package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryPageRepository : PageRepository {
    private val pagesDB = ConcurrentHashMap<String, Page>()

    override suspend fun getPages(token: String): List<Page> {
        // Em um cenário real, o token seria usado para filtrar as páginas por usuário
        return pagesDB.values.toList()
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        return pagesDB[id]?.let { Result.success(it) } ?: Result.failure(Exception("Page not found"))
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        pagesDB[page.id] = page
        return Result.success(Unit)
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        return if (pagesDB.remove(id) != null) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Page not found"))
        }
    }
}
