package com.itbenevides.genesys21.domain.service

import com.itbenevides.genesys21.util.SearchUtils

interface SuggestionProvider {
    fun getSuggestions(query: String): List<String>
}

class StaticSuggestionProvider(private val items: List<String>) : SuggestionProvider {
    override fun getSuggestions(query: String): List<String> {
        return items.filter { SearchUtils.fuzzyMatch(query, it) }.take(5)
    }
}
