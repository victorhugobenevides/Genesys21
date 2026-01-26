package com.itbenevides.genesys21.presentation.screens.editor

import com.itbenevides.genesys21.domain.model.Page
import kotlin.random.Random

/**
 * UI State: Representa tudo o que a tela mostra.
 */
data class PageEditorState(
    val id: String = "",
    val title: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val canSave: Boolean = false
) {
    companion object {
        fun initial(page: Page?): PageEditorState {
            val id = page?.id ?: (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
            return PageEditorState(
                id = id,
                title = page?.title ?: "",
                isEditing = page != null,
                canSave = page?.title?.isNotBlank() ?: false
            )
        }
    }
}

/**
 * UI Intent (Event): Representa tudo o que o usuário faz na tela.
 */
sealed class PageEditorEvent {
    data class OnTitleChanged(val newTitle: String) : PageEditorEvent()
    object OnSaveClicked : PageEditorEvent()
    object OnBackClicked : PageEditorEvent()
}
