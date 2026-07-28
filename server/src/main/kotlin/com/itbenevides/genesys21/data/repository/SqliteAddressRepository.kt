package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.Address
import com.itbenevides.genesys21.domain.repository.AddressRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteAddressRepository : AddressRepository {
    override suspend fun getAddresses(userId: String): List<Address> = dbQuery {
        AddressesTable.selectAll().where { AddressesTable.userId eq userId }
            .map { it.toAddress() }
    }

    override suspend fun saveAddress(address: Address): Result<String> = try {
        dbQuery {
            val finalId = address.id.ifBlank { java.util.UUID.randomUUID().toString() }
            val exists = AddressesTable.selectAll().where { AddressesTable.id eq finalId }.count() > 0

            if (exists) {
                AddressesTable.update({ AddressesTable.id eq finalId }) {
                    it[street] = address.street
                    it[number] = address.number
                    it[complement] = address.complement
                    it[neighborhood] = address.neighborhood
                    it[city] = address.city
                    it[state] = address.state
                    it[zipCode] = address.zipCode
                    it[isDefault] = address.isDefault
                    it[updatedAt] = System.currentTimeMillis()
                }
            } else {
                AddressesTable.insert {
                    it[id] = finalId
                    it[userId] = address.userId
                    it[street] = address.street
                    it[number] = address.number
                    it[complement] = address.complement
                    it[neighborhood] = address.neighborhood
                    it[city] = address.city
                    it[state] = address.state
                    it[zipCode] = address.zipCode
                    it[isDefault] = address.isDefault
                }
            }
            Result.success(finalId)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> = try {
        dbQuery {
            AddressesTable.deleteWhere { id eq addressId }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun ResultRow.toAddress() = Address(
        id = this[AddressesTable.id],
        userId = this[AddressesTable.userId],
        street = this[AddressesTable.street],
        number = this[AddressesTable.number],
        complement = this[AddressesTable.complement],
        neighborhood = this[AddressesTable.neighborhood],
        city = this[AddressesTable.city],
        state = this[AddressesTable.state],
        zipCode = this[AddressesTable.zipCode],
        isDefault = this[AddressesTable.isDefault]
    )
}
