package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository

class GetPagesUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(): List<Page> = repository.getPages()
}
