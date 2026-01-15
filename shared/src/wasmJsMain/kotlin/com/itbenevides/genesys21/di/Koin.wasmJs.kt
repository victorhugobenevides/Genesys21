package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.data.repository.WasmAuthRepository
import org.koin.dsl.module

actual fun platformModule() = module {
    single<AuthRepository> { WasmAuthRepository() }
}
