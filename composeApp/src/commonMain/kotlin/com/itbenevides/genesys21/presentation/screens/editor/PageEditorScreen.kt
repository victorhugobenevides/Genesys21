package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageEditorScreen(viewModel: PageViewModel, page: Page?, onBack: () -> Unit) {
    var title by remember { mutableStateOf(page?.title ?: "") }
    val id = remember { page?.id ?: (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789"[Random.nextInt(36)] }.joinToString("") }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (page != null) "Editar Página" else "Nova Página", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Voltar", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp)) {
            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("Título da Página") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val newPage = (page ?: Page(id, title)).copy(title = title)
                    viewModel.savePage(newPage, isEditing = page != null) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = title.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary) 
                else Text("Salvar Alterações", fontWeight = FontWeight.Bold)
            }
        }
    }
}
