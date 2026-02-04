package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryCustomerRepository : CustomerRepository {
    private val _customerName = MutableStateFlow("")
    override val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    override val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    override suspend fun saveName(name: String) {
        _customerName.value = name
    }

    override suspend fun savePhone(phone: String) {
        _customerPhone.value = phone
    }

    override suspend fun loadData() {
        // Implementação em memória, nada a carregar de persistência externa
    }

    override suspend fun loadName() {
        // Mantido por compatibilidade com a interface
    }
}
