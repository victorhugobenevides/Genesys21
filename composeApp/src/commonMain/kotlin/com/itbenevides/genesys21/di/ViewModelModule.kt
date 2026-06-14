package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import org.koin.dsl.module

/**
 * Módulo de injeção de dependências para a camada de apresentação.
 */
val viewModelModule =
    module {

        // PageViewModel como SINGLE para compartilhar o estado do carrinho e dados entre todas as telas
        single {
            PageViewModel(
                get(), get(), get(), get(), get(), get(), get(),
                get(), get(), get(), get(), get(),
                get(), get(), get(), get(),
                get(), get(), get(), // ADICIONADO: Use cases de categoria
            )
        }

        // O Router precisa ser single para manter o estado da navegação global
        single { Router(get()) }
    }
