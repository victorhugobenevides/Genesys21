package com.itbenevides.genesys21.data.util

@JsFun("(key) => window.localStorage.getItem(key)")
external fun localStorageGetItem(key: String): String?

@JsFun("(key, value) => window.localStorage.setItem(key, value)")
external fun localStorageSetItem(key: String, value: String)

@JsFun("(key) => window.localStorage.removeItem(key)")
external fun localStorageRemoveItem(key: String)

@JsFun("() => window.localStorage.clear()")
external fun localStorageClear()

class WasmSecureStorage : SecureStorage {
    override suspend fun save(key: String, value: String) {
        localStorageSetItem(key, value)
    }

    override suspend fun get(key: String): String? {
        return localStorageGetItem(key)
    }

    override suspend fun remove(key: String) {
        localStorageRemoveItem(key)
    }

    override suspend fun clear() {
        localStorageClear()
    }
}

actual fun createSecureStorage(context: Any?): SecureStorage = WasmSecureStorage()
