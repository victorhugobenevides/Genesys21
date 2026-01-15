package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.KtorPageRepository
import com.itbenevides.genesys21.domain.repository.PageRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.koin.dsl.module

// Helpers para DI de plataforma
expect fun getBaseUrl(): String
expect fun getAuthRepository(): AuthRepository

val dataModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) { json() }
            install(HttpTimeout) { requestTimeoutMillis = 15000 }
        }
    }

    single<PageRepository> { KtorPageRepository(get(), getBaseUrl()) }
    
    // Registra o AuthRepository conforme a plataforma
    single { getAuthRepository() }
}
