package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.PagesTable
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.repository.PageRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqlitePageRepository : PageRepository {

    override suspend fun getPages(token: String): List<Page> = dbQuery {
        val query = if (token.isBlank()) {
            PagesTable.selectAll()
        } else {
            PagesTable.selectAll().where { PagesTable.ownerId eq token }
        }
        query.map { it.toPage() }
    }

    override suspend fun getPublicPage(id: String): Result<Page> = try {
        dbQuery {
            PagesTable.selectAll().where { PagesTable.id eq id }
                .map { it.toPage() }
                .singleOrNull()?.let { Result.success(it) } 
                ?: Result.failure(Exception("Página não encontrada"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> = try {
        dbQuery {
            val searchDomain = domain.lowercase().removePrefix("www.")
            PagesTable.selectAll().where { 
                (PagesTable.customDomain.lowerCase() eq searchDomain) or 
                (PagesTable.customDomain.lowerCase() eq "www.$searchDomain")
            }.map { it.toPage() }
             .firstOrNull()?.let { Result.success(it) }
             ?: Result.failure(Exception("Domínio $domain não vinculado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> = try {
        dbQuery {
            val formattedDomain = page.customDomain?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
            val formattedWhatsapp = page.whatsapp?.trim()?.takeIf { it.isNotBlank() }

            // 1. REPLICA DOMÍNIO E WHATSAPP PARA TODAS AS PÁGINAS DO USUÁRIO (CENTRALIZAÇÃO)
            if (token.isNotBlank()) {
                PagesTable.update({ PagesTable.ownerId eq token }) {
                    it[customDomain] = formattedDomain
                    it[whatsapp] = formattedWhatsapp
                }
            }

            val existingPage = PagesTable.selectAll().where { PagesTable.id eq page.id }.singleOrNull()
            
            if (existingPage != null) {
                val owner = existingPage[PagesTable.ownerId]
                if (owner != null && owner.isNotBlank() && owner != token) {
                    throw Exception("Acesso negado: Você não é o dono desta página")
                }

                PagesTable.update({ PagesTable.id eq page.id }) {
                    it[title] = page.title
                    it[ownerId] = token
                    it[theme] = page.theme.name
                    it[customDomain] = formattedDomain
                    it[whatsapp] = formattedWhatsapp
                    it[components] = page.components
                }
            } else {
                PagesTable.insert {
                    it[id] = page.id
                    it[title] = page.title
                    it[ownerId] = token
                    it[theme] = page.theme.name
                    it[customDomain] = formattedDomain
                    it[whatsapp] = formattedWhatsapp
                    it[components] = page.components
                }
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> = try {
        dbQuery {
            val deleted = PagesTable.deleteWhere { (PagesTable.id eq id) and (ownerId eq token) }
            if (deleted > 0) Result.success(Unit) else Result.failure(Exception("Falha ao excluir"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        return Result.failure(Exception("Use /upload"))
    }

    private fun ResultRow.toPage() = Page(
        id = this[PagesTable.id],
        title = this[PagesTable.title],
        ownerId = this[PagesTable.ownerId],
        customDomain = this[PagesTable.customDomain],
        whatsapp = this[PagesTable.whatsapp],
        components = this[PagesTable.components],
        theme = try { PageThemeConfig.valueOf(this[PagesTable.theme]) } catch (e: Exception) { PageThemeConfig.DEFAULT }
    )
}
