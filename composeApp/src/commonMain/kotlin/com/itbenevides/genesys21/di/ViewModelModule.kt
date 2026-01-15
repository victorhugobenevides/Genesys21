package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.presentation.PageViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::PageViewModel)
}
