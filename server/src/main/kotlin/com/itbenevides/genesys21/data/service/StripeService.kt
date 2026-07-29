package com.itbenevides.genesys21.data.service

import com.stripe.Stripe
import com.stripe.StripeClient
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.itbenevides.genesys21.domain.model.Order
import com.stripe.net.RequestOptions

class StripeService {

    fun createCheckoutSession(
        order: Order,
        secretKey: String,
        successUrl: String,
        cancelUrl: String,
        connectedAccountId: String? = null
    ): String {
        val client = StripeClient(secretKey)

        val paramsBuilder = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(successUrl)
            .setCancelUrl(cancelUrl)
            .setClientReferenceId(order.id)
            .setCustomerEmail(if (order.customerId?.contains("@") == true) order.customerId else null)

        // Se houver uma conta conectada (Direct Charges), a cobrança vai para ela
        val requestOptions = if (!connectedAccountId.isNullOrBlank()) {
            RequestOptions.builder()
                .setStripeAccount(connectedAccountId)
                .build()
        } else {
            null
        }

        order.items.forEach { item ->
            paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(item.quantity.toLong())
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("brl")
                            .setUnitAmount(Math.round(item.price * 100)) // Stripe em centavos (arredondado)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.name)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
        }

        // Adiciona frete se houver
        if (order.shippingPrice > 0) {
            paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("brl")
                            .setUnitAmount(Math.round(order.shippingPrice * 100))
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Entrega / Frete")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
        }

        // Adiciona taxas de deslocamento se houver (Agrupadas como um item de serviço)
        val totalTravelFees = order.items.sumOf { it.appointment?.travelFee ?: 0.0 }
        if (totalTravelFees > 0) {
             paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("brl")
                            .setUnitAmount(Math.round(totalTravelFees * 100))
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Taxas de Deslocamento")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
        }

        val session = if (requestOptions != null) {
            client.v1().checkout().sessions().create(paramsBuilder.build(), requestOptions)
        } else {
            client.v1().checkout().sessions().create(paramsBuilder.build())
        }

        return session.url
    }
}
