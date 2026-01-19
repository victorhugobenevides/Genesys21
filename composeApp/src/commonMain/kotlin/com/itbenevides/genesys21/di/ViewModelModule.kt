package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import org.koin.dsl.module

val viewModelModule = module {
    // O Router precisa ser single para manter o estado da navegação
    single { Router(get()) }
    
    // PageViewModel como factory ou single conforme necessidade
    factory { PageViewModel(get(), get(), get(), get(), get(), get()) }
}
