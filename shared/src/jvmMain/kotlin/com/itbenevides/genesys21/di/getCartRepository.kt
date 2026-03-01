package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.InMemoryCartRepository
import com.itbenevides.genesys21.domain.repository.CartRepository

actual fun getCartRepository(): CartRepository = InMemoryCartRepository()
