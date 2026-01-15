package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository

class FakePageRepository : PageRepository {
    private val pages = mutableListOf<Page>()
    var shouldReturnError = false

    override suspend fun getPages(): List<Page> {
        if (shouldReturnError) throw Exception("Erro de teste")
        return pages
    }

    override suspend fun savePage(page: Page, token: String): Result<Page> {
        if (shouldReturnError) return Result.failure(Exception("Erro ao salvar"))
        pages.add(page)
        return Result.success(page)
    }

    override suspend fun updatePage(page: Page, token: String): Result<Page> {
        if (shouldReturnError) return Result.failure(Exception("Erro ao atualizar"))
        val index = pages.indexOfFirst { it.id == page.id }
        if (index != -1) {
            pages[index] = page
            return Result.success(page)
        }
        return Result.failure(Exception("Página não encontrada"))
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Erro ao deletar"))
        val removed = pages.removeAll { it.id == id }
        return if (removed) Result.success(Unit) else Result.failure(Exception("Não encontrado"))
    }
}
