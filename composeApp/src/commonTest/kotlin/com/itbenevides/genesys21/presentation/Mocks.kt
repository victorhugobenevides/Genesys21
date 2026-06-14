package com.itbenevides.genesys21.presentation

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class FakeOrderRepository : com.itbenevides.genesys21.domain.repository.OrderRepository {
    override fun getOrders(token: String) = kotlinx.coroutines.flow.flowOf(emptyList<Order>())

    override suspend fun createOrder(order: Order) = Result.success(Unit)

    override suspend fun getCustomerOrders(sessionId: String) = Result.success(emptyList<Order>())

    override suspend fun getOrderById(orderId: String): Result<Order> = Result.failure(Exception("Not found"))

    override suspend fun updateOrderStatus(
        token: String,
        orderId: String,
        status: OrderStatus,
    ) = Result.success(Unit)
}
