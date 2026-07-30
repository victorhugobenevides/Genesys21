package com.itbenevides.genesys21.data.storage

@JsFun("(key) => window.localStorage.getItem(key)")
private external fun jsGetItem(key: String): String?

@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun jsSetItem(key: String, value: String)

@JsFun("(key) => window.localStorage.removeItem(key)")
private external fun jsRemoveItem(key: String)

@JsFun("() => window.localStorage.clear()")
private external fun jsClear()

class WasmSecureStorage : SecureStorage {
    override suspend fun save(key: String, value: String) {
        jsSetItem(key, value)
    }

    override suspend fun get(key: String): String? {
        return jsGetItem(key)
    }

    override suspend fun remove(key: String) {
        jsRemoveItem(key)
    }

    override suspend fun clear() {
        jsClear()
    }
}
