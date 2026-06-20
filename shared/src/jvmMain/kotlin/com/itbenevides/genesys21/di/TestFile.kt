package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.data.repository.InMemoryCartRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

fun test(
    h: HttpClient,
    b: String,
    j: Json,
    a: AuthRepository,
) {
    InMemoryCartRepository(h, b, j, a)
}
