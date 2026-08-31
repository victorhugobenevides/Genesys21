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

    private fun ResultRow.toUserProfile(): UserProfile {
        val rawEmail = this[UsersTable.email]
        val email = rawEmail.lowercase().trim()
        val isDogmaAdmin = email == "victorkoto@gmail.com"

        val roleStr = this[UsersTable.role]
        val baseRole = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.CUSTOMER
        }

        // DOGMA: Força SUPERADMIN se o e-mail for o do proprietário, ignorando o que está no banco
        val role = if (isDogmaAdmin) UserRole.SUPERADMIN else baseRole

        val status = try { UserStatus.valueOf(this[UsersTable.status]) } catch (e: Exception) { UserStatus.APPROVED }

        val permissionsRaw = this[UsersTable.permissions].split(",")
            .filter { it.isNotBlank() }
            .mapNotNull {
                runCatching { com.itbenevides.genesys21.domain.model.UserPermission.valueOf(it) }.getOrNull()
            }.toSet()

        // DOGMA: Se for SuperAdmin, garante todas as permissões
        val permissions = if (isDogmaAdmin || (permissionsRaw.isEmpty() && (role == UserRole.MERCHANT || role == UserRole.ADMIN || role == UserRole.SUPERADMIN))) {
            com.itbenevides.genesys21.domain.model.UserPermission.entries.toSet()
        } else permissionsRaw

        return UserProfile(
            id = this[UsersTable.id],
            email = rawEmail, // Mantém o e-mail original da tabela para o DTO
            name = this[UsersTable.name],
            avatarUrl = this[UsersTable.avatarUrl],
            phone = this[UsersTable.phone],
            role = role,
            status = status,
            permissions = permissions,
            createdAt = this[UsersTable.createdAt],
            updatedAt = this[UsersTable.updatedAt],
            deletedAt = this[UsersTable.deletedAt]
        )
    }

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

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val email = profile.email.lowercase().trim()
        if (email.isBlank()) {
            return Result.failure(Exception("E-mail inválido"))
        }

        return try {
            dbQuery {
                val exists = UsersTable.selectAll().where { UsersTable.id eq profile.id }.count() > 0
                val isDogmaAdmin = email == "victorkoto@gmail.com"

                if (exists) {
                    // UPDATE: NUNCA atualizamos o Role ou Permissões por esta rota pública (Mass Assignment).
                    println("REPOSITORY: Atualizando perfil público do usuário ${profile.id}. Cargo será PRESERVADO.")

                    UsersTable.update({ UsersTable.id eq profile.id }) {
                        it[name] = profile.name
                        it[UsersTable.email] = profile.email
                        it[avatarUrl] = profile.avatarUrl
                        it[phone] = profile.phone
                        it[updatedAt] = System.currentTimeMillis()

                        // EXCEÇÃO: Apenas o admin dogma pode forçar cargo no update se necessário
                        if (isDogmaAdmin) {
                            it[role] = UserRole.SUPERADMIN.name
                            it[permissions] = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { perm -> perm.name }
                        }
                    }
                } else {
                    // INSERT: Novos usuários sempre CUSTOMER (exceto Dogma)
                    println("REPOSITORY: Inserindo novo usuário ${profile.id}. Forçando cargo inicial.")
                    UsersTable.insert {
                        it[id] = profile.id
                        it[name] = profile.name
                        it[UsersTable.email] = profile.email
                        it[avatarUrl] = profile.avatarUrl
                        it[phone] = profile.phone
                        it[createdAt] = System.currentTimeMillis()
                        it[updatedAt] = System.currentTimeMillis()

                        if (isDogmaAdmin) {
                            it[role] = UserRole.SUPERADMIN.name
                            it[permissions] = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { perm -> perm.name }
                        } else {
                            it[role] = UserRole.CUSTOMER.name
                            it[permissions] = ""
                        }
                    }
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(token: String): Result<List<UserProfile>> = try {
        dbQuery {
            Result.success(UsersTable.selectAll().map { it.toUserProfile() })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserRole(token: String, userId: String, role: UserRole): Result<Unit> = try {
        dbQuery {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.role] = role.name
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserStatus(token: String, userId: String, status: UserStatus): Result<Unit> = try {
        dbQuery {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.status] = status.name
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserPermissions(token: String, userId: String, permissions: Set<com.itbenevides.genesys21.domain.model.UserPermission>): Result<Unit> = try {
        dbQuery {
            val permsStr = permissions.joinToString(",") { it.name }
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.permissions] = permsStr
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteUser(userId: String): Result<Unit> = try {
        dbQuery {
            UsersTable.deleteWhere { id eq userId }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
