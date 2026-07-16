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
    override suspend fun getAppointments(serviceId: String, date: LocalDate): List<Appointment> = appointmentsList
    override suspend fun createAppointment(appointment: Appointment) {
        appointmentsList.add(appointment)
    }
}
