package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.WasmCartRepository
import com.itbenevides.genesys21.domain.repository.CartRepository

actual fun getCartRepository(): CartRepository = WasmCartRepository()
