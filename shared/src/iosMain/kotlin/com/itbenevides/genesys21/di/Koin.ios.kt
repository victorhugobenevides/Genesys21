package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.data.repository.IosAuthRepository
import org.koin.dsl.module

actual fun platformModule() = module {
    single<AuthRepository> { IosAuthRepository() }
}

fun doInitKoin() = initKoin { }
