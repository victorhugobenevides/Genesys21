package com.itbenevides.genesys21.data.service

import com.itbenevides.genesys21.data.database.AuditLogsTable
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.util.PrivacyUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.deleteWhere
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
        // LGPD: Anonimiza IP para logs comuns de auditoria
        // Mantemos o IP completo apenas se for uma ação crítica de segurança
        val effectiveIp = if (action.contains("SECURITY_") || action.contains("AUTH_FAILURE")) {
            ipAddress
        } else {
            PrivacyUtils.anonymizeIp(ipAddress)
        }

        dbQuery {
            AuditLogsTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[this.userId] = userId
                it[this.storeId] = storeId
                it[this.action] = action
                it[this.entityName] = entityName
                it[this.entityId] = entityId
                // LGPD: Sanitiza dados sensíveis nos detalhes
                it[this.details] = PrivacyUtils.sanitizeData(details)
                it[this.ipAddress] = effectiveIp
                it[this.createdAt] = System.currentTimeMillis()
                it[this.updatedAt] = System.currentTimeMillis()
            }
        }
    }

    /**
     * LGPD: Política de retenção de dados.
     * Remove logs de auditoria mais antigos que o período especificado.
     */
    suspend fun cleanupOldLogs(months: Int = 12) {
        val cutoffTime = System.currentTimeMillis() - (months * 30L * 24L * 60L * 60L * 1000L)
        dbQuery {
            AuditLogsTable.deleteWhere { createdAt less cutoffTime }
        }
    }
}
