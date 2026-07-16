package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = varchar("id", 100) // Firebase UID
    val email = varchar("email", 200).uniqueIndex()
    val name = varchar("name", 200)
    val role = varchar("role", 50).default("CUSTOMER")
    val status = varchar("status", 50).default("APPROVED")
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
