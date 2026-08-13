package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object OrdersTable : BaseTable("orders") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.RESTRICT).index("idx_orders_store_id")
    val customerId = varchar("customer_id", 100).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable().index("idx_orders_customer_id")
    val sessionId = varchar("session_id", 100).nullable()
    val customerName = varchar("customer_name", 255).nullable()
    val customerPhone = varchar("customer_phone", 50).nullable()
    val total = double("total")
    val status = varchar("status", 50)
    val paymentMethod = varchar("payment_method", 50).default("LOCAL")
    val whatsappContact = varchar("whatsapp_contact", 50).nullable()
    val theme = varchar("theme", 50).default("ELEGANCE")

    // Entrega / Frete
    val shippingStreet = varchar("shipping_street", 255).nullable()
    val shippingNumber = varchar("shipping_number", 20).nullable()
    val shippingComplement = varchar("shipping_complement", 255).nullable()
    val shippingNeighborhood = varchar("shipping_neighborhood", 100).nullable()
    val shippingCity = varchar("shipping_city", 100).nullable()
    val shippingState = varchar("shipping_state", 50).nullable()
    val shippingZipCode = varchar("shipping_zip_code", 20).nullable()
    val shippingPrice = double("shipping_price").default(0.0)
    val shippingMethod = varchar("shipping_method", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}

object OrderItemsTable : BaseTable("order_items") {
    val id = varchar("id", 50) // UUID
    val orderId = varchar("order_id", 50).references(OrdersTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 50).references(ProductsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val serviceId = varchar("service_id", 50).references(BookingServicesTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val appointmentId = varchar("appointment_id", 50).nullable() // UUID for appointment if applicable
    val productName = varchar("product_name", 255) // Also used for Service Name
    val productPrice = double("product_price")
    val quantity = integer("quantity")

    override val primaryKey = PrimaryKey(id)
}
