package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.repository.PageRepository

class DeletePageUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(
        id: String,
        token: String,
    ): Result<Unit> {
        return repository.deletePage(id, token)
    }
}
