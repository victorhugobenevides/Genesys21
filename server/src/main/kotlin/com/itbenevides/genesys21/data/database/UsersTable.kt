package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object UsersTable : BaseTable("users") {
    val id = varchar("id", 100) // Firebase UID
    val email = varchar("email", 200).uniqueIndex()
    val name = varchar("name", 200)
    val avatarUrl = text("avatar_url").nullable()
    val phone = varchar("phone", 50).nullable()
    val role = varchar("role", 50).default("CUSTOMER")
    val status = varchar("status", 50).default("APPROVED")
    val permissions = text("permissions").default("") // Separadas por vírgula

    override val primaryKey = PrimaryKey(id)
}

object StoresTable : BaseTable("stores") {
    val id = varchar("id", 50) // UUID
    val ownerId = varchar("owner_id", 100).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val logoUrl = text("logo_url").nullable()
    val whatsapp = varchar("whatsapp", 50).nullable()
    val originZipCode = varchar("origin_zip_code", 20).nullable()
    val originStreet = varchar("origin_street", 255).nullable()
    val originNumber = varchar("origin_number", 20).nullable()
    val originNeighborhood = varchar("origin_neighborhood", 100).nullable()
    val originCity = varchar("origin_city", 100).nullable()
    val originState = varchar("origin_state", 50).nullable()
    val allowPayOnLocation = bool("allow_pay_on_location").default(true)
    val allowPayInApp = bool("allow_pay_in_app").default(true)
    val allowPickup = bool("allow_pickup").default(true)
    val allowDelivery = bool("allow_delivery").default(true)
    val stripePublicKey = text("stripe_public_key").nullable()
    val stripeSecretKey = text("stripe_secret_key").nullable()
    val stripeAccountId = varchar("stripe_account_id", 100).nullable()
    val asaasApiKey = text("asaas_api_key").nullable()
    val paymentGateway = varchar("payment_gateway", 20).default("STRIPE")
    val customDomain = varchar("custom_domain", 255).nullable().uniqueIndex("idx_stores_custom_domain")
    val theme = varchar("theme", 50).default("ELEGANCE")

    override val primaryKey = PrimaryKey(id)
}
