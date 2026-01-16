package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository

class SavePageUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        return repository.savePage(page, token, isEditing)
    }
}
