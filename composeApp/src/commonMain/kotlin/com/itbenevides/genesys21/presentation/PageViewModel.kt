package com.itbenevides.genesys21.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import com.itbenevides.genesys21.domain.repository.PageDraftRepository
import com.itbenevides.genesys21.domain.usecase.*
import com.itbenevides.genesys21.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class AppError(
    val title: String,
    val message: String,
    val stackTrace: String? = null,
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
    private val getBookingServicesUseCase: GetBookingServicesUseCase,
    private val saveBookingServiceUseCase: SaveBookingServiceUseCase,
    private val deleteBookingServiceUseCase: DeleteBookingServiceUseCase,
    private val getAppointmentsUseCase: GetAppointmentsUseCase,
    private val createAppointmentUseCase: CreateAppointmentUseCase,
    private val updateAppointmentUseCase: UpdateAppointmentUseCase,
    private val validateBookingSlotUseCase: ValidateBookingSlotUseCase,
    private val getAvailabilityUseCase: GetAvailabilityUseCase,
    private val saveAvailabilityUseCase: SaveAvailabilityUseCase,
) : ViewModel() {
    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

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

    private val _availability = MutableStateFlow<MerchantAvailability?>(null)
    val availability: StateFlow<MerchantAvailability?> = _availability.asStateFlow()

    private val _trackedOrder = MutableStateFlow<Order?>(null)
    val trackedOrder: StateFlow<Order?> = _trackedOrder.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError: StateFlow<AppError?> = _currentError.asStateFlow()

    val customerName = customerRepository.customerName
    val customerPhone = customerRepository.customerPhone

    init {
        loadPages()
        loadCategories()
        loadBookingServices()
        viewModelScope.launch {
            customerRepository.loadData()
            cartRepository.loadInitialCart()
        }
    }

    fun saveCustomerName(name: String) =
        viewModelScope.launch {
            customerRepository.saveName(name)
        }

    fun saveCustomerPhone(phone: String) =
        viewModelScope.launch {
            customerRepository.savePhone(phone)
        }

    fun clearError() {
        _currentError.value = null
    }

    private fun handleError(
        title: String,
        error: Throwable,
    ) {
        _currentError.value = AppError(title, error.message ?: "Erro desconhecido", error.stackTraceToString())
        error.printStackTrace()
    }

    val cart: StateFlow<List<CartItem>> = cartRepository.cartItems

    val cartTotal: StateFlow<Double> =
        cart.map { items ->
            items.sumOf { it.product.price * it.quantity }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val cartCount = cart.map { it.sumOf { item -> item.quantity } }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun addToCart(product: Product): Boolean {
        viewModelScope.launch {
            cartRepository.addToCart(CartItem(product, 1))
        }
        return true
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch { cartRepository.removeFromCart(productId) }
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

    fun loadCustomerOrders() {
        val sessionId = cartRepository.getSessionId()
        val phone = customerPhone.value
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Carrega pedidos
                getCustomerOrdersUseCase(sessionId).onSuccess {
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
                getOrderByIdUseCase(orderId).onSuccess {
                    _trackedOrder.value = it
                }.onFailure {
                    handleError("Erro ao rastrear pedido", it)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitOrder(
        page: Page?,
        paymentMethod: String = "CASH",
        onSuccess: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val order =
                    Order(
                        id = "",
                        userId = page?.ownerId ?: "",
                        customerName = customerName.value,
                        customerPhone = customerPhone.value,
                        items = cart.value,
                        total = cartTotal.value,
                        status = OrderStatus.PENDING,
                        createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                    )
                submitOrderUseCase(order).onSuccess { _ ->
                    cartRepository.clearCart()
                    onSuccess("Pedido enviado!")
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

    fun deleteCategory(categoryId: Int) {
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
            try {
                saveBookingServiceUseCase(service).onSuccess { _ ->
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
            try {
                deleteBookingServiceUseCase(serviceId).onSuccess { _ ->
                    loadBookingServices()
                }.onFailure {
                    handleError("Erro ao deletar serviço", it)
                }
            } catch (e: Exception) {
                handleError("Erro ao deletar serviço", e)
            }
        }
    }

    fun loadAvailability(merchantId: String) {
        viewModelScope.launch {
            try {
                val mid = merchantId.ifBlank { "admin" }
                _availability.value = getAvailabilityUseCase(mid)
            } catch (e: Exception) {
                handleError("Erro ao carregar disponibilidade", e)
            }
        }
    }

    fun saveAvailability(availability: MerchantAvailability) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                saveAvailabilityUseCase(availability).onSuccess { _ ->
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
        merchantId: String,
        service: BookingService,
        date: LocalDate
    ): List<String> {
        val mid = merchantId.ifBlank { "admin" }
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
        val durationMs = service.durationMinutes * 60 * 1000L
        val nowMs = kotlin.time.Clock.System.now().toEpochMilliseconds()

        dayConfig.slots.forEach { range ->
            try {
                val startParts = range.startTime.split(":")
                val endParts = range.endTime.split(":")

                var currentMs = LocalDateTime(date, LocalTime(startParts[0].toInt(), startParts[1].toInt()))
                    .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val endMs = LocalDateTime(date, LocalTime(endParts[0].toInt(), endParts[1].toInt()))
                    .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                while (currentMs + durationMs <= endMs) {
                    val slotStart = currentMs
                    val slotEnd = currentMs + durationMs

                    // 4. Bloqueia horários no passado
                    if (slotStart < nowMs) {
                        currentMs += if (durationMs < 30 * 60 * 1000L) 30 * 60 * 1000L else durationMs
                        continue
                    }

                    val hasOverlap = existing.any { appt ->
                        val apptStart = appt.startTime.toEpochMilliseconds()
                        val apptEnd = appt.endTime.toEpochMilliseconds()

                        (slotStart in apptStart..<apptEnd) ||
                        (slotEnd > apptStart && slotEnd <= apptEnd) ||
                        (slotStart <= apptStart && slotEnd >= apptEnd)
                    }

                    if (!hasOverlap) {
                        val time = Instant.fromEpochMilliseconds(currentMs).toLocalDateTime(TimeZone.currentSystemDefault()).time
                        availableSlots.add("${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}")
                    }

                    // Avança 30 minutos ou o tempo de serviço para o próximo slot sugerido
                    val stepMs = if (durationMs < 30 * 60 * 1000L) 30 * 60 * 1000L else durationMs
                    currentMs += stepMs
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return availableSlots
    }

    fun loadAppointments(date: LocalDate, merchantId: String = "admin") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Buscamos todos os agendamentos do mercador para o dia
                _appointments.value = getAppointmentsUseCase(null, merchantId, date)
            } catch (e: Exception) {
                handleError("Erro ao carregar agenda", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAppointment(
        merchantId: String,
        appointment: Appointment,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Validation before creating
                val mid = merchantId.ifBlank { "admin" }
                val isValid = validateBookingSlotUseCase(
                    merchantId = mid,
                    serviceId = appointment.serviceId,
                    startTime = appointment.startTime,
                    endTime = appointment.endTime
                ).getOrDefault(false)

                if (isValid) {
                    createAppointmentUseCase(appointment).onSuccess { _ ->
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
            _isLoading.value = true
            try {
                updateAppointmentUseCase(appointment).onSuccess { _ ->
                    // Reload appointments to reflect changes
                    val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    loadAppointments(today, appointment.merchantId)
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
            pageList.flatMap { page ->
                page.components.filterIsInstance<PageComponent.ProductList>().flatMap { it.products }
            }.distinctBy { it.id }
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

    fun clearDraft(pageId: String) {
        pageDraftRepository.clearDraft(pageId)
    }

    fun prefetchProductDetails(product: Product) {
        println("Prefetching details for: ${product.name}")
    }

    fun getCurrentUserToken(): String? {
        return null // Should be handled by suspend calls
    }

    fun signIn(
        idToken: String,
        provider: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.signIn(idToken, provider).onSuccess { _ ->
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

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _pages.value = emptyList()
        }
    }
}
