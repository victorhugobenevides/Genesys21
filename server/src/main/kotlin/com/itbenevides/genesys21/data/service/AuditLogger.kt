package com.itbenevides.genesys21.data.service

import com.itbenevides.genesys21.data.database.AuditLogsTable
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.insert
import java.util.UUID

object AuditLogger {

    suspend fun log(
        userId: String?,
        storeId: String?,
        action: String,
        entityName: String,
        entityId: String,
        details: String? = null,
        ipAddress: String? = null
    ) {
        dbQuery {
            AuditLogsTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[this.userId] = userId
                it[this.storeId] = storeId
                it[this.action] = action
                it[this.entityName] = entityName
                it[this.entityId] = entityId
                it[this.details] = details
                it[this.ipAddress] = ipAddress
                it[this.createdAt] = System.currentTimeMillis()
                it[this.updatedAt] = System.currentTimeMillis()
            }
        }
    }
}
