package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.InMemoryCartRepository
import com.itbenevides.genesys21.data.repository.InMemoryCustomerRepository
import com.itbenevides.genesys21.data.repository.IosAuthRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<AuthRepository> { IosAuthRepository() }
        single<CartRepository> { InMemoryCartRepository() }
        single<CustomerRepository> { InMemoryCustomerRepository() }
    }

fun doInitKoin() = initKoin { }
