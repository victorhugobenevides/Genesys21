// CORREÇÃO FINAL E RADICAL: Classe renomeada e instanciação manual.
@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.WasmCartRepository
import com.itbenevides.genesys21.data.repository.LocalStorageCustomerRepository
import com.itbenevides.genesys21.data.repository.LocalStoragePageDraftRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import org.koin.dsl.module

@JsFun("() => window.location.href")
external fun getWindowLocationHref(): String

actual fun platformModule() = module {
    single<PageDraftRepository> { LocalStoragePageDraftRepository(get()) }
    
    // Usando a nova classe WasmCartRepository para limpar o cache de símbolos do compilador IR
    single<CartRepository> { WasmCartRepository() }
    
    single<CustomerRepository> { LocalStorageCustomerRepository() }
}

actual fun getCurrentUrl(): String? {
    return try {
        getWindowLocationHref()
    } catch (e: Exception) {
        null
    }
}
