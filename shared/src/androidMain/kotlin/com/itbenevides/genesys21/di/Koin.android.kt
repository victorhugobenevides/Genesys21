package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.data.repository.AndroidAuthRepository
import com.itbenevides.genesys21.data.repository.AndroidCartRepository
import com.itbenevides.genesys21.data.repository.AndroidCustomerRepository
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

actual fun platformModule() = module {
    single<AuthRepository> { AndroidAuthRepository() }
    single<CartRepository> { 
        AndroidCartRepository(
            context = androidContext(),
            httpClient = get(),
            baseUrl = getBaseUrl(),
            json = get(),
            authRepository = get()
        ) 
    }
    single<CustomerRepository> { AndroidCustomerRepository() }
}
