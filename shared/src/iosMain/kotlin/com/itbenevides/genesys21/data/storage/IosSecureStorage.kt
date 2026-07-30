package com.itbenevides.genesys21.data.storage

import platform.Foundation.NSUserDefaults

class IosSecureStorage : SecureStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun save(key: String, value: String) {
        defaults.setObject(value, key)
    }

    override suspend fun get(key: String): String? {
        return defaults.stringForKey(key)
    }

    override suspend fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    override suspend fun clear() {
        // Implementation for clearing all keys would need a registry of keys
    }
}
