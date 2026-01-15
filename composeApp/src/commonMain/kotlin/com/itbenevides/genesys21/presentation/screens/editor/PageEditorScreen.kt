package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageEditorScreen(viewModel: PageViewModel, page: Page?, onBack: () -> Unit) {
    var title by remember { mutableStateOf(page?.title ?: "") }
    val id = remember { page?.id ?: (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789"[Random.nextInt(36)] }.joinToString("") }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (page != null) "Editar" else "Novo", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(32.dp).fillMaxSize()) {
            Text(if (page != null) "Altere os detalhes" else "Crie um novo item", color = Color.Gray)
            Spacer(modifier = Modifier.height(48.dp))
            
            Text("Título", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            TextField(
                value = title, 
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.LightGray
                ),
                textStyle = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("ID: $id", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
            
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    val newPage = (page ?: Page(id, title)).copy(title = title)
                    viewModel.savePage(newPage, isEditing = page != null) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = title.isNotBlank() && !isLoading,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White) 
                else Text("Salvar Alterações", fontWeight = FontWeight.Bold)
            }
        }
    }
}
