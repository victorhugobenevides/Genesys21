package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.data.storage.SecureStorage
import com.itbenevides.genesys21.data.storage.createSecureStorage
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlinx.serialization.json.Json

actual fun platformModule(): Module =
    module {
        single<SecureStorage> { createSecureStorage() }
        single<CartRepository> {
            InMemoryCartRepository(
                get<HttpClient>(),
                getBaseUrl(),
                get<Json>(),
                get<AuthRepository>(),
                get<SecureStorage>()
            )
        }
        single<PageDraftRepository> {
            HybridPageDraftRepository(
                localRepository = InMemoryPageDraftRepository(),
                remoteRepository = get(),
                authRepository = get()
            )
        }
    }
