package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.data.storage.SecureStorage
import com.itbenevides.genesys21.data.storage.createSecureStorage
import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.util.AndroidShareManager
import com.itbenevides.genesys21.util.ShareManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<SecureStorage> { createSecureStorage(androidContext()) }
        single<AuthRepository> { AndroidAuthRepository() }
        single<CartRepository> { AndroidCartRepository(androidContext(), get(), getBaseUrl(), get(), get(), get()) }
        single<CustomerRepository> { AndroidCustomerRepository() }
        single<PageDraftRepository> {
            HybridPageDraftRepository(
                localRepository = InMemoryPageDraftRepository(),
                remoteRepository = get(),
                authRepository = get()
            )
        }
        single<ShareManager> { AndroidShareManager(androidContext()) }
    }
