package com.itbenevides.genesys21.domain.util

import com.itbenevides.genesys21.domain.model.Receipt
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Serializa a lista de notas fiscais em uma string JSON formatada.
     */
    fun exportToJson(receipts: List<Receipt>): String {
        return json.encodeToString(receipts)
    }

    /**
     * Desserializa a string JSON de volta para a lista de notas fiscais.
     */
    fun importFromJson(jsonString: String): Result<List<Receipt>> {
        return runCatching {
            json.decodeFromString<List<Receipt>>(jsonString)
        }
    }
}
