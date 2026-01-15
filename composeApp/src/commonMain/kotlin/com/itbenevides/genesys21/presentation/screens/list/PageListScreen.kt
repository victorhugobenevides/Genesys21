package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageListScreen(
    viewModel: PageViewModel, 
    onAddPage: () -> Unit, 
    onEditPage: (Page) -> Unit, 
    onViewPage: (Page) -> Unit, 
    onLogout: () -> Unit
) {
    val pages by viewModel.pages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadPages() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Páginas", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Gray) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPage, 
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(16.dp)) }
                items(pages) { page ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onViewPage(page) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4E7EC))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(page.title, fontWeight = FontWeight.Bold, color = Color(0xFF101828))
                                Text(page.id, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { onEditPage(page) }) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}
