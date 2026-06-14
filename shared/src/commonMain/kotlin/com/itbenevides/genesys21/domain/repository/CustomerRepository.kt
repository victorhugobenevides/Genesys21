package com.itbenevides.genesys21.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface CustomerRepository {
    val customerName: StateFlow<String>
    val customerPhone: StateFlow<String>

    suspend fun saveName(name: String)

    suspend fun savePhone(phone: String)

    suspend fun loadData()

    suspend fun loadName() // Mantido por compatibilidade
}
