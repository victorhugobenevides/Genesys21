package com.itbenevides.genesys21

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val authRepository = remember { getAuthRepository() }
        val scope = rememberCoroutineScope()
        
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Genesys21 Login", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        isError = false 
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError && email.isEmpty(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        isError = false
                    },
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError && password.length < 6,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            statusMessage = "Por favor, preencha todos os campos."
                            isError = true
                            return@Button
                        }
                        
                        if (password.length < 6) {
                            statusMessage = "A senha deve ter pelo menos 6 caracteres."
                            isError = true
                            return@Button
                        }

                        scope.launch {
                            isLoading = true
                            isError = false
                            statusMessage = "Autenticando..."
                            
                            val result = authRepository.signIn(email, password)
                            
                            result.fold(
                                onSuccess = { token ->
                                    if (token != null) {
                                        statusMessage = "Firebase OK! Validando no Servidor..."
                                        validateTokenOnServer(token) { serverResult ->
                                            statusMessage = serverResult
                                            isLoading = false
                                        }
                                    } else {
                                        statusMessage = "Erro inesperado: Token não recebido."
                                        isError = true
                                        isLoading = false
                                    }
                                },
                                onFailure = {
                                    // Tradução simples de erros comuns do Firebase
                                    val errorMsg = it.message ?: ""
                                    statusMessage = when {
                                        errorMsg.contains("user-not-found") -> "Usuário não encontrado."
                                        errorMsg.contains("wrong-password") -> "Senha incorreta."
                                        errorMsg.contains("invalid-email") -> "E-mail inválido."
                                        errorMsg.contains("network-request-failed") -> "Erro de conexão."
                                        else -> "Falha no login: ${it.message ?: "Erro desconhecido"}"
                                    }
                                    isError = true
                                    isLoading = false
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Entrar")
                    }
                }

                if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) MaterialTheme.colorScheme.error else Color.Unspecified
                    )
                }
            }
        }
    }
}

private fun validateTokenOnServer(token: String, onResult: (String) -> Unit) {
    // Usamos o token apenas como placeholder por enquanto
    println("Token: $token")
    onResult("Login realizado com sucesso! ✅")
}
