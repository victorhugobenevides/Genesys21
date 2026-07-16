package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.UserProfile
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.model.UserStatus

interface UserRepository {
    suspend fun getUserProfile(id: String): Result<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit>
    suspend fun getAllUsers(token: String): Result<List<UserProfile>>
    suspend fun updateUserRole(token: String, userId: String, role: UserRole): Result<Unit>
    suspend fun updateUserStatus(token: String, userId: String, status: UserStatus): Result<Unit>
}
