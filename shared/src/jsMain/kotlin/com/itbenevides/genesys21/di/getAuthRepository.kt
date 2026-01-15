package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.JsAuthRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository

actual fun getAuthRepository(): AuthRepository = JsAuthRepository()
