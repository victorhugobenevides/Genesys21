package com.itbenevides.genesys21.data.util

interface SecureStorage {
    suspend fun save(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun remove(key: String)
    suspend fun clear()
}

expect fun createSecureStorage(context: Any? = null): SecureStorage
