package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.JsAuthRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<AuthRepository> { JsAuthRepository() }
    }
