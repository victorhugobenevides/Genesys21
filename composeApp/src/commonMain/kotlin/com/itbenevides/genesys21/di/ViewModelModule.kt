package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.presentation.PageViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { PageViewModel(get(), get(), get(), get(), get()) }
}
