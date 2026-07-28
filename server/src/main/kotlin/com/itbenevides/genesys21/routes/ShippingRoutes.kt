package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.ShippingOption
import com.itbenevides.genesys21.domain.repository.StoreRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.shippingRoutes(storeRepository: StoreRepository) {
    route("/shipping") {
        get("/calculate") {
            val storeId = call.request.queryParameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "storeId missing")
            val destinationZip = call.request.queryParameters["zipCode"] ?: return@get call.respond(HttpStatusCode.BadRequest, "zipCode missing")

            val storeResult = storeRepository.getStore(storeId)
            val store = storeResult.getOrNull() ?: return@get call.respond(HttpStatusCode.NotFound, "Store not found")

            val originZip = store.originZipCode ?: "00000-000"

            // SIMULAÇÃO DE CÁLCULO
            val options = mutableListOf(
                ShippingOption("pac", "PAC (Correios)", 22.50, 7),
                ShippingOption("sedex", "SEDEX (Correios)", 48.90, 2),
                ShippingOption("transportadora", "Loggi / Jadlog", 18.00, 5)
            )

            // Cálculo dinâmico Uber/99 (Simulado baseado na distância/cidade - no mundo real usaria API Uber Direct)
            if (store.originCity == "São Paulo") {
                val uberPrice = 35.00 // Ex: Valor fixo ou calculado por distância
                options.add(ShippingOption("uber", "Uber Direct (Ida e Volta)", uberPrice * 2, 0))
                options.add(ShippingOption("99", "99 Entrega (Ida e Volta)", (uberPrice * 0.9) * 2, 0))
            }

            call.respond(options)
        }
    }
}
