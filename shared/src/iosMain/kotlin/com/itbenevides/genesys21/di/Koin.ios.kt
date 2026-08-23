package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.InMemoryCartRepository
import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.data.storage.SecureStorage
import com.itbenevides.genesys21.data.storage.createSecureStorage
import com.itbenevides.genesys21.domain.repository.*
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<SecureStorage> { createSecureStorage() }
        single<AuthRepository> { IosAuthRepository() }
        single<CartRepository> { InMemoryCartRepository(get(), getBaseUrl(), get(), get(), get()) }
        single<CustomerRepository> { InMemoryCustomerRepository() }
        single<PageDraftRepository> {
            HybridPageDraftRepository(
                localRepository = InMemoryPageDraftRepository(),
                remoteRepository = get(),
                authRepository = get()
            )
        }
    }

fun doInitKoin() = initKoin { }
