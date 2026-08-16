package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.InMemoryCartRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import io.ktor.client.HttpClient
import com.itbenevides.genesys21.data.util.createSecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlinx.serialization.json.Json

actual fun platformModule(): Module =
    module {
        single { createSecureStorage() }
        single<CartRepository> {
            InMemoryCartRepository(
                get<HttpClient>(),
                getBaseUrl(),
                get<Json>(),
                get<AuthRepository>(),
            )
        }
    }
