package com.itbenevides.genesys21.presentation.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import org.koin.compose.viewmodel.koinViewModel

/**
 * Entry point da tela de Login. Gerencia a injeção do ViewModel.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    LoginContent(
        state = state,
        onEvent = { event -> viewModel.onEvent(event, onLoginSuccess) }
    )
}

/**
 * UI pura da tela de Login. 
 * Esta função é agnóstica a ViewModel e Koin, facilitando testes de UI isolados.
 */
@Composable
fun LoginContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit
) {
    GenesysPage(useMaxWidth = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints {
                val isWideScreen = maxWidth > 600.dp
                
                GenesysCard(
                    modifier = Modifier
                        .widthIn(max = 450.dp)
                        .padding(horizontal = 16.dp),
                    elevation = if (isWideScreen) 2.dp else 0.dp,
                    backgroundColor = if (isWideScreen) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent,
                    contentPadding = if (isWideScreen) 32.dp else 16.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = GenesysIcons.Magic,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        GenesysSpacer(GenesysSpacing.Large)
                        
                        GenesysText(
                            text = GenesysStrings.Welcome,
                            style = GenesysTextStyle.Headline,
                            fontWeight = GenesysFontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        GenesysText(
                            text = GenesysStrings.LoginSubtitle,
                            style = GenesysTextStyle.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        GenesysSpacer(GenesysSpacing.Huge)

                        GenesysTextField(
                            value = state.email, 
                            onValueChange = { newValue -> onEvent(LoginEvent.OnEmailChanged(newValue)) }, 
                            label = GenesysStrings.EmailLabel,
                            icon = GenesysIcons.Email,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysTextField(
                            value = state.password,
                            onValueChange = { newValue -> onEvent(LoginEvent.OnPasswordChanged(newValue)) },
                            label = GenesysStrings.PasswordLabel,
                            icon = GenesysIcons.Lock,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        GenesysSpacer(GenesysSpacing.ExtraLarge)

                        GenesysLoadingButton(
                            text = GenesysStrings.LoginButton,
                            onClick = { onEvent(LoginEvent.OnLoginClicked) },
                            fillWidth = true,
                            isLoading = state.isLoading,
                            enabled = state.canLogin,
                            icon = GenesysIcons.Check
                        )

                        if (state.errorMessage.isNotEmpty()) {
                            GenesysSpacer(GenesysSpacing.Medium)
                            GenesysText(
                                text = state.errorMessage,
                                style = GenesysTextStyle.Error,
                                textAlign = GenesysTextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
