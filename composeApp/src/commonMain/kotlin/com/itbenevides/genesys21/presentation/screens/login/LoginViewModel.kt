package com.itbenevides.genesys21.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.presentation.PageViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val pageViewModel: PageViewModel // Ainda dependemos para loadPages, mas isolamos a UI
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent, onSuccess: () -> Unit) {
        when (event) {
            is LoginEvent.OnEmailChanged -> {
                _uiState.update { it.copy(
                    email = event.email,
                    canLogin = event.email.isNotBlank() && it.password.isNotBlank()
                ) }
            }
            is LoginEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(
                    password = event.password,
                    canLogin = it.email.isNotBlank() && event.password.isNotBlank()
                ) }
            }
            is LoginEvent.OnLoginClicked -> performLogin(onSuccess)
        }
    }

    private fun performLogin(onSuccess: () -> Unit) {
        val email = _uiState.value.email
        val password = _uiState.value.password
        
        _uiState.update { it.copy(isLoading = true, errorMessage = "") }
        
        viewModelScope.launch {
            authRepository.signIn(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    pageViewModel.loadPages() // Mantém consistência com o estado global
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(
                        isLoading = false, 
                        errorMessage = error.message ?: "Erro ao realizar login"
                    ) }
                }
        }
    }
}
