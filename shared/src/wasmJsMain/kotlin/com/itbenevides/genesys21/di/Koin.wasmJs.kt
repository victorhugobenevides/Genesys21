package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.data.storage.SecureStorage
import com.itbenevides.genesys21.data.storage.createSecureStorage
import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single<SecureStorage> { createSecureStorage() }
        single<CartRepository> { LocalStorageCartRepository(get(), getBaseUrl(), get(), get(), get()) }
        single<CustomerRepository> { LocalStorageCustomerRepository() }
        single<PageDraftRepository> {
            HybridPageDraftRepository(
                localRepository = LocalStoragePageDraftRepository(get()),
                remoteRepository = get(),
                authRepository = get()
            )
        }
    }
