package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository

/**
 * Use case para obter a primeira página pública disponível no sistema.
 * Utilizado como fallback para a Home de visitantes não logados.
 */
class GetFirstPublicPageUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(): Page? {
        // Passar token vazio indica busca global/pública no repositório
        return repository.getPages("").firstOrNull()
    }
}
