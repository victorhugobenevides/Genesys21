package com.itbenevides.genesys21.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "genesys21_cart_prefs")

class AndroidCartRepository(
    private val context: Context,
    httpClient: HttpClient,
    baseUrl: String,
    json: Json,
    authRepository: AuthRepository,
    secureStorage: com.itbenevides.genesys21.data.storage.SecureStorage,
) : BaseCartRepository(httpClient, baseUrl, json, authRepository, secureStorage) {

    private val CART_KEY = stringPreferencesKey("genesys21_cart")

    override suspend fun saveToLocal(items: List<CartItem>) {
        context.dataStore.edit { preferences ->
            preferences[CART_KEY] = json.encodeToString(items)
        }
    }

    override suspend fun loadFromLocal(): List<CartItem> {
        val jsonString = context.dataStore.data.map { it[CART_KEY] }.first() ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
