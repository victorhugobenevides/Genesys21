package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.InMemoryCartRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.dsl.module

actual fun platformModule() = module {
    single<CartRepository> { 
        InMemoryCartRepository(
            get<HttpClient>(),
            getBaseUrl(),
            get<Json>(),
            get<AuthRepository>()
        ) 
    }
}
