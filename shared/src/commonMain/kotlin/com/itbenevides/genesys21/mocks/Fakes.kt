package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCartRepository : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    // Propriedade para controlar o SessionId em testes
    private var fakeSessionId: String = "fake-session"

    fun setSessionId(id: String) {
        fakeSessionId = id
    }

    override suspend fun addToCart(item: CartItem): Result<Unit> {
        val current = _cartItems.value.toMutableList()
        current.add(item)
        _cartItems.value = current
        return Result.success(Unit)
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
        return Result.success(Unit)
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> {
        _cartItems.value = _cartItems.value.map { 
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
    override fun getSessionId(): String = fakeSessionId
}

class FakeCustomerRepository : CustomerRepository {
    private val _customerName = MutableStateFlow("")
    override val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    override val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    override suspend fun saveName(name: String) { _customerName.value = name }
    override suspend fun savePhone(phone: String) { _customerPhone.value = phone }
    override suspend fun loadData() {}
    override suspend fun loadName() {}
}

class FakePageDraftRepository : PageDraftRepository {
    private val drafts = mutableMapOf<String, Page>()
    override fun saveDraft(page: Page) { drafts[page.id] = page }
    override fun getDraft(pageId: String): Page? = drafts[pageId]
    override fun clearDraft(pageId: String) { drafts.remove(pageId) }
}

class FakeOrderRepository : OrderRepository {
    private val orders = mutableListOf<Order>()

    override suspend fun createMercadoPagoCheckout(order: Order, token: String): Result<String> = Result.success("http://fake-mp.com")
    override fun getOrders(token: String) = kotlinx.coroutines.flow.flowOf(orders.toList())
    
    override suspend fun getOrderById(orderId: String): Result<Order> {
        val order = orders.find { it.id == orderId }
        return if (order != null) Result.success(order) else Result.failure(Exception("Not found"))
    }
    
    override suspend fun getCustomerOrders(sessionId: String): Result<List<Order>> {
        // Simula o backend filtrando por sessionId
        return Result.success(orders.filter { it.customerId == sessionId })
    }
    
    override suspend fun createOrder(order: Order): Result<String> {
        orders.add(order)
        return Result.success(order.id)
    }
    
    override suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit> {
        val index = orders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            orders[index] = orders[index].copy(status = status)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Order not found"))
    }
}
