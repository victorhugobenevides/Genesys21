package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import org.koin.dsl.module

val viewModelModule = module {
    // PageViewModel como SINGLE para compartilhar o estado do carrinho entre todas as telas
    single { PageViewModel(get(), get(), get(), get(), get(), get()) }

    // O Router também precisa ser single para manter o estado da navegação
    single { Router(get()) }
}
