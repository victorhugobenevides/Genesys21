package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.StoresTable
import com.itbenevides.genesys21.domain.model.Store
import com.itbenevides.genesys21.domain.repository.StoreRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteStoreRepository : StoreRepository {

    override suspend fun getStore(id: String): Result<Store> = try {
        dbQuery {
            StoresTable.selectAll().where { StoresTable.id eq id }
                .map { it.toStore() }
                .singleOrNull()?.let { Result.success(it) }
                ?: Result.failure(Exception("Loja não encontrada"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveStore(store: Store, token: String): Result<Unit> = try {
        dbQuery {
            val exists = StoresTable.selectAll().where { StoresTable.id eq store.id }.count() > 0
            if (exists) {
                // Validação de posse
                val currentOwner = StoresTable.select(StoresTable.ownerId).where { StoresTable.id eq store.id }.single()[StoresTable.ownerId]
                if (currentOwner != token) throw Exception("Acesso negado")

                StoresTable.update({ StoresTable.id eq store.id }) {
                    it[name] = store.name
                    it[description] = store.description
                    it[logoUrl] = store.logoUrl
                    it[whatsapp] = store.whatsapp
                    it[originZipCode] = store.originZipCode
                    it[originStreet] = store.originStreet
                    it[originNumber] = store.originNumber
                    it[originNeighborhood] = store.originNeighborhood
                    it[originCity] = store.originCity
                    it[originState] = store.originState
                    it[allowPayOnLocation] = store.allowPayOnLocation
                    it[allowPayInApp] = store.allowPayInApp
                    it[allowPickup] = store.allowPickup
                    it[allowDelivery] = store.allowDelivery
                    it[stripePublicKey] = store.stripePublicKey
                    it[stripeSecretKey] = store.stripeSecretKey
                    it[stripeAccountId] = store.stripeAccountId
                    it[asaasApiKey] = store.asaasApiKey
                    it[paymentGateway] = store.paymentGateway
                    it[customDomain] = store.customDomain
                    it[updatedAt] = System.currentTimeMillis()
                }
            } else {
                StoresTable.insert {
                    it[id] = store.id
                    it[ownerId] = token
                    it[name] = store.name
                    it[description] = store.description
                    it[logoUrl] = store.logoUrl
                    it[whatsapp] = store.whatsapp
                    it[originZipCode] = store.originZipCode
                    it[originStreet] = store.originStreet
                    it[originNumber] = store.originNumber
                    it[originNeighborhood] = store.originNeighborhood
                    it[originCity] = store.originCity
                    it[originState] = store.originState
                    it[allowPayOnLocation] = store.allowPayOnLocation
                    it[allowPayInApp] = store.allowPayInApp
                    it[allowPickup] = store.allowPickup
                    it[allowDelivery] = store.allowDelivery
                    it[stripePublicKey] = store.stripePublicKey
                    it[stripeSecretKey] = store.stripeSecretKey
                    it[stripeAccountId] = store.stripeAccountId
                    it[asaasApiKey] = store.asaasApiKey
                    it[paymentGateway] = store.paymentGateway
                    it[customDomain] = store.customDomain
                }
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createConnectAccount(storeId: String, email: String, token: String): Result<String> {
        // Implementação delegada via rota/service no backend, mas o repo precisa do contrato
        return Result.failure(Exception("Operação deve ser realizada via API Admin"))
    }

    override suspend fun getConnectOnboardingLink(storeId: String, token: String): Result<String> {
        return Result.failure(Exception("Operação deve ser realizada via API Admin"))
    }

    override suspend fun getConnectLoginLink(storeId: String, token: String): Result<String> {
        return Result.failure(Exception("Operação deve ser realizada via API Admin"))
    }

    override suspend fun getAccountSession(storeId: String, token: String): Result<String> {
        return Result.failure(Exception("Operação deve ser realizada via API Admin"))
    }

    private fun ResultRow.toStore() = Store(
        id = this[StoresTable.id],
        ownerId = this[StoresTable.ownerId],
        name = this[StoresTable.name],
        description = this[StoresTable.description],
        logoUrl = this[StoresTable.logoUrl],
        whatsapp = this[StoresTable.whatsapp],
        originZipCode = this[StoresTable.originZipCode],
        originStreet = this[StoresTable.originStreet],
        originNumber = this[StoresTable.originNumber],
        originNeighborhood = this[StoresTable.originNeighborhood],
        originCity = this[StoresTable.originCity],
        originState = this[StoresTable.originState],
        allowPayOnLocation = this[StoresTable.allowPayOnLocation],
        allowPayInApp = this[StoresTable.allowPayInApp],
        allowPickup = this[StoresTable.allowPickup],
        allowDelivery = this[StoresTable.allowDelivery],
        stripePublicKey = this[StoresTable.stripePublicKey],
        stripeSecretKey = this[StoresTable.stripeSecretKey],
        stripeAccountId = this[StoresTable.stripeAccountId],
        asaasApiKey = this[StoresTable.asaasApiKey],
        paymentGateway = this[StoresTable.paymentGateway],
        customDomain = this[StoresTable.customDomain],
        createdAt = this[StoresTable.createdAt],
        updatedAt = this[StoresTable.updatedAt],
        deletedAt = this[StoresTable.deletedAt]
    )
}
