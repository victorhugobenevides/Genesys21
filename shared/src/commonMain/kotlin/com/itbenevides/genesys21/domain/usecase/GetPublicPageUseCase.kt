package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository

class GetPublicPageUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(id: String): Result<Page> {
        return repository.getPublicPage(id)
    }
}
