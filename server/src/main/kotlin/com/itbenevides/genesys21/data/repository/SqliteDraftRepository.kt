package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.DraftsTable
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.DraftRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteDraftRepository : DraftRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveDraft(page: Page, token: String): Result<Unit> = try {
        dbQuery {
            val exists = DraftsTable.selectAll()
                .where { (DraftsTable.ownerId eq token) and (DraftsTable.pageId eq page.id) }
                .count() > 0

            if (exists) {
                DraftsTable.update({ (DraftsTable.ownerId eq token) and (DraftsTable.pageId eq page.id) }) {
                    it[content] = json.encodeToString(page)
                    it[updatedAt] = System.currentTimeMillis()
                }
            } else {
                DraftsTable.insert {
                    it[id] = java.util.UUID.randomUUID().toString()
                    it[ownerId] = token
                    it[pageId] = page.id
                    it[content] = json.encodeToString(page)
                }
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDraft(pageId: String, token: String): Result<Page?> = try {
        dbQuery {
            val row = DraftsTable.selectAll()
                .where { (DraftsTable.ownerId eq token) and (DraftsTable.pageId eq pageId) }
                .singleOrNull()

            val page = row?.get(DraftsTable.content)?.let {
                json.decodeFromString<Page>(it)
            }
            Result.success(page)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteDraft(pageId: String, token: String): Result<Unit> = try {
        dbQuery {
            DraftsTable.deleteWhere { (ownerId eq token) and (DraftsTable.pageId eq pageId) }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
