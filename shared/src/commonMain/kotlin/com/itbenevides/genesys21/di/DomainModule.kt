package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.usecase.*
import org.koin.dsl.module

val domainModule = module {
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
}
