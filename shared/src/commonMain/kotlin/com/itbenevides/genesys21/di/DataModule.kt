package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.KtorPageRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    // Definimos uma instância única de Json configurada para ser resiliente
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
                // Usamos a instância de Json definida acima
                json(get<Json>())
            }
        }
    }
    
    single<AuthRepository> { getAuthRepository() }
    single<PageRepository> { KtorPageRepository(get(), getBaseUrl()) }
}

expect fun getAuthRepository(): AuthRepository
expect fun getBaseUrl(): String
