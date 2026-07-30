package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.UsersTable
import com.itbenevides.genesys21.domain.model.UserProfile
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.model.UserStatus
import com.itbenevides.genesys21.domain.repository.UserRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteUserRepository : UserRepository {

    private fun ResultRow.toUserProfile() = UserProfile(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        name = this[UsersTable.name],
        avatarUrl = this[UsersTable.avatarUrl],
        phone = this[UsersTable.phone],
        role = UserRole.valueOf(this[UsersTable.role]),
        status = UserStatus.valueOf(this[UsersTable.status]),
        createdAt = this[UsersTable.createdAt],
        updatedAt = this[UsersTable.updatedAt],
        deletedAt = this[UsersTable.deletedAt]
    )

    override suspend fun getUserProfile(id: String): Result<UserProfile> = try {
        dbQuery {
            UsersTable.selectAll().where { UsersTable.id eq id }
                .map { it.toUserProfile() }
                .singleOrNull()?.let { Result.success(it) }
                ?: Result.failure(Exception("Usuário não encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = try {
        dbQuery {
            val exists = UsersTable.selectAll().where { UsersTable.id eq profile.id }.count() > 0
            if (exists) {
                UsersTable.update({ UsersTable.id eq profile.id }) {
                    it[name] = profile.name
                    it[email] = profile.email
                    it[avatarUrl] = profile.avatarUrl
                    it[phone] = profile.phone
                    it[updatedAt] = System.currentTimeMillis()
                }
                com.itbenevides.genesys21.data.service.AuditLogger.log(
                    userId = profile.id,
                    storeId = null,
                    action = "UPDATE_PROFILE",
                    entityName = "User",
                    entityId = profile.id
                )
            } else {
                UsersTable.insert {
                    it[id] = profile.id
                    it[name] = profile.name
                    it[email] = profile.email
                    it[avatarUrl] = profile.avatarUrl
                    it[phone] = profile.phone
                    it[role] = profile.role.name
                    it[status] = profile.status.name
                }
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAllUsers(token: String): Result<List<UserProfile>> = try {
        // Validação de SuperAdmin deve ser feita no nível da Rota/UseCase,
        // mas aqui retornamos todos os usuários.
        dbQuery {
            Result.success(UsersTable.selectAll().map { it.toUserProfile() })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserRole(token: String, userId: String, role: UserRole): Result<Unit> = try {
        dbQuery {
            val updated = UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.role] = role.name
            }
            if (updated > 0) {
                com.itbenevides.genesys21.data.service.AuditLogger.log(
                    userId = token,
                    storeId = null,
                    action = "UPDATE_ROLE",
                    entityName = "User",
                    entityId = userId,
                    details = "Cargo alterado para ${role.name}"
                )
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserStatus(token: String, userId: String, status: UserStatus): Result<Unit> = try {
        dbQuery {
            val updated = UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.status] = status.name
            }
            if (updated > 0) {
                com.itbenevides.genesys21.data.service.AuditLogger.log(
                    userId = token,
                    storeId = null,
                    action = "UPDATE_STATUS",
                    entityName = "User",
                    entityId = userId,
                    details = "Status alterado para ${status.name}"
                )
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
