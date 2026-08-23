package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.data.storage.SecureStorage
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

        // SecureStorage is provided by platformModule
        // single<SecureStorage> { ... }

        single<AuthRepository> { getAuthRepository() }
        single<PageRepository> { KtorPageRepository(get(), getBaseUrl()) }
        single<OrderRepository> { KtorOrderRepository(get(), getBaseUrl()) }
        single<BookingRepository> { KtorBookingRepository(get(), getBaseUrl()) }
        single<UserRepository> { KtorUserRepository(get(), getBaseUrl(), get()) }
        single<AddressRepository> { KtorAddressRepository(get(), getBaseUrl(), get()) }
        single<ShippingRepository> { KtorShippingRepository(get(), getBaseUrl()) }
        single<StoreRepository> { KtorStoreRepository(get(), getBaseUrl(), get()) }
        single<DomainRepository> { KtorDomainRepository(get()) }
        single<ChatRepository> { KtorChatRepository(get(), getBaseUrl()) }
        single<DraftRepository> { KtorDraftRepository(get(), getBaseUrl()) }

        // UseCases
        single { GetDomainMappingsUseCase(get()) }
        single { SaveDomainMappingUseCase(get()) }
        single { DeleteDomainMappingUseCase(get()) }
        single { GetChatMessagesUseCase(get()) }
        single { SendChatMessageUseCase(get()) }
        single { GetUserProfileUseCase(get()) }
        single { SaveUserProfileUseCase(get()) }
        single { GetAllUsersUseCase(get()) }
        single { UpdateUserRoleUseCase(get()) }
        single { UpdateUserStatusUseCase(get()) }
        single { UpdateUserPermissionsUseCase(get()) }
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
        single { GetAnalyticsUseCase(get()) }
        single { DeleteUserUseCase(get()) }

        single<ReceiptRepository> {
            // Para Wasm e Android em produção, usamos o backend.
            // Para testes ou local legacional, poderíamos alternar aqui.
            com.itbenevides.genesys21.data.repository.KtorReceiptRepository(get(), getBaseUrl(), get())
        }
        single { com.itbenevides.genesys21.domain.service.ReceiptParserService(get(), getBaseUrl()) }
        single { com.itbenevides.genesys21.domain.service.PageAIGeneratorService(get(), getBaseUrl()) }
    }

expect fun getAuthRepository(): AuthRepository

expect fun getBaseUrl(): String

expect fun getHostname(): String
