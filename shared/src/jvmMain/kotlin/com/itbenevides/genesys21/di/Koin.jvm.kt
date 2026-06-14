package com.itbenevides.genesys21.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        // No Servidor (JVM), o AuthRepository KMP não é necessário da mesma forma que no App.
        // O servidor usa o Firebase Admin nativo configurado no Application.kt.
    }
