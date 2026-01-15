package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Page

interface PageRepository {
    suspend fun getPages(): List<Page>
    suspend fun savePage(page: Page, token: String): Result<Page>
    suspend fun updatePage(page: Page, token: String): Result<Page>
    suspend fun deletePage(id: String, token: String): Result<Unit>
}
