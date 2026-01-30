package com.itbenevides.genesys21.presentation.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysAlignment
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextAlign
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun LoginScreen(
    viewModel: PageViewModel,
    onLoginSuccess: () -> Unit
) {
    var state by remember { mutableStateOf(LoginState()) }

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

    LoginContent(state, onEvent)
}

@Composable
private fun LoginContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit
) {
     GenesysPage {
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            verticalArrangement = Arrangement.Center,
            usePadding = true
        ) {
            GenesysColumn(
                maxWidth = GenesysDimens.LoginMaxWidth,
                horizontalAlignment = GenesysAlignment.Center,
                usePadding = false
            ) {
                GenesysIconButton(
                    icon = GenesysIcons.Magic,
                    modifier = Modifier.size(64.dp),
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    onClick = {}
                )
                
                GenesysSpacer(GenesysSpacing.Medium)
                
                GenesysText(
                    text = GenesysStrings.Welcome,
                    style = GenesysTextStyle.Headline,
                    fontWeight = GenesysFontWeight.ExtraBold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
                
                GenesysText(
                    text = GenesysStrings.LoginSubtitle,
                    style = GenesysTextStyle.Body
                )

                GenesysSpacer(GenesysSpacing.ExtraLarge)

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
                    enabled = state.canLogin,
                    icon = GenesysIcons.Check
                )

                if (state.errorMessage.isNotEmpty()) {
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysText(
                        text = state.errorMessage,
                        style = GenesysTextStyle.Error,
                        textAlign = GenesysTextAlign.Center
                    )
                }
            }
        }
    }
}
