package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object AddressesTable : BaseTable("addresses") {
    val id = varchar("id", 50) // UUID
    val userId = varchar("user_id", 100).references(UsersTable.id, onDelete = ReferenceOption.CASCADE).nullable()
    val street = varchar("street", 255)
    val number = varchar("number", 20)
    val complement = varchar("complement", 255).nullable()
    val neighborhood = varchar("neighborhood", 100)
    val city = varchar("city", 100)
    val state = varchar("state", 50)
    val zipCode = varchar("zip_code", 20)
    val isDefault = bool("is_default").default(false)

    override val primaryKey = PrimaryKey(id)
}
