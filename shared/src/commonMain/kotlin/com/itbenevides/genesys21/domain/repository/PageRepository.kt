package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Page

interface PageRepository {
    suspend fun getPages(token: String): List<Page>
    suspend fun getPublicPage(id: String): Result<Page>
    suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit>
    suspend fun deletePage(id: String, token: String): Result<Unit>
}
