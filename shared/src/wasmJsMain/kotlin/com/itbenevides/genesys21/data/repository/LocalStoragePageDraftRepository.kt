package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Usando declarações externas para WasmJs quando kotlinx.browser não está disponível
external val localStorage: Storage

external interface Storage {
    val length: Int

    fun clear()

    fun getItem(key: String): String?

    fun key(index: Int): String?

    fun removeItem(key: String)

    fun setItem(
        key: String,
        value: String,
    )
}

class LocalStoragePageDraftRepository(
    private val json: Json,
) : PageDraftRepository {
    private val PREFIX = "page_draft_"

    override fun saveDraft(page: Page) {
        try {
            localStorage.setItem(PREFIX + page.id, json.encodeToString(page))
        } catch (e: Exception) {
            println("Draft: Falha ao salvar - \${e.message}")
        }
    }

    override fun getDraft(pageId: String): Page? {
        val cached = localStorage.getItem(PREFIX + pageId) ?: return null
        return try {
            json.decodeFromString<Page>(cached)
        } catch (e: Exception) {
            null
        }
    }

    override fun clearDraft(pageId: String) {
        localStorage.removeItem(PREFIX + pageId)
    }
}
