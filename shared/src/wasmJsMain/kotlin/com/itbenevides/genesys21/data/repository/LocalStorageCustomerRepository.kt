@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Funções Top-Level para interop com JS
@JsFun("() => localStorage.getItem('customer_name')")
private external fun getNameFromLocalStorage(): String?

@JsFun("() => localStorage.getItem('customer_phone')")
private external fun getPhoneFromLocalStorage(): String?

@JsFun("(name) => localStorage.setItem('customer_name', name)")
private external fun saveNameToLocalStorage(name: String)

@JsFun("(phone) => localStorage.setItem('customer_phone', phone)")
private external fun savePhoneToLocalStorage(phone: String)

class LocalStorageCustomerRepository : CustomerRepository {
    
    private val _customerName = MutableStateFlow("")
    override val customerName = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    override val customerPhone = _customerPhone.asStateFlow()
    
    override suspend fun loadData() {
        _customerName.value = getNameFromLocalStorage() ?: ""
        _customerPhone.value = getPhoneFromLocalStorage() ?: ""
    }
    
    override suspend fun loadName() {
        // Implementação mantida por compatibilidade, delega para loadData
        loadData()
    }

    override suspend fun saveName(name: String) {
        _customerName.value = name
        saveNameToLocalStorage(name)
    }

    override suspend fun savePhone(phone: String) {
        _customerPhone.value = phone
        savePhoneToLocalStorage(phone)
    }
}
