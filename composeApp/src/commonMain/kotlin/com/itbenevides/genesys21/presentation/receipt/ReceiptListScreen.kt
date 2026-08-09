package com.itbenevides.genesys21.presentation.receipt

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.util.NfeUrlBuilder
import com.itbenevides.genesys21.util.rememberFilePicker
import com.itbenevides.genesys21.util.SelectedFile
import com.itbenevides.genesys21.util.toBase64
import org.jetbrains.compose.resources.decodeToImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptListScreen(
    viewModel: ReceiptViewModel,
    onOpenUrl: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val categories = listOf("Todas", "Eletrônicos", "Supermercado", "Farmácia", "Combustível", "Alimentação", "Geral")

    val filteredReceipts = remember(state.receipts, state.searchQuery, state.selectedCategory) {
        state.receipts.filter { receipt ->
            val matchesQuery = state.searchQuery.isBlank() ||
                receipt.emitente.contains(state.searchQuery, ignoreCase = true) ||
                (receipt.cnpjEmitente?.contains(state.searchQuery) == true) ||
                (receipt.chaveAcesso?.contains(state.searchQuery) == true)
            val matchesCategory = state.selectedCategory == "Todas" || receipt.categoria == state.selectedCategory
            matchesQuery && matchesCategory
        }
    }

    val totalSpent = remember(filteredReceipts) {
        filteredReceipts.sumOf { it.valorTotal }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🧾 Notas Fiscais",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Organizador do Pai • Consulta SEFAZ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openBackupDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = "Backup JSON",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openScanDialog(true) },
                icon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) },
                text = { Text("Escanear Nota", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Card de Resumo Financeiro
            SummaryHeaderCard(
                totalSpent = totalSpent,
                receiptCount = filteredReceipts.size
            )

            // Campo de Busca
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por loja, CNPJ ou chave...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (state.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar")
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Chips de Categoria
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Notas Fiscais
            if (filteredReceipts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma nota fiscal encontrada",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.openScanDialog(true) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Escanear Primeira Nota")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReceipts, key = { it.id }) { receipt ->
                        ReceiptCardItem(
                            receipt = receipt,
                            onClick = { viewModel.selectReceipt(receipt) },
                            onOpenUrl = onOpenUrl,
                            onDelete = { viewModel.deleteReceipt(receipt.id) }
                        )
                    }
                }
            }
        }
    }

    // Modal de Scanner / Leitura de Foto
    if (state.showScanDialog) {
        ScanReceiptDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.openScanDialog(false) }
        )
    }

    // Modal de Detalhes da Nota
    state.selectedReceipt?.let { receipt ->
        ReceiptDetailDialog(
            receipt = receipt,
            onDismiss = { viewModel.selectReceipt(null) },
            onOpenUrl = onOpenUrl
        )
    }

    // Modal de Backup JSON
    if (state.showBackupDialog) {
        BackupJsonDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.openBackupDialog(false) }
        )
    }
}

@Composable
fun SummaryHeaderCard(totalSpent: Double, receiptCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1E3C72),
                            Color(0xFF2A5298)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "RESUMO DE NOTAS SALVAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "R$ " + formatMoney(totalSpent),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "$receiptCount nota(s) organizada(s)",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% Local",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptCardItem(
    receipt: Receipt,
    onClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = receipt.emitente,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Data: ${receipt.dataEmissao} • ${receipt.categoria}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "R$ " + formatMoney(receipt.valorTotal),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Informação da Chave de Acesso
            val chaveAcesso = receipt.chaveAcesso
            if (!chaveAcesso.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chave: " + NfeUrlBuilder.formatChaveAcesso(chaveAcesso),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        receipt.onlineUrl?.let { url ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onOpenUrl(url) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver SEFAZ", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, org.jetbrains.compose.resources.ExperimentalResourceApi::class)
@Composable
fun ScanReceiptDialog(
    viewModel: ReceiptViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var rawInputText by remember { mutableStateOf("") }

    val filePicker = rememberFilePicker { result ->
        viewModel.onImageSelected(result?.bytes, result?.mimeType)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🧾 Escanear Nota (Foto ou PDF)") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Selecione uma foto da galeria, use a câmera ou envie um arquivo PDF:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Área de Preview / Seleção
                if (state.selectedImageBytes != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.selectedMimeType == "application/pdf") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Description, "PDF", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("Documento PDF Selecionado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            state.selectedImageBytes?.let { bytes ->
                                val bitmap = remember(bytes) { bytes.decodeToImageBitmap() }
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap,
                                    contentDescription = "Preview da Nota",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.onImageSelected(null) },
                            modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { filePicker("image/*,application/pdf", false) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Arquivo/Galeria", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { filePicker("image/*", true) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Usar Câmera", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Chave Gemini (Opcional)
                OutlinedTextField(
                    value = state.geminiApiKey,
                    onValueChange = { viewModel.onGeminiApiKeyChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chave Gemini Personalizada (Opcional)") },
                    placeholder = { Text("O servidor já possui uma chave configurada") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Fallback: Texto Manual
                Text("Ou cole a URL do QR Code ou texto:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = { Text("Cole aqui o texto da nota ou link do QR Code...") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            val hasFile = state.selectedImageBytes != null
            val hasText = rawInputText.isNotBlank()

            Button(
                onClick = {
                    val base64 = state.selectedImageBytes?.toBase64()
                    viewModel.processReceiptText(rawInputText, base64, state.geminiApiKey)
                },
                enabled = (hasFile || hasText) && !state.isScanning,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    val actionText = if (hasFile) "Analisar ${if (state.selectedMimeType == "application/pdf") "PDF" else "Foto"}" else "Analisar Texto"
                    Text(actionText)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ReceiptDetailDialog(
    receipt: Receipt,
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(receipt.emitente, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("CNPJ: ${receipt.cnpjEmitente ?: "Não informado"}", fontSize = 13.sp)
                Text("Data de Emissão: ${receipt.dataEmissao}", fontSize = 13.sp)
                Text("Categoria: ${receipt.categoria}", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text("Valor Total: R$ ${formatMoney(receipt.valorTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                if (receipt.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Itens / Produtos:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    receipt.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${item.descricao}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("R$ ${formatMoney(item.valorTotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                receipt.chaveAcesso?.let { chave ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Chave de Acesso (44 Dígitos):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = NfeUrlBuilder.formatChaveAcesso(chave),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            receipt.onlineUrl?.let { url ->
                Button(
                    onClick = {
                        onOpenUrl(url)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir na SEFAZ")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun BackupJsonDialog(
    viewModel: ReceiptViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var jsonText by remember { mutableStateOf(viewModel.exportBackupJson()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("💾 Backup de Notas Fiscais (JSON)") },
        text = {
            Column {
                Text(
                    text = "Você pode copiar seu backup ou colar um JSON para restaurar suas notas:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    label = { Text("Conteúdo do Backup JSON") },
                    shape = RoundedCornerShape(12.dp)
                )
                state.backupMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.importBackupJson(jsonText) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Importar JSON")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

private fun formatMoney(amount: Double): String {
    val whole = amount.toLong()
    val cents = ((amount - whole) * 100).toLong()
    val centsStr = if (cents < 10) "0$cents" else "$cents"
    return "$whole,$centsStr"
}
