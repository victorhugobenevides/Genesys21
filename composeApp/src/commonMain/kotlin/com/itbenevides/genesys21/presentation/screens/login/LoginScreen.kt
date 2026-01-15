package com.itbenevides.genesys21.presentation.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.presentation.PageViewModel

@Composable
fun LoginScreen(viewModel: PageViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)) {
            Text("Login", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Acesse sua conta para continuar", color = MaterialTheme.colorScheme.secondary)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = email, onValueChange = { email = it }, 
                placeholder = { Text("E-mail") }, modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it }, 
                placeholder = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    isLoading = true
                    viewModel.signIn(email, password, 
                        onSuccess = { onLoginSuccess(); isLoading = false },
                        onFailure = { errorMessage = it; isLoading = false }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White) 
                else Text("Entrar", fontWeight = FontWeight.SemiBold)
            }
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}
