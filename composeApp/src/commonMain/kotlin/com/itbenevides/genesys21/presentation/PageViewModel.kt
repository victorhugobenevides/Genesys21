package com.itbenevides.genesys21.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.OrderRepository
import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.util.AnalyticsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.random.Random

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
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError = _currentError.asStateFlow()

    init {
        viewModelScope.launch {
            cartRepository.loadInitialCart()
        }
    }

    fun clearError() { _currentError.value = null }

    private fun handleError(title: String, e: Throwable) {
        val stackTrace = e.stackTraceToString()
        _currentError.value = AppError(
            title = title,
            message = e.message ?: "Erro desconhecido",
            stackTrace = if (stackTrace.length > 500) stackTrace.take(500) + "..." else stackTrace
        )
        AnalyticsManager.logEvent("app_error", mapOf("title" to title, "exception" to (e::class.simpleName ?: "unknown")))
    }

    // --- CARRINHO ---
    val cart = cartRepository.cartItems

    val cartTotal = cart.map { items -> 
        items.sumOf { it.product.price * it.quantity } 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartCount = cart.map { items -> items.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addToCart(product: Product): Boolean {
        if (product.stock <= 0) return false
        viewModelScope.launch {
            cartRepository.addToCart(CartItem(product, 1))
            AnalyticsManager.logEvent("add_to_cart", mapOf("item_id" to product.id, "item_name" to product.name))
        }
        return true
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch { cartRepository.removeFromCart(productId) }
    }

    fun updateCartQuantity(productId: String, quantity: Int): Boolean {
        viewModelScope.launch { cartRepository.updateQuantity(productId, quantity) }
        return true
    }

    // --- PEDIDOS ---
    fun loadOrders() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            println("ORDERS: Carregando pedidos para o token logado...")
            orderRepository.getOrders(token).collect {
                println("ORDERS: ${it.size} pedidos recebidos do servidor.")
                _orders.value = it
            }
        }
    }

    fun submitOrder(page: Page?) {
        val ownerId = page?.ownerId
        val whatsapp = page?.whatsapp
        
        if (ownerId == null || cart.value.isEmpty()) {
            println("ORDERS: Abortando salvar pedido. OwnerId=$ownerId, Itens=${cart.value.size}")
            return
        }
        
        viewModelScope.launch {
            val orderId = "ORD-" + Random.nextInt(100000, 999999).toString()
            val newOrder = Order(
                id = orderId,
                userId = ownerId, // CRÍTICO: Vincula o pedido ao dono da página
                items = cart.value,
                total = cartTotal.value,
                status = OrderStatus.PENDING,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                whatsappContact = whatsapp
            )
            
            println("ORDERS: Salvando pedido $orderId para o dono $ownerId")
            orderRepository.createOrder(newOrder).onSuccess {
                println("ORDERS: Pedido salvo com sucesso no servidor!")
                cartRepository.clearCart()
                AnalyticsManager.logEvent("purchase", mapOf("transaction_id" to orderId, "value" to cartTotal.value))
            }.onFailure {
                println("ORDERS: Erro ao salvar pedido - ${it.message}")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            orderRepository.updateOrderStatus(token, orderId, status).onSuccess {
                loadOrders()
            }
        }
    }

    fun generateWhatsappMessage(whatsapp: String?): String? {
        if (whatsapp.isNullOrBlank() || cart.value.isEmpty()) return null
        AnalyticsManager.logEvent("begin_checkout", mapOf("value" to cartTotal.value))
        val sb = StringBuilder().append("Olá! Gostaria de fazer um pedido:\n\n")
        cart.value.forEach { item -> sb.append("• ${item.quantity}x ${item.product.name} - R$ ${item.product.price * item.quantity}\n") }
        sb.append("\n*Total: R$ ${cartTotal.value}*")
        return "https://wa.me/$whatsapp?text=${sb.toString().replace(" ", "%20").replace("\n", "%0A")}"
    }

    // --- PÁGINAS ---
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
            onFailure = { null }
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
            AnalyticsManager.trackPageView("logout")
            authRepository.signOut() 
        }
    }

    // Derived states
    val allAvailableProducts: StateFlow<List<Product>> = _pages.map { pages ->
        pages.flatMap { page ->
            page.components.filterIsInstance<PageComponent.ProductList>().flatMap { it.products }
        }.distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAvailableCategories: StateFlow<List<String>> = allAvailableProducts.map { products ->
        products.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
