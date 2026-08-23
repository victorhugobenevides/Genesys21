package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Page

interface DraftRepository {
    suspend fun saveDraft(page: Page, token: String): Result<Unit>
    suspend fun getDraft(pageId: String, token: String): Result<Page?>
    suspend fun deleteDraft(pageId: String, token: String): Result<Unit>
}
