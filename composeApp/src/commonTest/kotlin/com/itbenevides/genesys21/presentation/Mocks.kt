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
        _cartItems.value = _cartItems.value.filter { (it.product?.id ?: it.service?.id) != productId }
        return Result.success(Unit)
    }

    override suspend fun updateQuantity(
        productId: String,
        quantity: Int,
    ): Result<Unit> {
        _cartItems.value =
            _cartItems.value.map {
                if ((it.product?.id ?: it.service?.id) == productId) it.copy(quantity = quantity) else it
            }
        return Result.success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> {
        _cartItems.value = emptyList()
        return Result.success(Unit)
    }

    override suspend fun syncWithServer(): Result<Unit> = Result.success(Unit)

    override suspend fun mergeWithServer(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun loadInitialCart() {}

    override suspend fun getSessionId(): String = "test-session"
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

    override suspend fun createOrder(order: Order) = Result.success(OrderResponse(orderId = "fake-order-id"))

    override suspend fun getCustomerOrders(sessionId: String) = Result.success(emptyList<Order>())

    override suspend fun getOrderById(orderId: String): Result<Order> = Result.failure(Exception("Not found"))

    override suspend fun updateOrderStatus(
        token: String,
        orderId: String,
        status: OrderStatus,
    ) = Result.success(Unit)

    override suspend fun getAnalytics(token: String) = Result.success(
        MerchantAnalytics(
            dailyRevenue = emptyList(),
            topProducts = emptyList(),
            bookingSummary = BookingSummary(0, 0, 0, 0),
            totalOrders = 0,
            averageTicket = 0.0
        )
    )
}

class FakeBookingRepository : BookingRepository {
    private var servicesList = mutableListOf<BookingService>()
    private val appointmentsList = mutableListOf<Appointment>()
    private var merchantAvailability: MerchantAvailability? = null

    override suspend fun getServices(): List<BookingService> = servicesList

    override suspend fun getServiceById(id: String): BookingService? = servicesList.find { it.id == id }

    override suspend fun saveService(service: BookingService, token: String) {
        servicesList.add(service)
    }

    override suspend fun deleteService(id: String, token: String) {
        servicesList.removeAll { it.id == id }
    }

    override suspend fun getAvailability(storeId: String): MerchantAvailability? = merchantAvailability

    override suspend fun saveAvailability(availability: MerchantAvailability, token: String) {
        this.merchantAvailability = availability
    }

    override suspend fun getAppointments(
        serviceId: String?,
        storeId: String?,
        date: LocalDate,
    ): List<Appointment> = appointmentsList

    override suspend fun getAllAppointments(storeId: String): List<Appointment> = appointmentsList

    override suspend fun getUpcomingAppointments(storeId: String): List<Appointment> = appointmentsList

    override suspend fun createAppointment(appointment: Appointment) {
        appointmentsList.add(appointment)
    }

    override suspend fun updateAppointment(appointment: Appointment, token: String) {
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

    override suspend fun updateUserPermissions(token: String, userId: String, permissions: Set<UserPermission>): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        users.removeAll { it.id == userId }
        return Result.success(Unit)
    }
}

class FakeAddressRepository : AddressRepository {
    private val addresses = mutableListOf<Address>()

    override suspend fun getAddresses(userId: String): List<Address> {
        return addresses.filter { it.userId == userId }
    }

    override suspend fun saveAddress(address: Address): Result<String> {
        addresses.add(address)
        return Result.success(address.id)
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> {
        addresses.removeAll { it.id == addressId }
        return Result.success(Unit)
    }
}

class FakeStoreRepository : StoreRepository {
    private val stores = mutableListOf<Store>()

    override suspend fun getStore(id: String): Result<Store> {
        return stores.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(Exception("Store not found"))
    }

    override suspend fun saveStore(store: Store, token: String): Result<Unit> {
        stores.add(store)
        return Result.success(Unit)
    }

    override suspend fun createConnectAccount(storeId: String, email: String, token: String): Result<String> {
        return Result.success("acct_mock_123")
    }

    override suspend fun getConnectOnboardingLink(storeId: String, token: String): Result<String> {
        return Result.success("https://connect.stripe.com/setup/s/mock")
    }

    override suspend fun getConnectLoginLink(storeId: String, token: String): Result<String> {
        return Result.success("https://connect.stripe.com/express/mock")
    }

    override suspend fun getAccountSession(storeId: String, token: String): Result<String> {
        return Result.success("account_session_secret_mock")
    }
}

class FakeShippingRepository : ShippingRepository {
    override suspend fun calculateShipping(storeId: String, zipCode: String): Result<List<ShippingOption>> {
        return Result.success(listOf(ShippingOption("1", "Sedex", 15.0, 2)))
    }
}
