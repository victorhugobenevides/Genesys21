package com.itbenevides.genesys21.presentation.screens.login

/**
 * UI State para a tela de Login.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val canLogin: Boolean = false,
)

/**
 * UI Intents (Eventos) para a tela de Login.
 */
sealed class LoginEvent {
    data class OnEmailChanged(val email: String) : LoginEvent()

    data class OnPasswordChanged(val password: String) : LoginEvent()

    object OnLoginClicked : LoginEvent()
}
