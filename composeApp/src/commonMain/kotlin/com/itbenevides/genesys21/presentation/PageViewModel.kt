package com.itbenevides.genesys21.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.data.repository.HybridPageDraftRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import com.itbenevides.genesys21.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.Clock.System.now

data class AppError(
    val title: String,
    val message: String,
    val stackTrace: String? = null,
)

sealed class UiEvent {
    data class ShowAccountLinkingDialog(val email: String) : UiEvent()
}

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
    private val getBookingServicesUseCase: GetBookingServicesUseCase,
    private val saveBookingServiceUseCase: SaveBookingServiceUseCase,
    private val deleteBookingServiceUseCase: DeleteBookingServiceUseCase,
    private val getAppointmentsUseCase: GetAppointmentsUseCase,
    private val createAppointmentUseCase: CreateAppointmentUseCase,
    private val updateAppointmentUseCase: UpdateAppointmentUseCase,
    private val validateBookingSlotUseCase: ValidateBookingSlotUseCase,
    private val getAvailabilityUseCase: GetAvailabilityUseCase,
    private val saveAvailabilityUseCase: SaveAvailabilityUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val updateUserRoleUseCase: UpdateUserRoleUseCase,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val updateUserPermissionsUseCase: UpdateUserPermissionsUseCase,
    private val getTemplatesUseCase: GetTemplatesUseCase,
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
    private val saveAddressUseCase: SaveAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val calculateShippingUseCase: CalculateShippingUseCase,
    private val storeRepository: com.itbenevides.genesys21.domain.repository.StoreRepository,
    private val getDomainMappingsUseCase: GetDomainMappingsUseCase,
    private val saveDomainMappingUseCase: SaveDomainMappingUseCase,
    private val deleteDomainMappingUseCase: DeleteDomainMappingUseCase,
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val getB2BAnalyticsUseCase: GetB2BAnalyticsUseCase,
    private val getAuditLogsUseCase: GetAuditLogsUseCase,
) : ViewModel() {
    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _appTheme = MutableStateFlow(PageThemeConfig.ELEGANCE)
    val appTheme: StateFlow<PageThemeConfig> = _appTheme.asStateFlow()

    private val _userAddresses = MutableStateFlow<List<com.itbenevides.genesys21.domain.model.Address>>(emptyList())
    val userAddresses: StateFlow<List<com.itbenevides.genesys21.domain.model.Address>> = _userAddresses.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.authState.map { it != null }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _customerOrders = MutableStateFlow<List<Order>>(emptyList())
    val customerOrders: StateFlow<List<Order>> = _customerOrders.asStateFlow()

    private val _customerAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val customerAppointments: StateFlow<List<Appointment>> = _customerAppointments.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _services = MutableStateFlow<List<BookingService>>(emptyList())
    val services: StateFlow<List<BookingService>> = _services.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _upcomingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val upcomingAppointments: StateFlow<List<Appointment>> = _upcomingAppointments.asStateFlow()

    private val _availability = MutableStateFlow<MerchantAvailability?>(null)
    val availability: StateFlow<MerchantAvailability?> = _availability.asStateFlow()

    private val _analytics = MutableStateFlow<MerchantAnalytics?>(null)
    val analytics: StateFlow<MerchantAnalytics?> = _analytics.asStateFlow()

    private val _b2bAnalytics = MutableStateFlow<B2BAnalytics?>(null)
    val b2bAnalytics: StateFlow<B2BAnalytics?> = _b2bAnalytics.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<Map<String, String>>>(emptyList())
    val auditLogs: StateFlow<List<Map<String, String>>> = _auditLogs.asStateFlow()

    private val _trackedOrder = MutableStateFlow<Order?>(null)
    val trackedOrder: StateFlow<Order?> = _trackedOrder.asStateFlow()

    private val _isWaitingForPaymentSignal = MutableStateFlow(false)
    val isWaitingForPaymentSignal: StateFlow<Boolean> = _isWaitingForPaymentSignal.asStateFlow()

    private val _templates = MutableStateFlow<List<PageTemplate>>(emptyList())
    val templates: StateFlow<List<PageTemplate>> = _templates.asStateFlow()

    private val _domainMappings = MutableStateFlow<List<DomainMapping>>(emptyList())
    val domainMappings: StateFlow<List<DomainMapping>> = _domainMappings.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    val productSuggestions: StateFlow<List<String>> = _pages.map { allPages ->
        allPages.flatMap { p -> p.components.filterIsInstance<PageComponent.ProductList>() }
            .flatMap { comp -> comp.products.map { it.name } }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categorySuggestions: StateFlow<List<String>> = _categories.map { allCats ->
        allCats.map { it.name }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorEvents = MutableSharedFlow<AppError>()
    val errorEvents = _errorEvents.asSharedFlow()

    private val _uiMessages = MutableSharedFlow<String>()
    val uiMessages = _uiMessages.asSharedFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val customerName = customerRepository.customerName
    val customerPhone = customerRepository.customerPhone

    private val currentStoreId: String
        get() = userProfile.value?.id ?: "admin"

    init {
        loadCategories()
        loadBookingServices()
        loadTemplates()

        viewModelScope.launch {
            customerRepository.loadData()
            cartRepository.loadInitialCart()

            // Reage a mudanças na autenticação (Permanecer Logado)
            authRepository.authState.distinctUntilChanged().collect { uid ->
                if (uid != null) {
                    loadUserProfile(uid)
                    loadPages()
                    loadOrders()
                    cartRepository.mergeWithServer()

                    // Sincroniza tema da loja com o app
                    getStore(uid).onSuccess { store ->
                        _appTheme.value = store.theme
                    }
                } else {
                    _userProfile.value = null
                    _pages.value = emptyList()
                    _orders.value = emptyList()

                    // Tenta One Tap se estiver deslogado
                    authRepository.initializeOneTap()
                }
            }
        }
    }

    fun loadTemplates() {
        _templates.value = getTemplatesUseCase()
    }

    fun saveCustomerName(name: String) =
        viewModelScope.launch {
            customerRepository.saveName(name)
        }

    fun saveCustomerPhone(phone: String) =
        viewModelScope.launch {
            customerRepository.savePhone(phone)
        }

    private fun handleError(
        title: String,
        error: Throwable,
    ) {
        viewModelScope.launch {
            _errorEvents.emit(AppError(title, error.message ?: "Erro desconhecido", error.stackTraceToString()))
        }
        error.printStackTrace()
    }

    val cart: StateFlow<List<CartItem>> = cartRepository.cartItems

    val cartTotal: StateFlow<Double> =
        cart.map { items ->
            items.sumOf { it.price * it.quantity }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val cartCount = cart.map { it.sumOf { item -> item.quantity } }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun addToCart(product: Product): Boolean {
        viewModelScope.launch {
            cartRepository.addToCart(CartItem(product = product, quantity = 1))
            _uiMessages.emit("Produto adicionado ao carrinho!")
        }
        return true
    }

    fun addServiceToCart(service: BookingService, appointment: Appointment) {
        viewModelScope.launch {
            cartRepository.addToCart(
                CartItem(
                    service = service,
                    appointment = appointment,
                    quantity = 1
                )
            )
            _uiMessages.emit("Serviço adicionado ao carrinho!")
        }
    }

    fun addValuedActionToCart(name: String, price: Double, storeId: String) {
        viewModelScope.launch {
            cartRepository.addToCart(
                CartItem(
                    customName = name,
                    customPrice = price,
                    product = Product(id = "valued_action", storeId = storeId, name = name, price = price),
                    quantity = 1
                )
            )
            _uiMessages.emit("Contribuição adicionada ao carrinho!")
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch { cartRepository.removeFromCart(productId) }
    }

    fun clearCart() {
        viewModelScope.launch { cartRepository.clearCart() }
    }

    fun updateCartQuantity(
        productId: String,
        quantity: Int,
    ) {
        viewModelScope.launch { cartRepository.updateQuantity(productId, quantity) }
    }

    fun loadOrders() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            getOrdersUseCase(token)
                .catch { handleError("Erro ao carregar pedidos", it) }
                .onEach { _orders.value = it }
                .onCompletion { _isLoading.value = false }
                .collect()
        }
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            getAnalyticsUseCase(token).onSuccess {
                _analytics.value = it
            }.onFailure {
                handleError("Erro ao carregar analytics", it)
            }
            _isLoading.value = false
        }
    }

    fun loadB2BAnalytics() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            getB2BAnalyticsUseCase(token).onSuccess {
                _b2bAnalytics.value = it
            }.onFailure {
                handleError("Erro ao carregar B2B Insights", it)
            }
            _isLoading.value = false
        }
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            getAuditLogsUseCase(token).onSuccess {
                _auditLogs.value = it
            }.onFailure {
                handleError("Erro ao carregar logs", it)
            }
            _isLoading.value = false
        }
    }

    fun loadCustomerOrders() {
        viewModelScope.launch {
            val sessionId = cartRepository.getSessionId()
            val phone = customerPhone.value

            _isLoading.value = true
            try {
                val currentUserId = authRepository.getCurrentUserId()
                val targetId = currentUserId ?: sessionId

                // Carrega pedidos (usa UID se logado, senão SessionID)
                getCustomerOrdersUseCase(targetId).onSuccess {
                    _customerOrders.value = it
                }.onFailure {
                    handleError("Erro ao carregar histórico de pedidos", it)
                }

                // Carrega agendamentos se tiver telefone
                if (phone.isNotBlank()) {
                    try {
                        _customerAppointments.value = getAppointmentsUseCase.byPhone(phone)
                    } catch (e: Exception) {
                        handleError("Erro ao carregar agendamentos", e)
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun trackOrder(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getOrderByIdUseCase(orderId).onSuccess { order ->
                    _trackedOrder.value = order

                    // Se o pedido está aguardando sinal do Webhook (AWAITING_PAYMENT), inicia polling
                    if (order.status == OrderStatus.AWAITING_PAYMENT) {
                        pollOrderStatus(orderId)
                    }
                }.onFailure {
                    handleError("Erro ao rastrear pedido", it)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun pollOrderStatus(orderId: String) {
        viewModelScope.launch {
            _isWaitingForPaymentSignal.value = true
            var attempts = 0
            val maxAttempts = 20 // ~40 segundos (2s por poll)

            while (attempts < maxAttempts) {
                attempts++
                kotlinx.coroutines.delay(2000)

                getOrderByIdUseCase(orderId).onSuccess { updatedOrder ->
                    _trackedOrder.value = updatedOrder
                    // Se o status mudou, o Webhook bateu!
                    if (updatedOrder.status != OrderStatus.AWAITING_PAYMENT) {
                        _isWaitingForPaymentSignal.value = false
                        return@launch
                    }
                }
            }
            // Timeout plausível atingido sem sinal do Webhook
            _isWaitingForPaymentSignal.value = false
        }
    }

    fun submitOrder(
        page: Page?,
        paymentMethod: PaymentMethod = PaymentMethod.LOCAL,
        shippingAddress: com.itbenevides.genesys21.domain.model.Address? = null,
        shippingPrice: Double = 0.0,
        shippingMethod: String? = null,
        onSuccess: (OrderResponse) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserId = authRepository.getCurrentUserId()
                val currentSessionId = cartRepository.getSessionId()

                // Se a página for nula (acesso direto ao carrinho), tenta pegar o storeId do primeiro item do carrinho
                val inferredStoreId = page?.storeId ?: cart.value.firstOrNull()?.product?.storeId
                    ?: cart.value.firstOrNull()?.service?.storeId
                    ?: ""

                println("VIEWMODEL: Submetendo pedido. StoreId inferido: [$inferredStoreId]")

                val order =
                    Order(
                        id = com.itbenevides.genesys21.util.GenesysUUID.randomUUID(),
                        storeId = inferredStoreId,
                        customerId = currentUserId, // UID real se logado (ou null)
                        sessionId = currentSessionId, // ID da sessão para visitantes
                        customerName = customerName.value,
                        customerEmail = authRepository.getCurrentUserEmail(),
                        customerPhone = customerPhone.value,
                        items = cart.value,
                        total = cartTotal.value,
                        status = if (paymentMethod == PaymentMethod.APP) OrderStatus.AWAITING_PAYMENT else OrderStatus.PENDING,
                        paymentMethod = paymentMethod,
                        shippingAddress = shippingAddress,
                        shippingPrice = shippingPrice,
                        shippingMethod = shippingMethod,
                        whatsappContact = page?.whatsapp,
                        theme = page?.theme ?: PageThemeConfig.ELEGANCE
                    )
                submitOrderUseCase(order).onSuccess { response ->
                    // Limpa o carrinho apenas se for pagamento LOCAL
                    // Para pagamentos via APP (Stripe), limpamos apenas após a confirmação de sucesso
                    if (paymentMethod == PaymentMethod.LOCAL) {
                        cartRepository.clearCart()
                    }
                    onSuccess(response)
                }.onFailure {
                    handleError("Erro ao enviar pedido", it)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
    ) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            try {
                updateOrderStatusUseCase(token, orderId, newStatus).onSuccess {
                    loadOrders()
                }.onFailure {
                    handleError("Erro ao atualizar status", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao atualizar status", e)
            }
        }
    }

    fun loadPages() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            try {
                _pages.value = getPagesUseCase(token)
            } catch (e: Exception) {
                handleError("Erro ao carregar páginas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun loadPublicPage(pageId: String): Page? {
        return getPublicPageUseCase(pageId).getOrNull()
    }

    suspend fun loadPageByDomain(domain: String): Page? {
        return getPageByDomainUseCase(domain).getOrNull()
    }

    suspend fun loadFirstPublicPage(): Page? {
        return getFirstPublicPageUseCase()
    }

    fun savePage(
        page: Page,
        isDraft: Boolean = false,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: ""
            _isLoading.value = true
            try {
                savePageUseCase(page, token, isDraft).onSuccess { _ ->
                    loadPages()
                    onSuccess()
                }.onFailure {
                    handleError("Erro ao salvar página", it)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePage(
        pageId: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            try {
                deletePageUseCase(pageId, token).onSuccess { _ ->
                    loadPages()
                    onSuccess()
                }.onFailure {
                    handleError("Erro ao deletar página", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao deletar página", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadImage(
        bytes: ByteArray,
        fileName: String,
        onSuccess: (String) -> Unit,
    ) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: ""
            _isLoading.value = true
            try {
                uploadImageUseCase(bytes, fileName, token).onSuccess { url ->
                    onSuccess(url)
                }.onFailure {
                    handleError("Erro no upload", it)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            try {
                getCategoriesUseCase(token).onSuccess {
                    _categories.value = it
                }.onFailure {
                    handleError("Erro ao carregar categorias", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao carregar categorias", e)
            }
        }
    }

    fun saveCategory(
        category: Category,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            try {
                saveCategoryUseCase(category, token).onSuccess { _ ->
                    loadCategories()
                    onSuccess()
                }.onFailure {
                    handleError("Erro ao salvar categoria", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao salvar categoria", e)
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            try {
                deleteCategoryUseCase(categoryId, token).onSuccess { _ ->
                    loadCategories()
                }.onFailure {
                    handleError("Erro ao deletar categoria", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao deletar categoria", e)
            }
        }
    }

    fun loadBookingServices() {
        viewModelScope.launch {
            try {
                _services.value = getBookingServicesUseCase()
            } catch (e: Exception) {
                handleError("Erro ao carregar serviços", e)
            }
        }
    }

    fun saveBookingService(
        service: BookingService,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            try {
                saveBookingServiceUseCase(service, token).onSuccess { _ ->
                    loadBookingServices()
                    onSuccess()
                }.onFailure {
                    handleError("Erro ao salvar serviço", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao salvar serviço", e)
            }
        }
    }

    fun deleteBookingService(serviceId: String) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            try {
                deleteBookingServiceUseCase(serviceId, token).onSuccess { _ ->
                    loadBookingServices()
                }.onFailure {
                    handleError("Erro ao deletar serviço", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao deletar serviço", e)
            }
        }
    }

    fun loadAvailability(storeId: String = "") {
        viewModelScope.launch {
            try {
                val mid = storeId.ifBlank { currentStoreId }
                _availability.value = getAvailabilityUseCase(mid)
            } catch (e: Exception) {
                handleError("Erro ao carregar disponibilidade", e)
            }
        }
    }

    fun saveAvailability(availability: MerchantAvailability) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            try {
                saveAvailabilityUseCase(availability, token).onSuccess { _ ->
                    _availability.value = availability
                }.onFailure {
                    handleError("Erro ao salvar disponibilidade", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao salvar disponibilidade", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getAvailableSlots(
        storeId: String,
        service: BookingService,
        date: LocalDate,
    ): List<String> {
        val mid = storeId.ifBlank { currentStoreId }
        val avail = getAvailabilityUseCase(mid)

        // 1. Check if day is blocked
        if (avail?.blockedDates?.contains(date) == true) return emptyList()

        // 2. Get day config
        val dayOfWeek = date.dayOfWeek.ordinal + 1
        val dayConfig = avail?.weeklyConfig?.find { it.dayOfWeek == dayOfWeek }
        if (dayConfig == null || dayConfig.isClosed) return emptyList()

        // 3. Get existing appointments
        val existing = getAppointmentsUseCase(null, mid, date)

        val availableSlots = mutableListOf<String>()
        val durationMs = service.durationMinutes.toLong() * 60L * 1000L
        val nowMs = now().toEpochMilliseconds()

        dayConfig.slots.forEach { range ->
            try {
                val startParts = range.startTime.split(":")
                val endParts = range.endTime.split(":")

                var currentMs =
                    LocalDateTime(date, LocalTime(startParts[0].toInt(), startParts[1].toInt()))
                        .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val endMs =
                    LocalDateTime(date, LocalTime(endParts[0].toInt(), endParts[1].toInt()))
                        .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                while (currentMs + durationMs <= endMs) {
                    val slotStart = currentMs
                    val slotEnd = currentMs + durationMs

                    // 4. Bloqueia horários no passado
                    if (slotStart < nowMs) {
                        currentMs += if (durationMs < 30L * 60L * 1000L) 30L * 60L * 1000L else durationMs
                        continue
                    }

                    val overlappingAppts =
                        existing.filter { appt ->
                            val apptStart = appt.startTime.toEpochMilliseconds()
                            val apptEnd = appt.endTime.toEpochMilliseconds()

                            (slotStart >= apptStart && slotStart < apptEnd) ||
                                (slotEnd > apptStart && slotEnd <= apptEnd) ||
                                (slotStart <= apptStart && slotEnd >= apptEnd)
                        }

                    val isBlockedByOtherService = overlappingAppts.any { it.serviceId != service.id }
                    val currentParticipants = overlappingAppts.count { it.serviceId == service.id }

                    val isAvailable = if (isBlockedByOtherService) {
                        false
                    } else if (currentParticipants > 0) {
                        // Se já tem gente, só permite se for o mesmo serviço e tiver vaga
                        currentParticipants < service.maxParticipants
                    } else {
                        // Slot livre
                        true
                    }

                    if (isAvailable) {
                        val time = Instant.fromEpochMilliseconds(currentMs).toLocalDateTime(TimeZone.currentSystemDefault()).time
                        availableSlots.add("${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}")
                    }

                    // Avança 30 minutos ou o tempo de serviço para o próximo slot sugerido
                    val stepMs = if (durationMs < 30L * 60L * 1000L) 30L * 60L * 1000L else durationMs
                    currentMs += stepMs
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return availableSlots
    }

    fun loadAppointments(
        date: LocalDate,
        storeId: String = "",
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val mid = storeId.ifBlank { currentStoreId }
                // Buscamos todos os agendamentos do mercador para o dia
                _appointments.value = getAppointmentsUseCase(null, mid, date)
            } catch (e: Exception) {
                handleError("Erro ao carregar agenda", e)
            } finally {
                // Sincroniza tema da loja com o app
                getStore(currentStoreId).onSuccess { store ->
                    _appTheme.value = store.theme
                }
                _isLoading.value = false
            }
        }
    }

    fun loadUpcomingAppointments(storeId: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val mid = storeId.ifBlank { currentStoreId }
                // Atualizamos tanto o dia quanto o global para garantir consistência
                _upcomingAppointments.value = getAppointmentsUseCase.upcoming(mid)
                _appointments.value = getAppointmentsUseCase.all(mid)
            } catch (e: Exception) {
                handleError("Erro ao carregar agendamentos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAppointment(
        storeId: String = "",
        appointment: Appointment,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Validation before creating
                val mid = storeId.ifBlank { currentStoreId }
                val isValid =
                    validateBookingSlotUseCase(
                        storeId = mid,
                        serviceId = appointment.serviceId,
                        startTime = appointment.startTime,
                        endTime = appointment.endTime,
                    ).getOrDefault(false)

                if (isValid) {
                    val finalAppointment = if (appointment.customerId == null) {
                        appointment.copy(customerId = authRepository.getCurrentUserId())
                    } else appointment

                    createAppointmentUseCase(finalAppointment).onSuccess { _ ->
                        onSuccess()
                    }.onFailure {
                        handleError("Erro ao agendar", it)
                    }
                } else {
                    handleError("Horário Indisponível", Exception("Este horário já foi ocupado ou não é permitido."))
                }
            } catch (e: Exception) {
                handleError("Erro ao agendar", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            try {
                updateAppointmentUseCase(appointment, token).onSuccess { _ ->
                    // Reload appointments to reflect changes
                    val today = now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    loadAppointments(today, appointment.storeId)
                }.onFailure {
                    handleError("Erro ao atualizar agendamento", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao atualizar agendamento", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    val allAvailableProducts: StateFlow<List<Product>> =
        pages.map { pageList ->
            pageList.asSequence().flatMap { page ->
                page.components.filterIsInstance<PageComponent.ProductList>().flatMap { it.products }
            }.distinctBy { it.id }.toList()
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allAvailableCategories: StateFlow<List<String>> =
        categories.map { categoryList ->
            categoryList.map { it.name }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveDraft(page: Page) {
        pageDraftRepository.saveDraft(page)
    }

    fun getDraft(pageId: String): Page? {
        return pageDraftRepository.getDraft(pageId)
    }

    fun syncDraftFromServer(pageId: String, onSynced: (Page?) -> Unit) {
        viewModelScope.launch {
            (pageDraftRepository as? HybridPageDraftRepository)?.syncFromRemote(pageId)?.let {
                onSynced(it)
            } ?: onSynced(null)
        }
    }

    fun clearDraft(pageId: String) {
        pageDraftRepository.clearDraft(pageId)
    }

    fun prefetchProductDetails(product: Product) {
        println("Prefetching details for: ${product.name}")
    }

    suspend fun calculateShipping(storeId: String, zipCode: String): List<com.itbenevides.genesys21.domain.model.ShippingOption> {
        return calculateShippingUseCase(storeId, zipCode).getOrDefault(emptyList())
    }

    fun getCurrentUserToken(): String? {
        return null // Should be handled by suspend calls
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            getUserProfileUseCase(userId).onSuccess { profile ->
                // DOGMA/GOD-MODE: Sincronização reativa de e-mail para garantir o cargo SuperAdmin
                val currentAuthEmail = authRepository.getCurrentUserEmail() ?: profile.email
                val ownerEmail = "victorkoto@gmail.com"

                val isOwner = currentAuthEmail.lowercase().trim() == ownerEmail ||
                             currentAuthEmail.lowercase().trim() == com.itbenevides.genesys21.domain.model.DogmaConstants.OWNER_EMAIL

                val finalProfile = if (isOwner) {
                    println("VIEWMODEL: [GOD MODE] Dono detectado ($currentAuthEmail). Forçando SuperAdmin.")
                    profile.copy(
                        email = currentAuthEmail,
                        role = UserRole.SUPERADMIN,
                        permissions = com.itbenevides.genesys21.domain.model.UserPermission.entries.toSet()
                    )
                } else profile

                _userProfile.value = finalProfile
                loadUserAddresses(userId)

                if (finalProfile.email.isBlank()) {
                    syncInitialProfile(userId)
                }
            }.onFailure {
                syncInitialProfile(userId)
            }
        }
    }

    private suspend fun syncInitialProfile(userId: String) {
        val email = authRepository.getCurrentUserEmail() ?: ""
        val name = authRepository.getCurrentUserName() ?: email.substringBefore("@").ifBlank { "Novo Usuário" }

        println("VIEWMODEL: Sincronizando perfil inicial para Email: [$email], Nome: [$name], UID: [$userId]")

        if (email.isBlank()) {
            println("VIEWMODEL: ABORTANDO. E-mail ausente. Verifique a ponte JS/Auth Bridge.")
            return
        }

        val newProfile = UserProfile(
            id = userId,
            email = email,
            name = name,
            role = UserRole.CUSTOMER,
            status = UserStatus.APPROVED,
            permissions = emptySet()
        )

        saveUserProfileUseCase(newProfile).onSuccess {
            // Recarrega do servidor para garantir que promoções (como SUPERADMIN) sejam aplicadas imediatamente
            loadUserProfile(userId)
            println("VIEWMODEL: Perfil inicial sincronizado com sucesso.")
        }.onFailure { e ->
            handleError("Erro ao sincronizar perfil", e)
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            saveUserProfileUseCase(profile).onSuccess {
                _userProfile.value = profile
            }.onFailure { e ->
                handleError("Erro ao salvar perfil", e)
            }
        }
    }

    private fun loadUserAddresses(userId: String) {
        viewModelScope.launch {
            _userAddresses.value = getAddressesUseCase(userId)
        }
    }

    fun saveAddress(address: com.itbenevides.genesys21.domain.model.Address) {
        viewModelScope.launch {
            val userId = _userProfile.value?.id ?: return@launch
            saveAddressUseCase(address.copy(userId = userId)).onSuccess {
                loadUserAddresses(userId)
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            val userId = _userProfile.value?.id ?: return@launch
            deleteAddressUseCase(addressId).onSuccess {
                loadUserAddresses(userId)
            }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            getAllUsersUseCase(token).onSuccess {
                _allUsers.value = it
            }.onFailure {
                handleError("Erro ao carregar usuários", it)
            }
            _isLoading.value = false
        }
    }

    fun updateUserRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            updateUserRoleUseCase(token, userId, role).onSuccess {
                loadAllUsers()
            }.onFailure {
                handleError("Erro ao atualizar cargo", it)
            }
            _isLoading.value = false
        }
    }

    fun updateUserPermissions(userId: String, permissions: Set<UserPermission>) {
        viewModelScope.launch {
            val token = authRepository.getCurrentUserToken() ?: return@launch
            _isLoading.value = true
            updateUserPermissionsUseCase(token, userId, permissions).onSuccess {
                loadAllUsers()
            }.onFailure {
                handleError("Erro ao atualizar permissões", it)
            }
            _isLoading.value = false
        }
    }

    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.signIn(email, password).onSuccess { token ->
                    val userId = authRepository.getCurrentUserId()
                    if (userId != null) {
                        loadUserProfile(userId)
                    }
                    onSuccess()
                }.onFailure {
                    onError(it.message ?: "Erro desconhecido")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Erro desconhecido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.signUp(email, password).onSuccess { token ->
                    val userId = authRepository.getCurrentUserId()
                    if (userId != null) {
                        loadUserProfile(userId)
                    }
                    onSuccess()
                }.onFailure {
                    onError(it.message ?: "Erro desconhecido")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Erro desconhecido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithToken(
        idToken: String,
        accessToken: String?,
        provider: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.signIn(idToken, accessToken, provider).onSuccess { token ->
                    val userId = authRepository.getCurrentUserId()
                    if (userId != null) {
                        loadUserProfile(userId)
                    }
                    onSuccess()
                }.onFailure { e ->
                    if (e.message == "ACCOUNT_EXISTS_PASSWORD") {
                        _uiEvent.emit(UiEvent.ShowAccountLinkingDialog(email = "Seu e-mail"))
                    } else {
                        handleError("Erro de Login", e)
                        onError(e.message ?: "Erro desconhecido")
                    }
                }
            } catch (e: Exception) {
                handleError("Falha na Autenticação", e)
                onError(e.message ?: "Erro desconhecido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            // Limpeza profunda de estado para segurança (Spec 015)
            _pages.value = emptyList()
            _orders.value = emptyList()
            _userProfile.value = null
            _userAddresses.value = emptyList()
            _allUsers.value = emptyList()
            _customerOrders.value = emptyList()
            _customerAppointments.value = emptyList()
            _appointments.value = emptyList()
            _upcomingAppointments.value = emptyList()
            _availability.value = null
            _analytics.value = null
            _trackedOrder.value = null
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val userId = _userProfile.value?.id ?: return@launch
            _isLoading.value = true
            try {
                // 1. Delete on server (handles DB anonymization)
                deleteUserUseCase(userId)
                    .onSuccess {
                        // 2. Sign out locally and clear state
                        signOut()
                        onSuccess()
                    }
                    .onFailure { handleError("Erro ao excluir conta", it) }
            } catch (e: Exception) {
                handleError("Erro ao excluir conta", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getStore(id: String) = storeRepository.getStore(id)

    fun saveStore(store: com.itbenevides.genesys21.domain.model.Store, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = authRepository.getCurrentUserToken() ?: return@launch
                storeRepository.saveStore(store, token).onSuccess {
                    // Atualiza tema global se mudou na loja salva
                    _appTheme.value = store.theme
                    onComplete()
                }.onFailure {
                    handleError("Erro ao salvar loja", it)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun connectStripe(storeId: String, email: String, onUrlReady: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = authRepository.getCurrentUserToken() ?: return@launch
                storeRepository.createConnectAccount(storeId, email, token).onSuccess {
                    storeRepository.getConnectOnboardingLink(storeId, token).onSuccess { url ->
                        onUrlReady(url)
                    }.onFailure { handleError("Erro ao gerar link de cadastro", it) }
                }.onFailure { handleError("Erro ao criar conta Stripe", it) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setAppTheme(theme: PageThemeConfig) {
        _appTheme.value = theme
        // Salva globalmente na loja
        viewModelScope.launch {
            getStore(currentStoreId).onSuccess { current ->
                saveStore(current.copy(theme = theme)) {}
            }
        }
    }

    fun openStripeDashboard(storeId: String, onUrlReady: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = authRepository.getCurrentUserToken() ?: return@launch
                storeRepository.getConnectLoginLink(storeId, token).onSuccess { url ->
                    onUrlReady(url)
                }.onFailure { handleError("Erro ao abrir Dashboard Stripe", it) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getAccountSession(storeId: String): Result<String> {
        val token = authRepository.getCurrentUserToken() ?: return Result.failure(Exception("Não autenticado"))
        return storeRepository.getAccountSession(storeId, token)
    }

    fun loadDomainMappings() {
        viewModelScope.launch {
            _isLoading.value = true
            getDomainMappingsUseCase().onSuccess {
                _domainMappings.value = it
            }.onFailure {
                handleError("Erro ao carregar domínios", it)
            }
            _isLoading.value = false
        }
    }

    fun saveDomainMapping(domain: String, targetPageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val mapping = DomainMapping(id = "", domain = domain, targetPageId = targetPageId)
            saveDomainMappingUseCase(mapping).onSuccess {
                loadDomainMappings()
            }.onFailure {
                handleError("Erro ao salvar domínio", it)
            }
            _isLoading.value = false
        }
    }

    fun deleteDomainMapping(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            deleteDomainMappingUseCase(id).onSuccess {
                loadDomainMappings()
            }.onFailure {
                handleError("Erro ao excluir domínio", it)
            }
            _isLoading.value = false
        }
    }

    fun loadChatMessages(refId: String) {
        viewModelScope.launch {
            getChatMessagesUseCase(refId).onSuccess {
                _chatMessages.value = it
            }
        }
    }

    fun sendChatMessage(refId: String, nick: String, content: String, isFromMerchant: Boolean = false) {
        viewModelScope.launch {
            val message = ChatMessage(
                id = "",
                refId = refId,
                senderNick = nick,
                content = content,
                isFromMerchant = isFromMerchant,
                createdAt = now().toEpochMilliseconds()
            )
            sendChatMessageUseCase(message).onSuccess {
                loadChatMessages(refId)
            }.onFailure {
                handleError("Erro ao enviar mensagem", it)
            }
        }
    }
}
