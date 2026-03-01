package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.AndroidAuthRepository
import com.itbenevides.genesys21.data.repository.AndroidCustomerRepository
import com.itbenevides.genesys21.data.repository.InMemoryPageDraftRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import org.koin.dsl.module

var initialDeepLink: String? = null

actual fun platformModule() = module {
    single<AuthRepository> { AndroidAuthRepository() }
    single<CustomerRepository> { AndroidCustomerRepository() }
    single<PageDraftRepository> { InMemoryPageDraftRepository() }
}

actual fun getCurrentUrl(): String? {
    return initialDeepLink
}
