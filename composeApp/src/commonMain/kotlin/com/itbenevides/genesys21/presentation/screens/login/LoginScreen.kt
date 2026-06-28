package com.itbenevides.genesys21.presentation.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysAlignment
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextAlign
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.AnimatedGradientBackground
import com.itbenevides.genesys21.ui.util.glassmorphic

@Composable
fun LoginScreen(
    viewModel: PageViewModel,
    onLoginSuccess: () -> Unit,
) {
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
                    onFailure = {
                        state = state.copy(isLoading = false, errorMessage = it)
                    },
                )
            }
        }
    }

    LoginContent(state, onEvent)
}

@Composable
private fun LoginContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "iconScale",
    )

    GenesysPage(
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedGradientBackground()

                GenesysColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = GenesysAlignment.Center,
                    verticalArrangement = Arrangement.Center,
                    usePadding = true,
                ) {
                    Surface(
                        modifier =
                            Modifier
                                .widthIn(max = GenesysDimens.LoginMaxWidth)
                                .glassmorphic(RoundedCornerShape(32.dp)),
                        tonalElevation = 8.dp,
                        shape = RoundedCornerShape(32.dp),
                        color = Color.Transparent,
                    ) {
                        GenesysColumn(
                            usePadding = true,
                            horizontalAlignment = GenesysAlignment.Center,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(80.dp)
                                        .scale(iconScale),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = GenesysIcons.Magic,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            GenesysSpacer(GenesysSpacing.Medium)

                            GenesysText(
                                text = GenesysStrings.Welcome,
                                style = GenesysTextStyle.Headline,
                                fontWeight = GenesysFontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                            )

                            GenesysText(
                                text = GenesysStrings.LoginSubtitle,
                                style = GenesysTextStyle.Body,
                            )

                            GenesysSpacer(GenesysSpacing.ExtraLarge)

                            GenesysTextField(
                                value = state.email,
                                onValueChange = { newValue -> onEvent(LoginEvent.OnEmailChanged(newValue)) },
                                label = GenesysStrings.EmailLabel,
                                icon = GenesysIcons.Email,
                            )

                            GenesysSpacer(GenesysSpacing.Medium)

                            GenesysTextField(
                                value = state.password,
                                onValueChange = { newValue -> onEvent(LoginEvent.OnPasswordChanged(newValue)) },
                                label = GenesysStrings.PasswordLabel,
                                icon = GenesysIcons.Lock,
                                visualTransformation = PasswordVisualTransformation(),
                            )

                            GenesysSpacer(GenesysSpacing.Large)

                            GenesysLoadingButton(
                                text = GenesysStrings.LoginButton,
                                onClick = { onEvent(LoginEvent.OnLoginClicked) },
                                fillWidth = true,
                                isLoading = state.isLoading,
                                enabled = state.canLogin,
                                icon = GenesysIcons.Check,
                            )

                            if (state.errorMessage.isNotEmpty()) {
                                GenesysSpacer(GenesysSpacing.Medium)
                                GenesysText(
                                    text = state.errorMessage,
                                    style = GenesysTextStyle.Error,
                                    textAlign = GenesysTextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}
