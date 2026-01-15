package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.usecase.DeletePageUseCase
import com.itbenevides.genesys21.domain.usecase.GetPagesUseCase
import com.itbenevides.genesys21.domain.usecase.SavePageUseCase
import org.koin.dsl.module

val domainModule = module {
    single { GetPagesUseCase(get()) }
    single { SavePageUseCase(get()) }
    single { DeletePageUseCase(get()) }
}
