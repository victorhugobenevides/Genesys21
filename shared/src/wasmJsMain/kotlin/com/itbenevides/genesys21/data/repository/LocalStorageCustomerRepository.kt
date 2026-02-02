package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@JsFun("(key) => window.localStorage.getItem(key)")
private external fun jsGetItem(key: String): String?

@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun jsSetItem(key: String, value: String)

class LocalStorageCustomerRepository : CustomerRepository {
    private val _customerName = MutableStateFlow("")
    override val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    override val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    private val NAME_KEY = "genesys21_customer_name"
    private val PHONE_KEY = "genesys21_customer_phone"

    override suspend fun loadData() {
        _customerName.value = jsGetItem(NAME_KEY) ?: ""
        _customerPhone.value = jsGetItem(PHONE_KEY) ?: ""
    }

    override suspend fun loadName() {
        loadData()
    }

    override suspend fun saveName(name: String) {
        _customerName.value = name
        jsSetItem(NAME_KEY, name)
    }

    override suspend fun savePhone(phone: String) {
        _customerPhone.value = phone
        jsSetItem(PHONE_KEY, phone)
    }
}
