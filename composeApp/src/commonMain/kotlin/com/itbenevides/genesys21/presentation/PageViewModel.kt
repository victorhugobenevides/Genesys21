package com.itbenevides.genesys21.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.usecase.DeletePageUseCase
import com.itbenevides.genesys21.domain.usecase.GetPagesUseCase
import com.itbenevides.genesys21.domain.usecase.SavePageUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PageViewModel(
    private val getPagesUseCase: GetPagesUseCase,
    private val savePageUseCase: SavePageUseCase,
    private val deletePageUseCase: DeletePageUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Inventário Global de Produtos extraído de todas as páginas
    val allAvailableProducts: StateFlow<List<Product>> = _pages.map { allPages ->
        allPages.flatMap { page -> 
            page.components.filterIsInstance<PageComponent.ProductList>().flatMap { it.products }
        }.distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPages() {
        viewModelScope.launch {
            _isLoading.value = true
            _pages.value = getPagesUseCase()
            _isLoading.value = false
        }
    }

    suspend fun getCurrentUserToken(): String? = authRepository.getCurrentUserToken()

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun savePage(page: Page, isEditing: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = authRepository.getCurrentUserToken() ?: ""
            savePageUseCase(page, token, isEditing).onSuccess {
                loadPages()
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun deletePage(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = authRepository.getCurrentUserToken() ?: ""
            deletePageUseCase(id, token).onSuccess {
                loadPages()
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun signIn(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.signIn(email, pass)
                .onSuccess { onSuccess() }
                .onFailure { onFailure(it.message ?: "Erro desconhecido") }
        }
    }
}
