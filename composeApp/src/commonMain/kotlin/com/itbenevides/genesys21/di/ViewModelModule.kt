package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import org.koin.dsl.module

/**
 * Módulo de injeção de dependências para a camada de apresentação.
 */
val viewModelModule =
    module {

        // PageViewModel como SINGLE para compartilhar o estado do carrinho e dados entre todas as telas
        single {
            PageViewModel(
                get(), get(), get(), get(), get(), get(), get(), // 1-7: Pages & Upload
                get(), get(), get(), get(), get(), // 8-12: Orders
                get(), get(), get(), get(), // 13-16: Repositories
                get(), get(), get(), // 17-19: Categories
                get(), get(), get(), // 20-22: Services
                get(), get(), get(), // 23-25: Appointments
                get(), get(), get(), // 26-28: Availability
                get(), get(), get(), get(), get(), get(), // 29-34: User Management
                get(), // 35: GetTemplates
                get(), // 36: GetAnalytics
                get(), // 37: DeleteUser
                get(), get(), get(), // 38-40: Addresses
                get(), // 41: CalculateShipping
                get(), // 42: StoreRepository
                get(), // 43: GetDomainMappings
                get(), // 44: SaveDomainMapping
                get(), // 45: DeleteDomainMapping
                get(), // 46: GetChatMessages
                get(), // 47: SendChatMessage
                get(), // 48: GetB2BAnalytics
                get()  // 49: GetAuditLogs
            )
        }

        // ReceiptViewModel para gerenciar notas fiscais
        single { com.itbenevides.genesys21.presentation.receipt.ReceiptViewModel(get(), get()) }

        // O Router precisa ser single para manter o estado da navegação global
        single { Router(get()) }
    }
