package com.itbenevides.genesys21.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidSecureStorage(context: Context) : SecureStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "genesys21_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun save(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override suspend fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override suspend fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override suspend fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}

fun createSecureStorage(context: Context): SecureStorage = AndroidSecureStorage(context)
