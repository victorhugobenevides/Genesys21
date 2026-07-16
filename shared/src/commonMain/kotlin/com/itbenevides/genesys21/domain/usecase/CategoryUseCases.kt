package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.domain.repository.PageRepository

class GetCategoriesUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(token: String): Result<List<Category>> = repository.getCategories(token)
}

class SaveCategoryUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(
        category: Category,
        token: String,
    ): Result<Unit> = repository.saveCategory(category, token)
}

class DeleteCategoryUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(
        id: Int,
        token: String,
    ): Result<Unit> = repository.deleteCategory(id, token)
}
