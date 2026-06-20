package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.LocalStorageCartRepository
import com.itbenevides.genesys21.data.repository.LocalStorageCustomerRepository
import com.itbenevides.genesys21.data.repository.LocalStoragePageDraftRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single<CartRepository> { LocalStorageCartRepository(get(), getBaseUrl(), get(), get()) }
        single<CustomerRepository> { LocalStorageCustomerRepository() }
        single<PageDraftRepository> { LocalStoragePageDraftRepository(get()) }
    }
