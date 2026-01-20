package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository

/**
 * Use case para buscar uma página através de um domínio customizado.
 * Utilizado principalmente para o redirecionamento inteligente de visitantes.
 */
class GetPageByDomainUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(domain: String): Result<Page> {
        return repository.getPageByDomain(domain)
    }
}
