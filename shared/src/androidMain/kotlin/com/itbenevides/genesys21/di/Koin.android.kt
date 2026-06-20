package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.AndroidAuthRepository
import com.itbenevides.genesys21.data.repository.AndroidCartRepository
import com.itbenevides.genesys21.data.repository.AndroidCustomerRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.util.AndroidShareManager
import com.itbenevides.genesys21.util.ShareManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<AuthRepository> { AndroidAuthRepository() }
        single<CartRepository> { AndroidCartRepository(androidContext(), get(), getBaseUrl(), get(), get()) }
        single<CustomerRepository> { AndroidCustomerRepository() }
        single<ShareManager> { AndroidShareManager(androidContext()) }
    }
