package com.itbenevides.genesys21.data.database

object UsersTable : BaseTable("users") {
    val id = varchar("id", 100) // Firebase UID
    val email = varchar("email", 200).uniqueIndex()
    val name = varchar("name", 200)
    val avatarUrl = text("avatar_url").nullable()
    val phone = varchar("phone", 50).nullable()
    val role = varchar("role", 50).default("CUSTOMER")
    val status = varchar("status", 50).default("APPROVED")

    override val primaryKey = PrimaryKey(id)
}

object StoresTable : BaseTable("stores") {
    val id = varchar("id", 50) // UUID
    val ownerId = varchar("owner_id", 100).references(UsersTable.id)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val logoUrl = text("logo_url").nullable()
    val whatsapp = varchar("whatsapp", 50).nullable()
    val customDomain = varchar("custom_domain", 255).nullable().uniqueIndex("idx_stores_custom_domain")
    val theme = varchar("theme", 50).default("ROYAL")

    override val primaryKey = PrimaryKey(id)
}
