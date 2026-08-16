package com.itbenevides.genesys21.data.util

import platform.Foundation.*

/**
 * iOS implementation of SecureStorage.
 * Note: For a production app, Keychain is mandatory.
 * This implementation uses NSUserDefaults as a placeholder for the multiplatform structure.
 */
class IosSecureStorage : SecureStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun save(key: String, value: String) {
        defaults.setObject(value, "secure_$key")
        defaults.synchronize()
    }

    override suspend fun get(key: String): String? {
        return defaults.stringForKey("secure_$key")
    }

    override suspend fun remove(key: String) {
        defaults.removeObjectForKey("secure_$key")
        defaults.synchronize()
    }

    override suspend fun clear() {
        val keys = defaults.dictionaryRepresentation().keys
        keys.filter { (it as? String)?.startsWith("secure_") == true }.forEach {
            defaults.removeObjectForKey(it as String)
        }
        defaults.synchronize()
    }
}

actual fun createSecureStorage(context: Any?): SecureStorage = IosSecureStorage()
