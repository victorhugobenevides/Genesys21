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

    private fun ResultRow.toUserProfile(): UserProfile {
        val email = this[UsersTable.email]
        val isDogmaAdmin = email == "victorkoto@gmail.com"

        val roleStr = this[UsersTable.role]
        val baseRole = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            println("REPOSITORY: Falha ao converter role '$roleStr', assumindo CUSTOMER")
            UserRole.CUSTOMER
        }

        val role = if (isDogmaAdmin) UserRole.SUPERADMIN else baseRole

        val status = try { UserStatus.valueOf(this[UsersTable.status]) } catch (e: Exception) { UserStatus.APPROVED }

        val permissionsRaw = this[UsersTable.permissions].split(",")
            .filter { it.isNotBlank() }
            .mapNotNull {
                runCatching { com.itbenevides.genesys21.domain.model.UserPermission.valueOf(it) }.getOrNull()
            }.toSet()

        // BACKWARD COMPATIBILITY & DOGMA: Se for SuperAdmin ou as permissões estiverem vazias para cargos adm, atribui tudo
        val permissions = if (isDogmaAdmin || (permissionsRaw.isEmpty() && (role == UserRole.MERCHANT || role == UserRole.ADMIN || role == UserRole.SUPERADMIN))) {
            com.itbenevides.genesys21.domain.model.UserPermission.entries.toSet()
        } else permissionsRaw

        return UserProfile(
            id = this[UsersTable.id],
            email = email,
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
        println("REPOSITORY ERROR (getUserProfile): ${e.message}")
        Result.failure(e)
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        if (profile.email.isBlank()) {
            return Result.failure(Exception("E-mail inválido para o perfil"))
        }

        return try {
            dbQuery {
                val maskedEmail = com.itbenevides.genesys21.util.PrivacyUtils.maskEmail(profile.email)
                println("REPOSITORY: saveUserProfile para $maskedEmail. Role no DTO: ${profile.role}")

                // Verificação de Troca de UID
                val existingByEmail = UsersTable.selectAll().where { UsersTable.email eq profile.email }.singleOrNull()
                if (existingByEmail != null && existingByEmail[UsersTable.id] != profile.id) {
                    val oldId = existingByEmail[UsersTable.id]
                    println("REPOSITORY: Atualizando UID de $oldId para ${profile.id}")
                    UsersTable.update({ UsersTable.id eq oldId }) {
                        it[id] = profile.id
                        it[name] = profile.name
                        it[avatarUrl] = profile.avatarUrl
                        it[phone] = profile.phone
                        it[updatedAt] = System.currentTimeMillis()

                        // BLOQUEIO DE ROLE: Apenas Dogma Admin pode ter SUPERADMIN atribuído via profile save
                        if (profile.email == "victorkoto@gmail.com") {
                            it[role] = UserRole.SUPERADMIN.name
                            it[permissions] = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { p -> p.name }
                        }
                    }
                } else {
                    val exists = UsersTable.selectAll().where { UsersTable.id eq profile.id }.count() > 0
                    if (exists) {
                        println("REPOSITORY: Atualizando usuário existente ${profile.id}")
                        UsersTable.update({ UsersTable.id eq profile.id }) {
                            it[name] = profile.name
                            it[email] = profile.email
                            it[avatarUrl] = profile.avatarUrl
                            it[phone] = profile.phone
                            it[updatedAt] = System.currentTimeMillis()

                            // SEGURANÇA: Bloqueia escalada de privilégios.
                            // O campo 'role' do DTO é IGNOERADO para usuários comuns.
                            if (profile.email == "victorkoto@gmail.com") {
                                it[role] = UserRole.SUPERADMIN.name
                                it[permissions] = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { p -> p.name }
                            }
                        }
                    } else {
                        println("REPOSITORY: Inserindo novo usuário ${profile.id}")
                        UsersTable.insert {
                            it[id] = profile.id
                            it[name] = profile.name
                            it[email] = profile.email
                            it[avatarUrl] = profile.avatarUrl
                            it[phone] = profile.phone

                            // SEGURANÇA: Novos usuários são SEMPRE CUSTOMER por padrão.
                            if (profile.email == "victorkoto@gmail.com") {
                                it[role] = UserRole.SUPERADMIN.name
                                it[permissions] = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { p -> p.name }
                            } else {
                                it[role] = UserRole.CUSTOMER.name
                                it[permissions] = ""
                            }

                            it[status] = profile.status.name
                            it[createdAt] = System.currentTimeMillis()
                            it[updatedAt] = System.currentTimeMillis()
                        }
                    }
                }

                com.itbenevides.genesys21.data.service.AuditLogger.log(
                    userId = profile.id,
                    storeId = null,
                    action = "UPDATE_PROFILE",
                    entityName = "User",
                    entityId = profile.id
                )
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
