package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.domain.model.UserRole
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object Seeder {
    fun seedInitialData() {
        transaction {
            // 1. Create SuperAdmin
            val adminId = "victorkoto-uid"
            UsersTable.insert {
                it[id] = adminId
                it[email] = "victorkoto@gmail.com"
                it[name] = "Victor Koto"
                it[role] = UserRole.SUPERADMIN.name
            }

            // 2. Create Default Store
            val storeId = UUID.randomUUID().toString()
            StoresTable.insert {
                it[id] = storeId
                it[ownerId] = adminId
                it[name] = "Genesys Store"
                it[description] = "The official Genesys21 store."
            }

            println("Seeder: Initial data seeded successfully.")
        }
    }
}
