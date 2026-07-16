package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.UserProfile
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.model.UserStatus
import com.itbenevides.genesys21.domain.repository.UserRepository

class GetUserProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String) = repository.getUserProfile(id)
}

class SaveUserProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(profile: UserProfile) = repository.saveUserProfile(profile)
}

class GetAllUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(token: String) = repository.getAllUsers(token)
}

class UpdateUserRoleUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(token: String, userId: String, role: UserRole) =
        repository.updateUserRole(token, userId, role)
}

class UpdateUserStatusUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(token: String, userId: String, status: UserStatus) =
        repository.updateUserStatus(token, userId, status)
}
