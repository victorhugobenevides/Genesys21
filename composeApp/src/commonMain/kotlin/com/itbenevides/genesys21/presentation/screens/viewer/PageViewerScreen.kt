package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.PageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageViewerScreen(viewModel: PageViewModel, page: Page, onBack: () -> Unit) {
    var currentPage by remember { mutableStateOf(page) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPage.title, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.savePage(currentPage, true) {} }) { Icon(Icons.Default.Save, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                IconButton(onClick = { currentPage = currentPage.copy(components = currentPage.components + PageComponent.Header("Novo Título")) }) {
                    Icon(Icons.Default.Title, null)
                }
                IconButton(onClick = { currentPage = currentPage.copy(components = currentPage.components + PageComponent.Text("Novo Texto")) }) {
                    Icon(Icons.AutoMirrored.Filled.Notes, null)
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(currentPage.components) { component ->
                when (component) {
                    is PageComponent.Header -> Text(component.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    is PageComponent.Text -> Text(component.content)
                    is PageComponent.Image -> Text("[Imagem]")
                }
            }
        }
    }
}
