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

    companion object {
        const val OWNER_EMAIL = "victorkoto@gmail.com"
    }

    private object DogmaUtils {
        fun isDogmaAdmin(email: String): Boolean =
            email.lowercase().trim() == OWNER_EMAIL

        fun forceDogmaRole(role: UserRole, email: String): UserRole =
            if (isDogmaAdmin(email)) UserRole.SUPERADMIN else role

        fun forceDogmaPermissions(permissions: Set<com.itbenevides.genesys21.domain.model.UserPermission>, email: String, role: UserRole): Set<com.itbenevides.genesys21.domain.model.UserPermission> {
            return if (isDogmaAdmin(email) || (permissions.isEmpty() && (role == UserRole.MERCHANT || role == UserRole.ADMIN || role == UserRole.SUPERADMIN))) {
                com.itbenevides.genesys21.domain.model.UserPermission.entries.toSet()
            } else permissions
        }
    }

    private fun ResultRow.toUserProfile(): UserProfile {
        val rawEmail = this[UsersTable.email]
        val email = rawEmail.lowercase().trim()

        // DOGMA ABSOLUTO: victorkoto@gmail.com é o dono do sistema.
        // Ignoramos o valor do banco e forçamos o cargo aqui para garantir acesso total.
        if (email == OWNER_EMAIL) {
            println("DOGMA: Identificado proprietário $email. Forçando cargo SUPERADMIN em memória.")
            return UserProfile(
                id = this[UsersTable.id],
                email = rawEmail,
                name = this[UsersTable.name],
                avatarUrl = this[UsersTable.avatarUrl],
                phone = this[UsersTable.phone],
                role = UserRole.SUPERADMIN,
                status = UserStatus.APPROVED,
                permissions = com.itbenevides.genesys21.domain.model.UserPermission.entries.toSet(),
                createdAt = this[UsersTable.createdAt],
                updatedAt = this[UsersTable.updatedAt],
                deletedAt = this[UsersTable.deletedAt]
            )
        }

        val roleStr = this[UsersTable.role]
        val baseRole = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.CUSTOMER
        }

        val role = DogmaUtils.forceDogmaRole(baseRole, email)
        val status = try { UserStatus.valueOf(this[UsersTable.status]) } catch (e: Exception) { UserStatus.APPROVED }

        val permissionsRaw = this[UsersTable.permissions].split(",")
            .filter { it.isNotBlank() }
            .mapNotNull {
                runCatching { com.itbenevides.genesys21.domain.model.UserPermission.valueOf(it) }.getOrNull()
            }.toSet()

        val permissions = DogmaUtils.forceDogmaPermissions(permissionsRaw, email, role)

        return UserProfile(
            id = this[UsersTable.id],
            email = rawEmail,
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
            val userRow = UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()

            if (userRow != null) {
                val profile = userRow.toUserProfile()

                // AUTO-REPARO: Se for o admin principal mas o banco estiver desatualizado, corrigimos na hora
                if (DogmaUtils.isDogmaAdmin(profile.email) && profile.role != UserRole.SUPERADMIN) {
                    println("REPOSITORY: Detectado Admin Dogma com cargo incorreto. Reparando...")
                    UsersTable.update({ UsersTable.id eq id }) {
                        it[role] = UserRole.SUPERADMIN.name
                        it[permissions] = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { it.name }
                    }
                    // Retorna o perfil já corrigido
                    Result.success(profile.copy(
                        role = UserRole.SUPERADMIN,
                        permissions = com.itbenevides.genesys21.domain.model.UserPermission.entries.toSet()
                    ))
                } else {
                    Result.success(profile)
                }
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
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
                val isDogmaAdmin = DogmaUtils.isDogmaAdmin(email)

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
            val userRow = UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
            val userEmail = userRow?.get(UsersTable.email) ?: ""
            val finalRole = DogmaUtils.forceDogmaRole(role, userEmail)

            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.role] = finalRole.name
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
            val userRow = UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
            val userEmail = userRow?.get(UsersTable.email) ?: ""
            val userRoleStr = userRow?.get(UsersTable.role) ?: UserRole.CUSTOMER.name
            val userRole = try { UserRole.valueOf(userRoleStr) } catch(e: Exception) { UserRole.CUSTOMER }

            val finalPermissions = DogmaUtils.forceDogmaPermissions(permissions, userEmail, userRole)
            val permsStr = finalPermissions.joinToString(",") { it.name }

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
