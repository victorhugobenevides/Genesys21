package com.itbenevides.genesys21.util

object SearchUtils {

    /**
     * Filtro fuzzy leve para sugestões de autocomplete.
     * Retorna verdadeiro se [item] contém todos os caracteres de [query] na mesma ordem,
     * ignorando maiúsculas/minúsculas e acentos.
     */
    fun fuzzyMatch(query: String, item: String): Boolean {
        if (query.isBlank()) return true
        val cleanQuery = normalize(query)
        val cleanItem = normalize(item)

        var queryIdx = 0
        var itemIdx = 0

        while (queryIdx < cleanQuery.length && itemIdx < cleanItem.length) {
            if (cleanQuery[queryIdx] == cleanItem[itemIdx]) {
                queryIdx++
            }
            itemIdx++
        }

        return queryIdx == cleanQuery.length
    }

    private fun normalize(input: String): String {
        return input.lowercase()
            .replace("á", "a")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ã", "a")
            .replace("é", "e")
            .replace("è", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ì", "i")
            .replace("î", "i")
            .replace("ó", "o")
            .replace("ò", "o")
            .replace("ô", "o")
            .replace("õ", "o")
            .replace("ú", "u")
            .replace("ù", "u")
            .replace("û", "u")
            .replace("ç", "c")
            .replace("ñ", "n")
    }
}
