package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.data.util.createSecureStorage
import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single { createSecureStorage() }
        single<CartRepository> { LocalStorageCartRepository(get(), getBaseUrl(), get(), get()) }
        single<CustomerRepository> { LocalStorageCustomerRepository() }
        single<PageDraftRepository> { LocalStoragePageDraftRepository(get()) }
    }
