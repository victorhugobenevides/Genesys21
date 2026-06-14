package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Page

interface PageDraftRepository {
    fun saveDraft(page: Page)

    fun getDraft(pageId: String): Page?

    fun clearDraft(pageId: String)
}
