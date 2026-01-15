package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryPageRepository : PageRepository {
    private val pagesDB = ConcurrentHashMap<String, Page>()

    override suspend fun getPages(): List<Page> = pagesDB.values.toList()

    override suspend fun savePage(page: Page, token: String): Result<Page> {
        pagesDB[page.id] = page
        return Result.success(page)
    }

    override suspend fun updatePage(page: Page, token: String): Result<Page> {
        return if (pagesDB.containsKey(page.id)) {
            pagesDB[page.id] = page
            Result.success(page)
        } else {
            Result.failure(Exception("Page not found"))
        }
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        return if (pagesDB.remove(id) != null) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Page not found"))
        }
    }
}
