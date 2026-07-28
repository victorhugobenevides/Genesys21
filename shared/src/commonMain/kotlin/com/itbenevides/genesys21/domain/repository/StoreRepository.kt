package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Store

interface StoreRepository {
    suspend fun getStore(id: String): Result<Store>
    suspend fun saveStore(store: Store, token: String): Result<Unit>
}
