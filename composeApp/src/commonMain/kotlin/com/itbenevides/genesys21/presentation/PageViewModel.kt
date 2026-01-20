package com.itbenevides.genesys21.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.PageRepository
import com.itbenevides.genesys21.domain.usecase.DeletePageUseCase
import com.itbenevides.genesys21.domain.usecase.GetPagesUseCase
import com.itbenevides.genesys21.domain.usecase.GetPublicPageUseCase
import com.itbenevides.genesys21.domain.usecase.SavePageUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PageViewModel(
    private val getPagesUseCase: GetPagesUseCase,
    private val savePageUseCase: SavePageUseCase,
    private val deletePageUseCase: DeletePageUseCase,
    private val getPublicPageUseCase: GetPublicPageUseCase,
    private val authRepository: AuthRepository,
    private val pageRepository: PageRepository
) : ViewModel() {

    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    val cartTotal = _cart.map { items -> 
        items.sumOf { it.product.price * it.quantity } 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartCount = _cart.map { items -> items.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addToCart(product: Product): Boolean {
        val current = _cart.value.toMutableList()
        val existing = current.find { it.product.id == product.id }
        
        val currentQuantity = existing?.quantity ?: 0
        if (currentQuantity + 1 > product.stock) {
            return false
        }

        if (existing != null) {
            val index = current.indexOf(existing)
            current[index] = existing.copy(quantity = currentQuantity + 1)
        } else {
            current.add(CartItem(product, 1))
        }
        _cart.value = current
        return true
    }

    fun removeFromCart(productId: String) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun updateCartQuantity(productId: String, quantity: Int): Boolean {
        if (quantity <= 0) {
            removeFromCart(productId)
            return true
        }
        
        val item = _cart.value.find { it.product.id == productId } ?: return false
        if (quantity > item.product.stock) {
            return false
        }

        _cart.value = _cart.value.map { 
            if (it.product.id == productId) it.copy(quantity = quantity) else it 
        }
        return true
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun generateWhatsappMessage(whatsapp: String?): String? {
        if (whatsapp.isNullOrBlank() || _cart.value.isEmpty()) return null
        
        val sb = StringBuilder()
        sb.append("Olá! Gostaria de fazer um pedido:\n\n")
        
        _cart.value.forEach { item ->
            val stockWarning = if (item.quantity > item.product.stock) " (Aviso: Qtd excede estoque disponível)" else ""
            sb.append("• ${item.quantity}x ${item.product.name} - R$ ${item.product.price * item.quantity}$stockWarning\n")
        }
        
        sb.append("\n*Total: R$ ${cartTotal.value}*")
        
        val encodedMessage = sb.toString()
            .replace(" ", "%20")
            .replace("\n", "%0A")
            .replace("*", "%2A")
            
        return "https://wa.me/$whatsapp?text=$encodedMessage"
    }

    val allAvailableProducts: StateFlow<List<Product>> = _pages.map { allPages ->
        allPages.flatMap { page -> 
            page.components.filterIsInstance<PageComponent.ProductList>().flatMap { it.products }
        }.distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAvailableCategories: StateFlow<List<String>> = allAvailableProducts.map { products ->
        products.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPages() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = authRepository.getCurrentUserToken() ?: ""
            _pages.value = getPagesUseCase(token)
            _isLoading.value = false
        }
    }

    suspend fun getPagesSync(): List<Page> {
        val token = authRepository.getCurrentUserToken() ?: return emptyList()
        return getPagesUseCase(token)
    }
    
    suspend fun loadPublicPage(id: String): Page? {
        _isLoading.value = true
        val result = getPublicPageUseCase(id)
        _isLoading.value = false
        return result.getOrNull()
    }

    // NOVA FUNÇÃO: Busca a primeira página do sistema (pública)
    suspend fun loadFirstPublicPage(): Page? {
        _isLoading.value = true
        val result = pageRepository.getPages("").firstOrNull()
        _isLoading.value = false
        return result
    }

    suspend fun loadPageByDomain(domain: String): Page? {
        _isLoading.value = true
        val result = pageRepository.getPageByDomain(domain)
        _isLoading.value = false
        return result.getOrNull()
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

    fun uploadImage(bytes: ByteArray, fileName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = authRepository.getCurrentUserToken() ?: ""
            pageRepository.uploadImage(bytes, fileName, token).onSuccess { url ->
                onSuccess(url)
            }.onFailure {
                it.printStackTrace()
            }
            _isLoading.value = false
        }
    }
}
