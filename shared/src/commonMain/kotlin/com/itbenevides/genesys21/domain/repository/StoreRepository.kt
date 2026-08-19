package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Store
import kotlinx.serialization.Serializable

@Serializable
data class ConnectAccountRequest(val storeId: String, val email: String)

@Serializable
data class ConnectLinkResponse(val url: String)

@Serializable
data class AccountSessionRequest(val storeId: String)

@Serializable
data class AccountSessionResponse(val clientSecret: String)

interface StoreRepository {
    suspend fun getStore(id: String): Result<Store>
    suspend fun saveStore(store: Store, token: String): Result<Unit>

    // Stripe Connect
    suspend fun createConnectAccount(storeId: String, email: String, token: String): Result<String>
    suspend fun getConnectOnboardingLink(storeId: String, token: String): Result<String>
    suspend fun getConnectLoginLink(storeId: String, token: String): Result<String>
    suspend fun getAccountSession(storeId: String, token: String): Result<String>
}
