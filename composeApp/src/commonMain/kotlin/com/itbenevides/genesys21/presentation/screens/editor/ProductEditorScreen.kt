package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Product
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditorScreen(
    product: Product?,
    onSave: (Product) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "0") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (product == null) "Novo Produto" else "Editar Produto", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val finalProduct = Product(
                                id = product?.id ?: Random.nextInt().toString(),
                                name = name,
                                price = price.toDoubleOrNull() ?: 0.0,
                                imageUrl = imageUrl,
                                description = description,
                                category = category,
                                stock = stock.toIntOrNull() ?: 0
                            )
                            onSave(finalProduct)
                        },
                        enabled = name.isNotBlank() && price.toDoubleOrNull() != null
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Salvar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Informações Básicas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Produto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Preço") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Estoque") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Detalhes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL da Imagem") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição Completa") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
