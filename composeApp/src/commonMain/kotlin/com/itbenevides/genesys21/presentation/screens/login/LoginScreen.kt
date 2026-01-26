package com.itbenevides.genesys21.presentation.screens.login

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun LoginScreen(
    viewModel: PageViewModel,
    onLoginSuccess: () -> Unit
) {
    // 1. State Holder
    var state by remember { mutableStateOf(LoginState()) }

    // 2. Orquestrador de Eventos
    val onEvent: (LoginEvent) -> Unit = { event ->
        when (event) {
            is LoginEvent.OnEmailChanged -> {
                state = state.copy(
                    email = event.email,
                    canLogin = event.email.isNotBlank() && state.password.isNotBlank()
                )
            }
            is LoginEvent.OnPasswordChanged -> {
                state = state.copy(
                    password = event.password,
                    canLogin = state.email.isNotBlank() && event.password.isNotBlank()
                )
            }
            is LoginEvent.OnLoginClicked -> {
                state = state.copy(isLoading = true, errorMessage = "")
                viewModel.signIn(state.email, state.password,
                    onSuccess = { 
                        state = state.copy(isLoading = false)
                        onLoginSuccess() 
                    },
                    onFailure = { 
                        state = state.copy(isLoading = false, errorMessage = it) 
                    }
                )
            }
        }
    }

    // 3. UI Pura
    LoginContent(state, onEvent)
}

@Composable
private fun LoginContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit
) {
    GenesysPage {
        GenesysColumn(
            maxWidth = GenesysDimens.LoginMaxWidth,
            horizontalAlignment = GenesysAlignment.Center,
            // Centralização vertical simulada
        ) {
            GenesysSpacer(GenesysSpacing.Huge)
            
            GenesysText(
                text = GenesysStrings.Welcome,
                style = GenesysTextStyle.Headline,
                fontWeight = GenesysFontWeight.Bold
            )
             GenesysText(
                text = GenesysStrings.LoginSubtitle,
                style = GenesysTextStyle.Body
            )

            GenesysSpacer(GenesysSpacing.Huge)

            GenesysTextField(
                value = state.email, 
                onValueChange = { newValue -> onEvent(LoginEvent.OnEmailChanged(newValue)) }, 
                label = GenesysStrings.EmailLabel,
                icon = GenesysIcons.Email
            )
            
            GenesysSpacer(GenesysSpacing.Medium)
            
            GenesysTextField(
                value = state.password,
                onValueChange = { newValue -> onEvent(LoginEvent.OnPasswordChanged(newValue)) },
                label = GenesysStrings.PasswordLabel,
                icon = GenesysIcons.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            GenesysSpacer(GenesysSpacing.Large)

            GenesysLoadingButton(
                text = GenesysStrings.LoginButton,
                onClick = { onEvent(LoginEvent.OnLoginClicked) },
                fillWidth = true,
                isLoading = state.isLoading,
                enabled = state.canLogin
            )

            if (state.errorMessage.isNotEmpty()) {
                GenesysSpacer(GenesysSpacing.Medium)
                GenesysText(
                    text = state.errorMessage,
                    style = GenesysTextStyle.Error
                )
            }
        }
    }
}
