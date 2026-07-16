package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.UserProfile
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.model.UserStatus
import com.itbenevides.genesys21.domain.repository.UserRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorUserRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) : UserRepository {

    override suspend fun getUserProfile(id: String): Result<UserProfile> = try {
        val response = client.get("$baseUrl/api/public/users/profile/$id")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Perfil não encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = try {
        val response = client.post("$baseUrl/api/users/profile") {
            contentType(ContentType.Application.Json)
            setBody(profile)
        }
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Erro ao salvar perfil"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAllUsers(token: String): Result<List<UserProfile>> = try {
        val response = client.get("$baseUrl/api/admin/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Erro ao buscar usuários"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserRole(token: String, userId: String, role: UserRole): Result<Unit> = try {
        val response = client.put("$baseUrl/api/admin/users/$userId/role") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("role", role.name)
        }
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Erro ao atualizar cargo"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserStatus(token: String, userId: String, status: UserStatus): Result<Unit> = try {
        val response = client.put("$baseUrl/api/admin/users/$userId/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("status", status.name)
        }
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Erro ao atualizar status"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
