package com.itbenevides.genesys21.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.*
import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.util.Analytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    private val customerRepository: CustomerRepository,
    private val pageDraftRepository: PageDraftRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()
    
    private val _isCreatingCheckout = MutableStateFlow(false)
    val isCreatingCheckout: StateFlow<Boolean> = _isCreatingCheckout.asStateFlow()

    private val _customerOrders = MutableStateFlow<List<Order>>(emptyList())
    val customerOrders: StateFlow<List<Order>> = _customerOrders.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _trackedOrder = MutableStateFlow<Order?>(null)
    val trackedOrder: StateFlow<Order?> = _trackedOrder.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError = _currentError.asStateFlow()

    val customerName = customerRepository.customerName
    val customerPhone = customerRepository.customerPhone

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            cartRepository.loadInitialCart()
            customerRepository.loadData()
        }
    }

    fun saveCustomerName(name: String) {
        viewModelScope.launch {
            customerRepository.saveName(name)
        }
    }

    fun saveCustomerPhone(phone: String) {
        viewModelScope.launch {
            customerRepository.savePhone(phone)
        }
    }
    
    fun clearPaymentUrl() {
        _paymentUrl.value = null
    }

    fun clearError() { _currentError.value = null }

    private fun handleError(title: String, e: Throwable) {
        println("LOG_ERROR [$title]: ${e.message}")
        _currentError.value = AppError(title, e.message ?: "Erro desconhecido", e.stackTraceToString())
        Analytics.logException(throwable = e, message = title, additionalParams = mapOf("error_context" to title))
    }

    // Cart
    val cart = cartRepository.cartItems
    val cartTotal = cart.map { items -> items.sumOf { it.product.price * it.quantity } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val cartCount = cart.map { items -> items.sumOf { it.quantity } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addToCart(product: Product): Boolean {
        if (product.stock <= 0) return false
        viewModelScope.launch {
            cartRepository.addToCart(CartItem(product, 1)).onFailure { handleError("Erro ao adicionar ao carrinho", it) }
            Analytics.logEvent("add_to_cart", mapOf("item_id" to product.id, "item_name" to product.name))
        }
        return true
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch { cartRepository.removeFromCart(productId).onFailure { handleError("Erro ao remover do carrinho", it) } }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch { cartRepository.updateQuantity(productId, quantity).onFailure { handleError("Erro ao atualizar quantidade", it) } }
    }

    // Orders
    fun loadOrders() {
        viewModelScope.launch {
            try {
                val token = authRepository.getCurrentUserToken() ?: ""
                if (token.isNotBlank()) {
                    _isLoading.value = true
                    getOrdersUseCase(token).collect { 
                        _orders.value = it 
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                handleError("Falha ao carregar pedidos", e)
            }
        }
    }

    fun loadCustomerOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            getCustomerOrdersUseCase(cartRepository.getSessionId())
                .onSuccess { _customerOrders.value = it }
                .onFailure { handleError("Falha ao buscar histórico", it) }
            _isLoading.value = false
        }
    }

    fun trackOrder(orderId: String) {
        stopOrderPolling() // Cancela qualquer polling anterior

        viewModelScope.launch {
            _trackedOrder.value = null
            _isLoading.value = true
            getOrderByIdUseCase(orderId)
                .onSuccess { initialOrder ->
                    _trackedOrder.value = initialOrder
                    // Se o pedido está em um estado que pode mudar, inicia o polling
                    if (initialOrder.status in listOf(OrderStatus.PENDING, OrderStatus.PAYMENT_PENDING, OrderStatus.PROCESSING)) {
                        startOrderPolling(orderId)
                    }
                }
                .onFailure { handleError("Pedido não encontrado", it) }
            _isLoading.value = false
        }
    }
    
    private fun startOrderPolling(orderId: String) {
        pollingJob = viewModelScope.launch {
            while (isActive) { // Este loop roda enquanto o scope do ViewModel estiver ativo
                delay(5000) // Aguarda 5 segundos para a próxima verificação

                getOrderByIdUseCase(orderId)
                    .onSuccess { updatedOrder ->
                        // Atualiza a UI somente se o status mudou, para evitar recomposições desnecessárias
                        if (_trackedOrder.value?.status != updatedOrder.status) {
                            _trackedOrder.value = updatedOrder
                        }

                        // Para o polling se o pedido atingiu um estado final
                        if (updatedOrder.status !in listOf(OrderStatus.PENDING, OrderStatus.PAYMENT_PENDING, OrderStatus.PROCESSING)) {
                            stopOrderPolling()
                        }
                    }
                    .onFailure {
                        // Para o polling em caso de erro (ex: pedido não encontrado)
                        handleError("Falha no Polling do Pedido", it)
                        stopOrderPolling()
                    }
            }
        }
    }

    fun stopOrderPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // Chame este método na sua UI quando a tela de tracking for descartada
    fun onDispose() {
        stopOrderPolling()
    }

    fun submitOrder(page: Page?, phone: String = "", onComplete: (String) -> Unit = {}) {
        val ownerId = page?.ownerId
        if (ownerId == null || cart.value.isEmpty()) {
            handleError("Erro no Pedido", Exception("Não foi possível identificar o dono desta vitrine ou carrinho vazio."))
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            val orderId = "ORD-" + Random.nextInt(100000, 999999).toString()
            
            val newOrder = Order(id = orderId, userId = ownerId, customerId = cartRepository.getSessionId(), customerName = customerName.value, customerPhone = if (phone.isNotBlank()) phone else customerPhone.value, items = cart.value, total = cartTotal.value, status = OrderStatus.PENDING, createdAt = 0L, whatsappContact = page.whatsapp, theme = page.theme)
            
            submitOrderUseCase(newOrder)
                .onSuccess { generatedId ->
                    cartRepository.clearCart()
                    if (phone.isNotBlank()) saveCustomerPhone(phone)
                    Analytics.logEvent("purchase", mapOf("transaction_id" to generatedId, "value" to cartTotal.value))
                    onComplete(generatedId)
                }
                .onFailure { handleError("Falha ao submeter pedido", it) }
            _isLoading.value = false
        }
    }
    
    suspend fun createMercadoPagoCheckout(page: Page?) {
        val ownerId = page?.ownerId
        if (ownerId == null || cart.value.isEmpty()) {
            handleError("Erro no Pedido", Exception("Não foi possível identificar o dono desta vitrine ou carrinho vazio."))
            return
        }

        viewModelScope.launch {
            _isCreatingCheckout.value = true
            val orderId = "MP-" + Random.nextInt(100000, 999999).toString()
            val newOrder = Order(id = orderId, userId = ownerId, customerId = cartRepository.getSessionId(), customerName = customerName.value, customerPhone = customerPhone.value, items = cart.value, total = cartTotal.value, status = OrderStatus.PENDING, createdAt = 0L, whatsappContact = page.whatsapp, theme = page.theme)
            val token = authRepository.getCurrentUserToken() ?: ""

            orderRepository.createMercadoPagoCheckout(newOrder, token)
                .onSuccess { 
                    _paymentUrl.value = it 
                    cartRepository.clearCart()
                    Analytics.logEvent("begin_checkout", mapOf("transaction_id" to orderId, "value" to cartTotal.value))
                }
                .onFailure { handleError("Falha ao criar checkout", it) }
            _isCreatingCheckout.value = false
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            updateOrderStatusUseCase(token, orderId, status)
                .onFailure { handleError("Falha ao atualizar status", it) }
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
                loadCategories() 
            }.onFailure {
                handleError("Falha ao carregar páginas", it)
            }
            _isLoading.value = false
        }
    }

    suspend fun loadPublicPage(id: String): Page? {
        return getPublicPageUseCase(id)
            .getOrElse {
                handleError("Erro na página pública", it)
                null
            }
    }

    suspend fun loadPageByDomain(domain: String): Page? {
        return getPageByDomainUseCase(domain).getOrNull()
    }

    suspend fun loadFirstPublicPage(): Page? {
        val page = getFirstPublicPageUseCase()
        page?.let { _pages.value = listOf(it) }
        return page
    }

    fun savePage(page: Page, isEditing: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            savePageUseCase(page, authRepository.getCurrentUserToken() ?: "", isEditing)
                .onSuccess {
                    pageDraftRepository.clearDraft(page.id)
                    loadPages()
                    onComplete()
                }
                .onFailure { handleError("Falha ao salvar página", it) }
            _isLoading.value = false
        }
    }

    fun deletePage(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            deletePageUseCase(id, authRepository.getCurrentUserToken() ?: "")
                .onSuccess {
                    pageDraftRepository.clearDraft(id)
                    loadPages()
                    onComplete()
                }
                .onFailure { handleError("Falha ao excluir página", it) }
            _isLoading.value = false
        }
    }

    fun uploadImage(bytes: ByteArray, fileName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            uploadImageUseCase(bytes, fileName, authRepository.getCurrentUserToken() ?: "")
                .onSuccess(onSuccess)
                .onFailure { handleError("Erro no upload de imagem", it) }
            _isLoading.value = false
        }
    }

    // Categories
    fun loadCategories() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            getCategoriesUseCase(token)
                .onFailure { handleError("Falha ao carregar categorias", it) }
        }
    }

    fun saveCategory(category: Category, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            saveCategoryUseCase(category, token)
                .onSuccess {
                    loadCategories()
                    onComplete()
                }
                .onFailure { handleError("Falha ao salvar categoria", it) }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            deleteCategoryUseCase(id, token)
                .onSuccess { loadCategories() }
                .onFailure { handleError("Falha ao excluir categoria", it) }
        }
    }

    val allAvailableProducts: StateFlow<List<Product>> = _pages.map { pageList ->
        pageList.flatMap { page -> page.components.filterIsInstance<PageComponent.ProductList>().flatMap { it.products } }
            .distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAvailableCategories: StateFlow<List<String>> = combine(categories, allAvailableProducts) { cats, products ->
        (cats.map { it.name } + products.mapNotNull { it.categoryName })
            .filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Drafts
    fun saveDraft(page: Page) { pageDraftRepository.saveDraft(page) }
    fun getDraft(pageId: String): Page? = pageDraftRepository.getDraft(pageId)
    fun clearDraft(pageId: String) { pageDraftRepository.clearDraft(pageId) }

    // Auth
    suspend fun getCurrentUserToken(): String? = authRepository.getCurrentUserToken()
    
    fun signIn(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.signIn(email, pass)
                .onSuccess {
                    Analytics.logEvent("login")
                    loadPages()
                    onSuccess() 
                }
                .onFailure { 
                    handleError("Erro de Login", it)
                    onFailure(it.message ?: "Erro") 
                }
        }
    }

    fun signOut() {
        viewModelScope.launch { 
            Analytics.trackPageView("logout")
            authRepository.signOut().onFailure { handleError("Erro ao fazer Logout", it) }
        }
    }
}
