package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.DomainMappingsTable
import com.itbenevides.genesys21.domain.model.DomainMapping
import com.itbenevides.genesys21.domain.repository.DomainRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteDomainRepository : DomainRepository {

    override suspend fun getAllMappings(): Result<List<DomainMapping>> = try {
        dbQuery {
            val mappings = DomainMappingsTable.selectAll()
                .where { DomainMappingsTable.deletedAt.isNull() }
                .map { it.toDomainMapping() }
            Result.success(mappings)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getMappingByDomain(domain: String): Result<DomainMapping?> = try {
        dbQuery {
            val searchDomain = domain.lowercase().removePrefix("www.")
            val mapping = DomainMappingsTable.selectAll()
                .where {
                    (DomainMappingsTable.domain eq searchDomain) or
                    (DomainMappingsTable.domain eq "www.$searchDomain")
                }
                .firstOrNull()?.toDomainMapping()
            Result.success(mapping)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveMapping(mapping: DomainMapping): Result<Unit> = try {
        dbQuery {
            val exists = DomainMappingsTable.selectAll().where { DomainMappingsTable.id eq mapping.id }.count() > 0
            if (exists) {
                DomainMappingsTable.update({ DomainMappingsTable.id eq mapping.id }) {
                    it[domain] = mapping.domain.lowercase()
                    it[targetPageId] = mapping.targetPageId
                    it[updatedAt] = System.currentTimeMillis()
                }
            } else {
                DomainMappingsTable.insert {
                    it[id] = mapping.id.ifBlank { java.util.UUID.randomUUID().toString() }
                    it[domain] = mapping.domain.lowercase()
                    it[targetPageId] = mapping.targetPageId
                }
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteMapping(id: String): Result<Unit> = try {
        dbQuery {
            DomainMappingsTable.deleteWhere { DomainMappingsTable.id eq id }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun ResultRow.toDomainMapping() = DomainMapping(
        id = this[DomainMappingsTable.id],
        domain = this[DomainMappingsTable.domain],
        targetPageId = this[DomainMappingsTable.targetPageId],
        createdAt = this[DomainMappingsTable.createdAt],
        updatedAt = this[DomainMappingsTable.updatedAt]
    )
}
