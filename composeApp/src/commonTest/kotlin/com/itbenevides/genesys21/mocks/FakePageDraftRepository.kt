package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageDraftRepository

class FakePageDraftRepository : PageDraftRepository {
    private val drafts = mutableMapOf<String, Page>()

    override fun saveDraft(page: Page) {
        drafts[page.id] = page
    }

    override fun getDraft(pageId: String): Page? {
        return drafts[pageId]
    }

    override fun clearDraft(pageId: String) {
        drafts.remove(pageId)
    }
}
