package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.KtorBookingRepository
import com.itbenevides.genesys21.data.repository.KtorOrderRepository
import com.itbenevides.genesys21.data.repository.KtorPageRepository
import com.itbenevides.genesys21.data.service.GoogleCalendarService
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.BookingRepository
import com.itbenevides.genesys21.domain.repository.OrderRepository
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.koin.dsl.module
import kotlinx.serialization.json.Json

val dataModule =
    module {
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
        single<BookingRepository> { KtorBookingRepository(get(), getBaseUrl()) }
        single { GoogleCalendarService(get(), get()) }
    }

expect fun getAuthRepository(): AuthRepository

expect fun getBaseUrl(): String

expect fun getHostname(): String
