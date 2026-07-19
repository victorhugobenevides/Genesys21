package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.PageTemplate
import com.itbenevides.genesys21.domain.model.PageTemplateRegistry

class GetTemplatesUseCase {
    operator fun invoke(): List<PageTemplate> = PageTemplateRegistry.templates
}
