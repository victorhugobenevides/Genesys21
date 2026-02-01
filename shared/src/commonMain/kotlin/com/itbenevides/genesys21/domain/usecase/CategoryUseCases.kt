package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.domain.repository.PageRepository

class GetCategoriesUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(token: String) = repository.getCategories(token)
}

class SaveCategoryUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(category: Category, token: String) = repository.saveCategory(category, token)
}

class DeleteCategoryUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(id: Int, token: String) = repository.deleteCategory(id, token)
}
