package com.itbenevides.genesys21.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface CustomerRepository {
    val customerName: StateFlow<String>
    suspend fun saveName(name: String)
    suspend fun loadName()
}
