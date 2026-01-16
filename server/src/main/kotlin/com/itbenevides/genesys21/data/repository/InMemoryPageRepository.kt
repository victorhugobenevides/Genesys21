package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryPageRepository : PageRepository {
    private val pagesDB = ConcurrentHashMap<String, Page>()

    override suspend fun getPages(token: String): List<Page> {
        // O parâmetro 'token' aqui deve ser o UID do Firebase extraído na rota
        return pagesDB.values.filter { it.ownerId == token }
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        return pagesDB[id]?.let { Result.success(it) } ?: Result.failure(Exception("Page not found"))
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        // Garante que a página salva pertence ao usuário logado
        val pageWithOwner = page.copy(ownerId = token)
        
        if (isEditing) {
            val existingPage = pagesDB[page.id]
            if (existingPage != null && existingPage.ownerId != token) {
                return Result.failure(Exception("Unauthorized: You do not own this page"))
            }
        }
        
        pagesDB[page.id] = pageWithOwner
        return Result.success(Unit)
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        val existingPage = pagesDB[id]
        if (existingPage != null && existingPage.ownerId != token) {
            return Result.failure(Exception("Unauthorized: You do not own this page"))
        }
        
        return if (pagesDB.remove(id) != null) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Page not found"))
        }
    }
}
