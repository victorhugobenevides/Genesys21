package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.di.getBaseUrl
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
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var imageUrls by remember { mutableStateOf(product?.imageUrls ?: emptyList<String>()) }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "0") }

    val isLoading by viewModel.isLoading.collectAsState()
    var isUploading by remember { mutableStateOf(false) }
    val backendUrl = remember { getBaseUrl() }

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (product == null) "Novo Produto" else "Editar Detalhes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBackIosNew, "Voltar", modifier = Modifier.size(20.dp)) }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val finalProduct = Product(
                                id = product?.id ?: "P-${Random.nextInt(1000, 9999)}",
                                name = name.trim(),
                                price = price.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                imageUrls = imageUrls,
                                description = description.trim(),
                                category = category.trim(),
                                stock = stock.toIntOrNull() ?: 0
                            )
                            onSave(finalProduct)
                        },
                        enabled = name.isNotBlank() && !isUploading && !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Salvar", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(max = 800.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // SEÇÃO DE FOTOS COM PREFIXO DE URL
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fotos do Produto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("${imageUrls.size}/5", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        items(imageUrls) { url ->
                            val fullUrl = if (url.startsWith("/")) "$backendUrl$url" else url
                            Box(modifier = Modifier.size(140.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                                AsyncImage(model = fullUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                IconButton(
                                    onClick = { imageUrls = imageUrls.filter { it != url } },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                            }
                        }
                        if (imageUrls.size < 5) {
                            item {
                                Surface(
                                    modifier = Modifier.size(140.dp).clickable { launchImagePicker() },
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                            Text("Adicionar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text("Informações Gerais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        EditorTextField(value = name, onValueChange = { name = it }, label = "Nome do Produto", icon = Icons.Default.Inventory)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            EditorTextField(value = price, onValueChange = { price = it }, label = "Preço", modifier = Modifier.weight(1f), icon = Icons.Default.Payments, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            EditorTextField(value = stock, onValueChange = { stock = it }, label = "Estoque", modifier = Modifier.weight(1f), icon = Icons.Default.Numbers, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        Box {
                            EditorTextField(value = category, onValueChange = { category = it; expanded = true }, label = "Categoria", icon = Icons.Default.Category, trailingIcon = {
                                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null) }
                            })
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(300.dp)) {
                                existingCategories.forEach { cat ->
                                    DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                                }
                            }
                        }
                        EditorTextField(value = description, onValueChange = { description = it }, label = "Descrição", icon = Icons.Default.Description, singleLine = false, minLines = 4)
                    }
                }
            }
        }
    }
}

@Composable
fun EditorTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, singleLine: Boolean = true, minLines: Int = 1, trailingIcon: @Composable (() -> Unit)? = null) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = trailingIcon, modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), singleLine = singleLine, minLines = minLines, keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    )
}
