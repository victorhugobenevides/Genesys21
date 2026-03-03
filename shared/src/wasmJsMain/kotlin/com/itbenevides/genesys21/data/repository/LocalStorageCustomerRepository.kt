package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@JsFun("() => localStorage.getItem('customer_name')")
private external fun getNameFromLocalStorage(): String?

@JsFun("(name) => localStorage.setItem('customer_name', name)")
private external fun setNameInLocalStorage(name: String)

@JsFun("() => localStorage.getItem('customer_phone')")
private external fun getPhoneFromLocalStorage(): String?

@JsFun("(phone) => localStorage.setItem('customer_phone', phone)")
private external fun setPhoneInLocalStorage(phone: String)

class WasmCustomerRepository : CustomerRepository {
    private val _customerName = MutableStateFlow("")
    override val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    override val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    override suspend fun saveName(name: String) {
        setNameInLocalStorage(name)
        _customerName.value = name
    }

    override suspend fun savePhone(phone: String) {
        setPhoneInLocalStorage(phone)
        _customerPhone.value = phone
    }

    override suspend fun loadData() {
        _customerName.value = getNameFromLocalStorage() ?: ""
        _customerPhone.value = getPhoneFromLocalStorage() ?: ""
    }

    override suspend fun loadName() {
        _customerName.value = getNameFromLocalStorage() ?: ""
    }
}
