package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.KtorOrderRepository
import com.itbenevides.genesys21.data.repository.KtorPageRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.OrderRepository
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = true
            coerceInputValues = true
        }
    }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>())
            }
        }
    }
    
    single<AuthRepository> { getAuthRepository() }
    single<PageRepository> { KtorPageRepository(get(), getBaseUrl()) }
    single<OrderRepository> { KtorOrderRepository(get(), getBaseUrl()) }
}

expect fun getAuthRepository(): AuthRepository
expect fun getBaseUrl(): String
expect fun getHostname(): String 
