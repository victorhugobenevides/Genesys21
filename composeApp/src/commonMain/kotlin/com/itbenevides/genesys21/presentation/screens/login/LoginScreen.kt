package com.itbenevides.genesys21.presentation.screens.login

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import org.koin.compose.viewmodel.koinViewModel

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

@Composable
fun LoginContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current

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
                        .padding(horizontal = 16.dp)
                        .testTag("login_card"),
                    elevation = if (isWideScreen) 4.dp else 0.dp,
                    backgroundColor = if (isWideScreen) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent,
                    contentPadding = if (isWideScreen) 32.dp else 16.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = GenesysIcons.Magic,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        GenesysSpacer(GenesysSpacing.Large)
                        
                        GenesysText(
                            text = "Genesys21",
                            style = GenesysTextStyle.Headline,
                            fontWeight = GenesysFontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        GenesysText(
                            text = "Gerencie sua vitrine inteligente",
                            style = GenesysTextStyle.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = GenesysTextAlign.Center // Corrigido para GenesysTextAlign
                        )

                        GenesysSpacer(GenesysSpacing.Huge)

                        GenesysTextField(
                            value = state.email, 
                            onValueChange = { onEvent(LoginEvent.OnEmailChanged(it)) }, 
                            label = GenesysStrings.EmailLabel,
                            icon = GenesysIcons.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("email_field")
                        )
                        
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysTextField(
                            value = state.password,
                            onValueChange = { onEvent(LoginEvent.OnPasswordChanged(it)) },
                            label = GenesysStrings.PasswordLabel,
                            icon = GenesysIcons.Lock,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("password_field")
                        )

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            GenesysTextButton(
                                text = "Esqueceu a senha?",
                                onClick = { /* TODO */ },
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        GenesysSpacer(GenesysSpacing.Large)

                        GenesysLoadingButton(
                            text = GenesysStrings.LoginButton,
                            onClick = { 
                                focusManager.clearFocus()
                                onEvent(LoginEvent.OnLoginClicked) 
                            },
                            fillWidth = true,
                            isLoading = state.isLoading,
                            enabled = state.canLogin,
                            icon = GenesysIcons.Check,
                            modifier = Modifier.testTag("btn_login")
                        )

                        AnimatedVisibility(
                            visible = state.errorMessage.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                GenesysSpacer(GenesysSpacing.Medium)
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = state.errorMessage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
