package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.service.PageAIGeneratorService
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPageBuilderDialog(
    onDismiss: () -> Unit,
    onPageGenerated: (Page) -> Unit
) {
    val aiService: PageAIGeneratorService = koinInject()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedPage by remember { mutableStateOf<Page?>(null) }

    val listState = rememberLazyListState()
    val messages = remember {
        mutableStateListOf(
            ChatMessage("Olá! Eu sou o Mestre de Design Genesys. ✨\n\nDescreva que tipo de página você precisa (ex: 'Uma loja de bolos caseiros' ou 'Meu portfólio de engenheiro') e eu montarei tudo para você!", false)
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(GenesysIcons.Magic, "IA", tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Construtor IA Genesys", style = MaterialTheme.typography.titleMedium)
                                    Text("Design & Estrutura Autônoma", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Fechar") }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(messages) { msg ->
                            ChatBubble(msg)
                        }
                        if (isGenerating) {
                            item { GeneratingIndicator() }
                        }
                    }

                    // Ação Final: Confirmar Criação
                    AnimatedVisibility(
                        visible = generatedPage != null,
                        enter = expandVertically() + fadeIn()
                    ) {
                        Button(
                            onClick = { generatedPage?.let { onPageGenerated(it) } },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Usar este Design no Painel", fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 2.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Descreva sua ideia...") },
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank() && !isGenerating) {
                                        val prompt = inputText
                                        messages.add(ChatMessage(prompt, true))
                                        inputText = ""
                                        isGenerating = true
                                        scope.launch {
                                            try {
                                                val page = aiService.generatePage(prompt)
                                                generatedPage = page
                                                messages.add(ChatMessage("Pronto! Analisei sua ideia e criei uma estrutura com o tema **${page.theme}**. \n\nAdicionei componentes como ${page.components.size} seções de impacto. \n\nO que achou?", false))
                                            } catch (e: Exception) {
                                                messages.add(ChatMessage("Ops, tive um problema ao gerar o design: ${e.message}", false))
                                            } finally {
                                                isGenerating = false
                                            }
                                        }
                                    }
                                },
                                enabled = inputText.isNotBlank() && !isGenerating,
                                modifier = Modifier.background(
                                    if (inputText.isNotBlank()) MaterialTheme.colorScheme.tertiary else Color.LightGray,
                                    CircleShape
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GeneratingIndicator() {
    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.tertiary)
        Spacer(Modifier.width(12.dp))
        Text("Mestre de Design está trabalhando...", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
    }
}

private data class ChatMessage(val text: String, val isUser: Boolean)
