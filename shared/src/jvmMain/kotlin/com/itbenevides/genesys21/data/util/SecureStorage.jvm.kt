package com.itbenevides.genesys21.data.util

import java.util.prefs.Preferences

class JvmSecureStorage : SecureStorage {
    private val prefs = Preferences.userRoot().node("genesys21")

    override suspend fun save(key: String, value: String) {
        prefs.put(key, value)
    }

    override suspend fun get(key: String): String? {
        return prefs.get(key, null)
    }

    override suspend fun remove(key: String) {
        prefs.remove(key)
    }

    override suspend fun clear() {
        prefs.clear()
    }
}

actual fun createSecureStorage(context: Any?): SecureStorage = JvmSecureStorage()
