package com.itbenevides.genesys21

import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.mocks.*
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.login.LoginViewModel
import org.koin.dsl.module
import org.koin.dsl.bind

fun createTestModule(
    fakeAuth: FakeAuthRepository = FakeAuthRepository(),
    fakePage: FakePageRepository = FakePageRepository(),
    fakeOrder: FakeOrderRepository = FakeOrderRepository()
) = module {
    // Repositórios Fakes
    single { fakeAuth } bind AuthRepository::class
    single { fakePage } bind PageRepository::class
    single { fakeOrder } bind OrderRepository::class
    
    single { FakeCartRepository() } bind CartRepository::class
    single { FakeCustomerRepository() } bind CustomerRepository::class
    single { FakePageDraftRepository() } bind PageDraftRepository::class

    // UseCases
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

    // PageViewModel
    single { 
        PageViewModel(
            get(), get(), get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get(),
            get(), get(), get(), get(),
            get(), get(), get(),
            get() // OrderRepository
        )
    }
    
    // LoginViewModel
    single { LoginViewModel(get(), get()) }
    
    // Router
    single { Router(get()) }
}
