package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.data.repository.WasmAuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.data.repository.LocalStorageCartRepository
import org.koin.dsl.module

actual fun platformModule() = module {
    single<AuthRepository> { WasmAuthRepository() }
    single<CartRepository> { LocalStorageCartRepository(get(), getBaseUrl(), get(), get()) }
}
