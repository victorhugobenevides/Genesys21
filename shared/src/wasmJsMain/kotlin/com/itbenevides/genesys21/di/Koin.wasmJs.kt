package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.LocalStoragePageDraftRepository
import com.itbenevides.genesys21.data.repository.WasmAuthRepository
import com.itbenevides.genesys21.data.repository.WasmCartRepository
import com.itbenevides.genesys21.data.repository.WasmCustomerRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { WasmAuthRepository() } bind AuthRepository::class
    single { WasmCartRepository() } bind CartRepository::class
    single { WasmCustomerRepository() } bind CustomerRepository::class
    single<PageDraftRepository> { LocalStoragePageDraftRepository(get()) }
}
