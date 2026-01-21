package com.itbenevides.genesys21.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.util.AnalyticsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppError(
    val title: String,
    val message: String,
    val stackTrace: String? = null
)

class PageViewModel(
    private val getPagesUseCase: GetPagesUseCase,
    private val savePageUseCase: SavePageUseCase,
    private val deletePageUseCase: DeletePageUseCase,
    private val getPublicPageUseCase: GetPublicPageUseCase,
    private val getPageByDomainUseCase: GetPageByDomainUseCase,
    private val getFirstPublicPageUseCase: GetFirstPublicPageUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // --- ESTADO DE ERRO GLOBAL ---
    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError = _currentError.asStateFlow()

    fun clearError() { _currentError.value = null }

    private fun handleError(title: String, e: Throwable) {
        val stackTrace = e.stackTraceToString()
        _currentError.value = AppError(
            title = title,
            message = e.message ?: "Erro desconhecido",
            stackTrace = if (stackTrace.length > 500) stackTrace.take(500) + "..." else stackTrace
        )
        // GA4: Rastrear erro
        AnalyticsManager.logEvent("app_error", mapOf("title" to title, "exception" to (e::class.simpleName ?: "unknown")))
    }

    // Derived states for products and categories across all user pages
    val allAvailableProducts: StateFlow<List<Product>> = _pages.map { pages ->
        pages.flatMap { page ->
            page.components.filterIsInstance<PageComponent.ProductList>().flatMap { it.products }
        }.distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAvailableCategories: StateFlow<List<String>> = allAvailableProducts.map { products ->
        products.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- LÓGICA DO CARRINHO ---
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
        if (currentQuantity + 1 > product.stock) return false

        if (existing != null) {
            val index = current.indexOf(existing)
            current[index] = existing.copy(quantity = currentQuantity + 1)
        } else {
            current.add(CartItem(product, 1))
        }
        _cart.value = current
        AnalyticsManager.logEvent("add_to_cart", mapOf("item_id" to product.id, "item_name" to product.name))
        return true
    }

    fun removeFromCart(productId: String) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun updateCartQuantity(productId: String, quantity: Int): Boolean {
        if (quantity <= 0) { removeFromCart(productId); return true }
        val item = _cart.value.find { it.product.id == productId } ?: return false
        if (quantity > item.product.stock) return false
        _cart.value = _cart.value.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
        return true
    }

    fun generateWhatsappMessage(whatsapp: String?): String? {
        if (whatsapp.isNullOrBlank() || _cart.value.isEmpty()) return null
        AnalyticsManager.logEvent("begin_checkout", mapOf("value" to cartTotal.value))
        val sb = StringBuilder().append("Olá! Gostaria de fazer um pedido:\n\n")
        _cart.value.forEach { item -> sb.append("• ${item.quantity}x ${item.product.name} - R$ ${item.product.price * item.quantity}\n") }
        sb.append("\n*Total: R$ ${cartTotal.value}*")
        return "https://wa.me/$whatsapp?text=${sb.toString().replace(" ", "%20").replace("\n", "%0A")}"
    }

    // --- OPERAÇÕES DE PÁGINAS COM TRATAMENTO DE ERRO ---
    fun loadPages() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = authRepository.getCurrentUserToken() ?: ""
                _pages.value = getPagesUseCase(token)
            } catch (e: Exception) {
                handleError("Falha ao carregar páginas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun loadPublicPage(id: String): Page? {
        return getPublicPageUseCase(id).fold(
            onSuccess = { it },
            onFailure = { 
                handleError("Erro na página pública", it)
                null 
            }
        )
    }

    suspend fun loadPageByDomain(domain: String): Page? {
        return getPageByDomainUseCase(domain).fold(
            onSuccess = { it },
            onFailure = { 
                // Silencioso para domínio não encontrado (fluxo normal)
                null 
            }
        )
    }

    suspend fun loadFirstPublicPage(): Page? = getFirstPublicPageUseCase()

    fun savePage(page: Page, isEditing: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = authRepository.getCurrentUserToken() ?: ""
                savePageUseCase(page, token, isEditing).fold(
                    onSuccess = {
                        loadPages()
                        onComplete()
                    },
                    onFailure = { throw it }
                )
            } catch (e: Exception) {
                handleError("Falha ao salvar página", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePage(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = authRepository.getCurrentUserToken() ?: ""
                deletePageUseCase(id, token).fold(
                    onSuccess = {
                        loadPages()
                        onComplete()
                    },
                    onFailure = { throw it }
                )
            } catch (e: Exception) {
                handleError("Falha ao excluir página", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadImage(bytes: ByteArray, fileName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = authRepository.getCurrentUserToken() ?: ""
                uploadImageUseCase(bytes, fileName, token).fold(
                    onSuccess = { onSuccess(it) },
                    onFailure = { throw it }
                )
            } catch (e: Exception) {
                handleError("Erro no upload de imagem", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- AUTENTICAÇÃO ---
    suspend fun getCurrentUserToken(): String? = authRepository.getCurrentUserToken()
    suspend fun getPagesSync(): List<Page> = getPagesUseCase(authRepository.getCurrentUserToken() ?: "")

    fun signIn(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.signIn(email, pass).fold(
                onSuccess = { 
                    AnalyticsManager.logEvent("login")
                    onSuccess() 
                },
                onFailure = { 
                    handleError("Erro de Login", it)
                    onFailure(it.message ?: "Erro") 
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch { 
            AnalyticsManager.logEvent("logout")
            authRepository.signOut() 
        }
    }
}
