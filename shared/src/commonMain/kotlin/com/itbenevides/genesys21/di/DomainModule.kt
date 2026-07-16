package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.usecase.*
import org.koin.dsl.module

val domainModule =
    module {
        single { GetPagesUseCase(get()) }
        single { SavePageUseCase(get()) }
        single { DeletePageUseCase(get()) }
        single { GetPublicPageUseCase(get()) }
        single { GetPageByDomainUseCase(get()) }
        single { GetFirstPublicPageUseCase(get()) }

        single { GetOrdersUseCase(get()) }
        single { GetCustomerOrdersUseCase(get()) }
        single { GetOrderByIdUseCase(get()) }
        single { SubmitOrderUseCase(get()) }
        single { UpdateOrderStatusUseCase(get()) }

        single { UploadImageUseCase(get()) }

        // Category Use Cases
        single { GetCategoriesUseCase(get()) }
        single { SaveCategoryUseCase(get()) }
        single { DeleteCategoryUseCase(get()) }

        // Booking Use Cases
        single { GetBookingServicesUseCase(get()) }
        single { SaveBookingServiceUseCase(get()) }
        single { DeleteBookingServiceUseCase(get()) }
        single { GetAvailabilityUseCase(get()) }
        single { SaveAvailabilityUseCase(get()) }
        single { GetAppointmentsUseCase(get()) }
        single { CreateAppointmentUseCase(get()) }
        single { UpdateAppointmentUseCase(get()) }
        single { ValidateBookingSlotUseCase(get()) }
    }
