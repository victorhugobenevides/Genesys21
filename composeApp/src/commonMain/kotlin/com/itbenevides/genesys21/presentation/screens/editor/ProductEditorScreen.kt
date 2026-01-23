package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.util.rememberImagePicker
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditorScreen(
    viewModel: PageViewModel,
    product: Product?,
    existingCategories: List<String>,
    onSave: (Product) -> Unit,
    onBack: () -> Unit
) {
    // Estados estabilizados para evitar bugs de input no WasmJs
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var imageUrls by remember { mutableStateOf(product?.imageUrls ?: emptyList<String>()) }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "0") }

    val isLoading by viewModel.isLoading.collectAsState()
    var isUploading by remember { mutableStateOf(false) }

    val launchImagePicker = rememberImagePicker { bytes ->
        bytes?.let {
            if (imageUrls.size < 5) {
                isUploading = true
                viewModel.uploadImage(it, "prod_${Random.nextInt()}.jpg") { uploadedUrl ->
                    imageUrls = imageUrls + uploadedUrl
                    isUploading = false
                }
            }
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (product == null) "Novo Produto" else "Editar Produto", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Voltar", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val finalProduct = Product(
                                id = product?.id ?: Random.nextInt().toString(),
                                name = name.trim(),
                                price = price.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                imageUrls = imageUrls,
                                description = description.trim(),
                                category = category.trim(),
                                stock = stock.toIntOrNull() ?: 0
                            )
                            onSave(finalProduct)
                        },
                        enabled = name.isNotBlank() && !isUploading && !isLoading,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Concluir", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // SEÇÃO DE FOTOS
                SectionHeader("Fotos do Produto", "${imageUrls.size}/5")
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(imageUrls) { url ->
                            Box(modifier = Modifier.size(120.dp)) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    onClick = { imageUrls = imageUrls.filter { it != url } },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp),
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.6f),
                                    contentColor = Color.White
                                ) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                        
                        if (imageUrls.size < 5) {
                            item {
                                Surface(
                                    modifier = Modifier.size(120.dp).clickable { launchImagePicker() },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isUploading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                                Text("Adicionar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SEÇÃO DE INFORMAÇÕES
                SectionHeader("Informações Gerais")
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it }, 
                            label = { Text("Nome do Produto") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Preço") },
                                prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = { Text("Estoque") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                        }

                        Column {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it; expanded = it.isNotEmpty() },
                                label = { Text("Categoria") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.width(300.dp)
                            ) {
                                existingCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = { category = cat; expanded = false }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it }, // Atribuição direta sem processamento
                            label = { Text("Descrição") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, badge: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (badge != null) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
