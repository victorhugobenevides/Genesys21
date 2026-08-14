package com.itbenevides.genesys21.ui.components.molecules.input

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * GenesysSearchBar: Refactored to use official Material Design 3 SearchBar.
 * Provides a modern, expanding search experience with proper focus states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesysSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    onActiveChange: (Boolean) -> Unit = {},
    onSearch: (String) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = active,
        onActiveChange = onActiveChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpar")
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (active) 0.dp else 16.dp),
        colors = SearchBarDefaults.colors(
            containerColor = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            dividerColor = MaterialTheme.colorScheme.outlineVariant
        ),
        content = content
    )
}
