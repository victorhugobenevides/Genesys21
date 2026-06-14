package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.AndroidAuthRepository
import com.itbenevides.genesys21.data.repository.AndroidCartRepository
import com.itbenevides.genesys21.data.repository.AndroidCustomerRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<AuthRepository> { AndroidAuthRepository() }
        single<CartRepository> { AndroidCartRepository() }
        single<CustomerRepository> { AndroidCustomerRepository() }
    }
