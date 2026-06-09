package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.InMemoryCartRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import com.itbenevides.genesys21.domain.repository.AuthRepository

fun test(h: HttpClient, b: String, j: Json, a: AuthRepository) {
    InMemoryCartRepository(h, b, j, a)
}
