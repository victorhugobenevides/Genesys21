package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.AndroidCartRepository
import com.itbenevides.genesys21.domain.repository.CartRepository

actual fun getCartRepository(): CartRepository = AndroidCartRepository()
