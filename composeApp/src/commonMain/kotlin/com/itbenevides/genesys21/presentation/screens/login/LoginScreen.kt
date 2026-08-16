package com.itbenevides.genesys21.presentation.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.components.auth.GoogleSignInButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysAlignment
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.AnimatedGradientBackground
import com.itbenevides.genesys21.ui.util.LocalTestMode
import com.itbenevides.genesys21.ui.util.glassmorphic

@Composable
fun LoginScreen(
    viewModel: PageViewModel,
    onLoginSuccess: () -> Unit,
) {
    val globalAppTheme by viewModel.appTheme.collectAsState()
    var state by remember { mutableStateOf(LoginState()) }

    val onEvent: (LoginEvent) -> Unit = { event ->
        when (event) {
            is LoginEvent.OnEmailChanged -> {
                state =
                    state.copy(
                        email = event.email,
                        canLogin = event.email.isNotBlank() && state.password.isNotBlank(),
                    )
            }
            is LoginEvent.OnPasswordChanged -> {
                state =
                    state.copy(
                        password = event.password,
                        canLogin = state.email.isNotBlank() && event.password.isNotBlank(),
                    )
            }
            is LoginEvent.OnLoginClicked -> {
                state = state.copy(isLoading = true, errorMessage = "")
                viewModel.signIn(
                    state.email,
                    state.password,
                    onSuccess = {
                        state = state.copy(isLoading = false)
                        onLoginSuccess()
                    },
                    onError = {
                        state = state.copy(isLoading = false, errorMessage = it)
                    },
                )
            }
            is LoginEvent.OnSignUpClicked -> {
                state = state.copy(isLoading = true, errorMessage = "")
                viewModel.signUp(
                    state.email,
                    state.password,
                    onSuccess = {
                        state = state.copy(isLoading = false)
                        onLoginSuccess()
                    },
                    onError = {
                        state = state.copy(isLoading = false, errorMessage = it)
                    },
                )
            }
            is LoginEvent.ToggleMode -> {
                state = state.copy(isSignUp = !state.isSignUp, errorMessage = "")
            }
            is LoginEvent.OnError -> {
                state = state.copy(isLoading = false, errorMessage = event.message)
            }
        }
    }

    AppTheme(themeConfig = globalAppTheme, customTheme = null) {
        LoginContent(state, onEvent, viewModel, onLoginSuccess)
    }
}

@Composable
private fun LoginContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    viewModel: PageViewModel,
    onLoginSuccess: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "login")
    val floatAnim by if (!LocalTestMode.current) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "float",
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    GenesysPage(usePadding = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedGradientBackground()

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Logo animado
                Box(
                    modifier =
                        Modifier
                            .size(100.dp)
                            .scale(floatAnim)
                            .background(GenesysTheme.colors.brandContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = GenesysIcons.Magic,
                        contentDescription = "Logo",
                        modifier = Modifier.size(50.dp),
                        tint = GenesysTheme.colors.brand,
                    )
                }

                Spacer(modifier = Modifier.height(GenesysTheme.spacing.l))

                GenesysText(
                    text = GenesysStrings.AppName,
                    style = GenesysTextStyle.Headline,
                    fontWeight = GenesysFontWeight.ExtraBold,
                )
                GenesysText(
                    text = if (state.isSignUp) "Crie sua conta Genesys21" else GenesysStrings.LoginSubtitle,
                    style = GenesysTextStyle.Body,
                    color = GenesysTheme.colors.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(GenesysTheme.spacing.xl))

                // Card de formulário com Glassmorphism
                Box(
                    modifier =
                        Modifier
                            .widthIn(max = GenesysDimens.LoginMaxWidth)
                            .glassmorphic(RoundedCornerShape(24.dp), alpha = 0.4f)
                            .padding(24.dp),
                ) {
                    Column {
                        GenesysTextField(
                            value = state.email,
                            onValueChange = { onEvent(LoginEvent.OnEmailChanged(it)) },
                            label = GenesysStrings.EmailLabel,
                            icon = GenesysIcons.Person,
                            placeholder = "seu@email.com",
                        )

                        Spacer(modifier = Modifier.height(GenesysTheme.spacing.m))

                        GenesysTextField(
                            value = state.password,
                            onValueChange = { onEvent(LoginEvent.OnPasswordChanged(it)) },
                            label = GenesysStrings.PasswordLabel,
                            icon = GenesysIcons.Lock,
                            placeholder = "******",
                            visualTransformation = PasswordVisualTransformation(),
                        )

                        if (state.errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(GenesysTheme.spacing.xs))
                            GenesysText(text = state.errorMessage, style = GenesysTextStyle.Error)
                        }

                        Spacer(modifier = Modifier.height(GenesysTheme.spacing.l))

                        GenesysLoadingButton(
                            text = if (state.isSignUp) "Cadastrar Agora" else GenesysStrings.LoginButton,
                            onClick = {
                                if (state.isSignUp) onEvent(LoginEvent.OnSignUpClicked)
                                else onEvent(LoginEvent.OnLoginClicked)
                            },
                            isLoading = state.isLoading,
                            enabled = state.canLogin,
                            fillWidth = true,
                        )

                        Spacer(modifier = Modifier.height(GenesysTheme.spacing.m))

                        GoogleSignInButton(
                            onTokenReceived = { idToken, accessToken ->
                                viewModel.signInWithToken(
                                    idToken = idToken,
                                    accessToken = accessToken,
                                    provider = "google",
                                    onSuccess = onLoginSuccess,
                                    onError = { error ->
                                        onEvent(LoginEvent.OnError(error))
                                    }
                                )
                            },
                            onError = { error ->
                                onEvent(LoginEvent.OnError(error))
                            }
                        )

                        Spacer(modifier = Modifier.height(GenesysTheme.spacing.l))

                        TextButton(
                            onClick = { onEvent(LoginEvent.ToggleMode) },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = if (state.isSignUp) "Já tem conta? Faça Login" else "Não tem conta? Cadastre-se",
                                color = GenesysTheme.colors.brand,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
