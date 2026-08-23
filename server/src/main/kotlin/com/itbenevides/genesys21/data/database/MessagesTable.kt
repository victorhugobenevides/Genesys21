package com.itbenevides.genesys21.data.database

object MessagesTable : BaseTable("messages") {
    val id = varchar("id", 50) // UUID
    val refId = varchar("ref_id", 50).index() // Order ID or Appointment ID
    val senderNick = varchar("sender_nick", 100)
    val content = text("content")
    val isFromMerchant = bool("is_from_merchant").default(false)

    override val primaryKey = PrimaryKey(id)
}
