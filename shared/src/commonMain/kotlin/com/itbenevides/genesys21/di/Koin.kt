package com.itbenevides.genesys21.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    additionalModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
) = startKoin {
    appDeclaration()
    modules(
        dataModule,
        domainModule,
        platformModule(),
        *additionalModules.toTypedArray(),
    )
}

expect fun platformModule(): Module
