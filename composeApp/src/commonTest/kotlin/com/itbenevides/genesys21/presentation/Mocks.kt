package com.itbenevides.genesys21.presentation

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

class FakeCartRepository : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems = _cartItems.asStateFlow()

    override suspend fun addToCart(item: CartItem): Result<Unit> {
        _cartItems.value = _cartItems.value + item
        return Result.success(Unit)
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
        return Result.success(Unit)
    }

    override suspend fun updateQuantity(
        productId: String,
        quantity: Int,
    ): Result<Unit> {
        _cartItems.value =
            _cartItems.value.map {
                if (it.product.id == productId) it.copy(quantity = quantity) else it
            }
        return Result.success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> {
        _cartItems.value = emptyList()
        return Result.success(Unit)
    }

    override suspend fun syncWithServer(): Result<Unit> = Result.success(Unit)

    override suspend fun loadInitialCart() {}

    override fun getSessionId(): String = "test-session"
}

class FakeCustomerRepository : CustomerRepository {
    override val customerName = MutableStateFlow("")
    override val customerPhone = MutableStateFlow("")

    override suspend fun saveName(name: String) {
        customerName.value = name
    }

    override suspend fun savePhone(phone: String) {
        customerPhone.value = phone
    }

    override suspend fun loadData() {}

    override suspend fun loadName() {}
}

class FakeOrderRepository : OrderRepository {
    override fun getOrders(token: String) = flowOf(emptyList<Order>())

    override suspend fun createOrder(order: Order) = Result.success(Unit)

    override suspend fun getCustomerOrders(sessionId: String) = Result.success(emptyList<Order>())

    override suspend fun getOrderById(orderId: String): Result<Order> = Result.failure(Exception("Not found"))

    override suspend fun updateOrderStatus(
        token: String,
        orderId: String,
        status: OrderStatus,
    ) = Result.success(Unit)
}

class FakeBookingRepository : BookingRepository {
    private var servicesList = mutableListOf<BookingService>()
    private val appointmentsList = mutableListOf<Appointment>()
    private var merchantAvailability: MerchantAvailability? = null

    override suspend fun getServices(): List<BookingService> = servicesList

    override suspend fun getServiceById(id: String): BookingService? = servicesList.find { it.id == id }

    override suspend fun saveService(service: BookingService) {
        servicesList.add(service)
    }

    override suspend fun deleteService(id: String) {
        servicesList.removeAll { it.id == id }
    }

    override suspend fun getAvailability(merchantId: String): MerchantAvailability? = merchantAvailability

    override suspend fun saveAvailability(availability: MerchantAvailability) {
        this.merchantAvailability = availability
    }

    override suspend fun getAppointments(
        serviceId: String?,
        merchantId: String?,
        date: LocalDate,
    ): List<Appointment> = appointmentsList

    override suspend fun createAppointment(appointment: Appointment) {
        appointmentsList.add(appointment)
    }

    override suspend fun updateAppointment(appointment: Appointment) {
        val index = appointmentsList.indexOfFirst { it.id == appointment.id }
        if (index != -1) {
            appointmentsList[index] = appointment
        }
    }

    override suspend fun getAppointmentsByPhone(phone: String): List<Appointment> {
        return appointmentsList.filter { it.customerPhone == phone }
    }
}

class FakeUserRepository : UserRepository {
    private val users = mutableListOf<UserProfile>()

    override suspend fun getUserProfile(id: String): Result<UserProfile> {
        return users.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(Exception("Not found"))
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        users.add(profile)
        return Result.success(Unit)
    }

    override suspend fun getAllUsers(token: String): Result<List<UserProfile>> = Result.success(users)

    override suspend fun updateUserRole(token: String, userId: String, role: UserRole): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateUserStatus(token: String, userId: String, status: UserStatus): Result<Unit> {
        return Result.success(Unit)
    }
}
