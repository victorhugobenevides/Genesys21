package com.itbenevides.genesys21.data.storage

class JsSecureStorage : SecureStorage {
    override suspend fun save(key: String, value: String) {
        // Mock
    }

    override suspend fun get(key: String): String? {
        return null
    }

    override suspend fun remove(key: String) {
        // Mock
    }

    override suspend fun clear() {
        // Mock
    }
}

actual fun createSecureStorage(context: Any?): SecureStorage = JsSecureStorage()
