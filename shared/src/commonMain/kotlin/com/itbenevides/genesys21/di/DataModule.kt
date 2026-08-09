package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.KtorBookingRepository
import com.itbenevides.genesys21.data.repository.KtorOrderRepository
import com.itbenevides.genesys21.data.repository.KtorPageRepository
import com.itbenevides.genesys21.data.repository.KtorUserRepository
import com.itbenevides.genesys21.data.repository.KtorAddressRepository
import com.itbenevides.genesys21.data.repository.KtorShippingRepository
import com.itbenevides.genesys21.data.repository.KtorStoreRepository
import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.domain.usecase.*
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
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            encodeDefaults = true
                            coerceInputValues = true
                        }
                    )
                }
            }
        }

        single<AuthRepository> { getAuthRepository() }
        single<PageRepository> { KtorPageRepository(get(), getBaseUrl()) }
        single<OrderRepository> { KtorOrderRepository(get(), getBaseUrl()) }
        single<BookingRepository> { KtorBookingRepository(get(), getBaseUrl()) }
        single<UserRepository> { KtorUserRepository(get(), getBaseUrl()) }
        single<AddressRepository> { KtorAddressRepository(get(), getBaseUrl(), get()) }
        single<ShippingRepository> { KtorShippingRepository(get(), getBaseUrl()) }
        single<StoreRepository> { KtorStoreRepository(get(), getBaseUrl(), get()) }

        // UseCases
        single { GetUserProfileUseCase(get()) }
        single { SaveUserProfileUseCase(get()) }
        single { GetAllUsersUseCase(get()) }
        single { UpdateUserRoleUseCase(get()) }
        single { UpdateUserStatusUseCase(get()) }
        single { GetAvailabilityUseCase(get()) }
        single { SaveAvailabilityUseCase(get()) }
        single { GetBookingServicesUseCase(get()) }
        single { SaveBookingServiceUseCase(get()) }
        single { DeleteBookingServiceUseCase(get()) }
        single { GetAppointmentsUseCase(get()) }
        single { CreateAppointmentUseCase(get()) }
        single { UpdateAppointmentUseCase(get()) }
        single { ValidateBookingSlotUseCase(get()) }
        single { GetPagesUseCase(get()) }
        single { SavePageUseCase(get()) }
        single { DeletePageUseCase(get()) }
        single { GetPublicPageUseCase(get()) }
        single { GetPageByDomainUseCase(get()) }
        single { GetFirstPublicPageUseCase(get()) }
        single { UploadImageUseCase(get()) }
        single { GetOrdersUseCase(get()) }
        single { GetCustomerOrdersUseCase(get()) }
        single { GetOrderByIdUseCase(get()) }
        single { SubmitOrderUseCase(get()) }
        single { UpdateOrderStatusUseCase(get()) }
        single { GetCategoriesUseCase(get()) }
        single { SaveCategoryUseCase(get()) }
        single { DeleteCategoryUseCase(get()) }
        single { GetAddressesUseCase(get()) }
        single { SaveAddressUseCase(get()) }
        single { DeleteAddressUseCase(get()) }
        single { CalculateShippingUseCase(get()) }
        single { com.itbenevides.genesys21.data.repository.ReceiptLocalRepository() }
        single { com.itbenevides.genesys21.domain.service.ReceiptParserService(get(), getBaseUrl()) }
    }

expect fun getAuthRepository(): AuthRepository

expect fun getBaseUrl(): String

expect fun getHostname(): String
