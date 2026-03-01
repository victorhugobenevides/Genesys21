package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.data.repository.IosAuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.data.repository.InMemoryCartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.data.repository.InMemoryCustomerRepository
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import com.itbenevides.genesys21.data.repository.InMemoryPageDraftRepository
import org.koin.dsl.module
import kotlin.experimental.ExperimentalObjCName

var initialDeepLink: String? = null

@OptIn(ExperimentalObjCName::class)
@ObjCName("setInitialDeepLink")
fun setInitialDeepLink(url: String) {
    initialDeepLink = url
}

actual fun platformModule() = module {
    single<AuthRepository> { IosAuthRepository() }
    single<CartRepository> { InMemoryCartRepository() }
    single<CustomerRepository> { InMemoryCustomerRepository() }
    single<PageDraftRepository> { InMemoryPageDraftRepository() }
}

actual fun getCurrentUrl(): String? {
    return initialDeepLink
}

fun doInitKoin() = initKoin { }
