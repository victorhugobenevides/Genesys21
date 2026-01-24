package com.itbenevides.genesys21.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
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
    private val getOrdersUseCase: GetOrdersUseCase,
    private val getCustomerOrdersUseCase: GetCustomerOrdersUseCase,
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val submitOrderUseCase: SubmitOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _customerOrders = MutableStateFlow<List<Order>>(emptyList())
    val customerOrders: StateFlow<List<Order>> = _customerOrders.asStateFlow()

    private val _trackedOrder = MutableStateFlow<Order?>(null)
    val trackedOrder: StateFlow<Order?> = _trackedOrder.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError = _currentError.asStateFlow()

    val customerName = customerRepository.customerName

    init {
        viewModelScope.launch {
            cartRepository.loadInitialCart()
            customerRepository.loadName()
        }
    }

    fun saveCustomerName(name: String) {
        viewModelScope.launch {
            customerRepository.saveName(name)
        }
    }

    fun clearError() { _currentError.value = null }

    private fun handleError(title: String, e: Throwable) {
        _currentError.value = AppError(title, e.message ?: "Erro desconhecido", e.stackTraceToString())
        AnalyticsManager.logEvent("app_error", mapOf("title" to title, "exception" to (e::class.simpleName ?: "unknown")))
    }

    // Cart
    val cart = cartRepository.cartItems
    val cartTotal = cart.map { items -> items.sumOf { it.product.price * it.quantity } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val cartCount = cart.map { items -> items.sumOf { it.quantity } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch { cartRepository.updateQuantity(productId, quantity) }
    }

    // Orders
    fun loadOrders() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            getOrdersUseCase(token).collect { _orders.value = it }
        }
    }

    fun loadCustomerOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            getCustomerOrdersUseCase(cartRepository.getSessionId()).onSuccess {
                _customerOrders.value = it
            }.onFailure {
                handleError("Falha ao buscar histórico", it)
            }
            _isLoading.value = false
        }
    }

    fun trackOrder(orderId: String) {
        viewModelScope.launch {
            _trackedOrder.value = null
            _isLoading.value = true
            getOrderByIdUseCase(orderId).onSuccess {
                _trackedOrder.value = it
            }.onFailure {
                handleError("Pedido não encontrado", it)
            }
            _isLoading.value = false
        }
    }

    fun submitOrder(page: Page?, onComplete: (String) -> Unit = {}) {
        val ownerId = page?.ownerId
        if (ownerId == null || cart.value.isEmpty()) return
        
        viewModelScope.launch {
            val orderId = "ORD-" + Random.nextInt(100000, 999999).toString()
            val newOrder = Order(
                id = orderId,
                userId = ownerId,
                customerId = cartRepository.getSessionId(),
                customerName = customerName.value,
                items = cart.value,
                total = cartTotal.value,
                status = OrderStatus.PENDING,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                whatsappContact = page.whatsapp,
                theme = page.theme
            )
            
            submitOrderUseCase(newOrder).onSuccess {
                cartRepository.clearCart()
                AnalyticsManager.logEvent("purchase", mapOf("transaction_id" to orderId, "value" to cartTotal.value))
                onComplete(orderId)
            }.onFailure {
                handleError("Falha ao submeter pedido", it)
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            updateOrderStatusUseCase(token, orderId, status).onSuccess {
                loadOrders()
            }.onFailure { 
                handleError("Falha ao atualizar status", it)
            }
        }
    }

    // Pages
    fun loadPages() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val token = authRepository.getCurrentUserToken() ?: ""
                getPagesUseCase(token)
            }.onSuccess {
                _pages.value = it
            }.onFailure {
                handleError("Falha ao carregar páginas", it)
            }
            _isLoading.value = false
        }
    }

    suspend fun loadPublicPage(id: String): Page? {
        return getPublicPageUseCase(id).getOrElse {
            handleError("Erro na página pública", it)
            null
        }
    }

    suspend fun loadPageByDomain(domain: String): Page? = getPageByDomainUseCase(domain).getOrNull()

    suspend fun loadFirstPublicPage(): Page? = getFirstPublicPageUseCase()

    fun savePage(page: Page, isEditing: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            savePageUseCase(page, authRepository.getCurrentUserToken() ?: "", isEditing).onSuccess {
                loadPages()
                onComplete()
            }.onFailure {
                handleError("Falha ao salvar página", it)
            }
            _isLoading.value = false
        }
    }

    fun deletePage(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            deletePageUseCase(id, authRepository.getCurrentUserToken() ?: "").onSuccess {
                loadPages()
                onComplete()
            }.onFailure {
                handleError("Falha ao excluir página", it)
            }
            _isLoading.value = false
        }
    }

    fun uploadImage(bytes: ByteArray, fileName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            uploadImageUseCase(bytes, fileName, authRepository.getCurrentUserToken() ?: "").onSuccess(onSuccess).onFailure {
                handleError("Erro no upload de imagem", it)
            }
            _isLoading.value = false
        }
    }

    // Auth
    suspend fun getCurrentUserToken(): String? = authRepository.getCurrentUserToken()
    
    fun signIn(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.signIn(email, pass).onSuccess {
                AnalyticsManager.logEvent("login")
                onSuccess() 
            }.onFailure { 
                handleError("Erro de Login", it)
                onFailure(it.message ?: "Erro") 
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { 
            AnalyticsManager.trackPageView("logout")
            authRepository.signOut() 
        }
    }

    // Derived States
    val allAvailableProducts: StateFlow<List<Product>> = _pages.map { pageList ->
        val result = mutableListOf<Product>()
        for (page in pageList) {
            for (component in page.components) {
                if (component is PageComponent.ProductList) {
                    result.addAll(component.products)
                }
            }
        }
        result.distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAvailableCategories: StateFlow<List<String>> = allAvailableProducts.map { products ->
        products.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
