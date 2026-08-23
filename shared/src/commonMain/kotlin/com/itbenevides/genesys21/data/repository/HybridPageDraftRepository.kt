package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.DraftRepository
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HybridPageDraftRepository(
    private val localRepository: PageDraftRepository,
    private val remoteRepository: DraftRepository,
    private val authRepository: AuthRepository
) : PageDraftRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun saveDraft(page: Page) {
        // Always save locally first
        localRepository.saveDraft(page)

        // Sync to cloud if logged in
        scope.launch {
            authRepository.getCurrentUserToken()?.let { token ->
                remoteRepository.saveDraft(page, token)
            }
        }
    }

    override fun getDraft(pageId: String): Page? {
        // Try local first for speed
        val local = localRepository.getDraft(pageId)
        if (local != null) return local

        // In a real scenario, we might want to block or use a Flow here.
        // For now, ViewModel will handle remote fetching explicitly if needed.
        return null
    }

    override fun clearDraft(pageId: String) {
        localRepository.clearDraft(pageId)
        scope.launch {
            authRepository.getCurrentUserToken()?.let { token ->
                remoteRepository.deleteDraft(pageId, token)
            }
        }
    }

    suspend fun syncFromRemote(pageId: String): Page? {
        val token = authRepository.getCurrentUserToken() ?: return null
        return remoteRepository.getDraft(pageId, token).getOrNull()?.also {
            localRepository.saveDraft(it)
        }
    }
}
