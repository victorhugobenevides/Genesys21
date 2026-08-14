package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.AuditLogsTable
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
        permissions = this[UsersTable.permissions].split(",")
            .filter { it.isNotBlank() }
            .mapNotNull {
                runCatching { com.itbenevides.genesys21.domain.model.UserPermission.valueOf(it) }.getOrNull()
            }.toSet(),
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
        println("REPOSITORY ERROR (getUserProfile): ${e.message}")
        Result.failure(e)
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        if (profile.email.isBlank()) {
            return Result.failure(Exception("E-mail inválido para o perfil"))
        }

        return try {
            dbQuery {
                // LGPD: Mascaramos o e-mail no log do servidor
                val maskedEmail = com.itbenevides.genesys21.util.PrivacyUtils.maskEmail(profile.email)
                println("REPOSITORY: Salvando perfil de usuário $maskedEmail (${profile.id})")
                val exists = UsersTable.selectAll().where { UsersTable.id eq profile.id }.count() > 0
                if (exists) {
                    UsersTable.update({ UsersTable.id eq profile.id }) {
                        it[name] = profile.name
                        it[email] = profile.email
                        it[avatarUrl] = profile.avatarUrl
                        it[phone] = profile.phone
                        it[updatedAt] = System.currentTimeMillis()
                        it[permissions] = profile.permissions.joinToString(",") { p -> p.name }
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
                        it[permissions] = profile.permissions.joinToString(",") { p -> p.name }
                    }
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            println("REPOSITORY ERROR (saveUserProfile): ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(token: String): Result<List<UserProfile>> = try {
        dbQuery {
            val list = UsersTable.selectAll().map { it.toUserProfile() }
            println("REPOSITORY: Listagem SuperAdmin. Retornando ${list.size} usuários.")
            Result.success(list)
        }
    } catch (e: Exception) {
        println("REPOSITORY ERROR (getAllUsers): ${e.message}")
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

    override suspend fun updateUserPermissions(token: String, userId: String, permissions: Set<com.itbenevides.genesys21.domain.model.UserPermission>): Result<Unit> = try {
        dbQuery {
            val permsStr = permissions.joinToString(",") { it.name }
            val updated = UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.permissions] = permsStr
            }
            if (updated > 0) {
                com.itbenevides.genesys21.data.service.AuditLogger.log(
                    userId = token,
                    storeId = null,
                    action = "UPDATE_PERMISSIONS",
                    entityName = "User",
                    entityId = userId,
                    details = "Permissões alteradas: $permsStr"
                )
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteUser(userId: String): Result<Unit> = try {
        dbQuery {
            // LGPD: Anonimização de dados pessoais em logs de auditoria vinculados
            AuditLogsTable.update({ AuditLogsTable.userId eq userId }) {
                it[this.userId] = null
                it[details] = "User data deleted (LGPD)"
            }

            // Hard delete do usuário
            UsersTable.deleteWhere { id eq userId }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
