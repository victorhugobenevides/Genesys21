package com.itbenevides.genesys21.data.service

import com.stripe.Stripe
import com.stripe.StripeClient
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.PaymentIntentCreateParams
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.util.CurrencyUtils
import com.stripe.net.RequestOptions

class StripeService(private val clientProvider: (String) -> StripeClient = { StripeClient(it) }) {

    fun createCheckoutSession(
        order: Order,
        secretKey: String,
        successUrl: String,
        cancelUrl: String,
        connectedAccountId: String? = null
    ): String {
        val client = clientProvider(secretKey)

        val paramsBuilder = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(successUrl)
            .setCancelUrl(cancelUrl)
            .setClientReferenceId(order.id)
            .setCustomerEmail(if (order.customerEmail?.contains("@") == true) order.customerEmail else null)

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
                            .setUnitAmount(CurrencyUtils.toStripeCents(item.price))
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

        if (order.shippingPrice > 0) {
            paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("brl")
                            .setUnitAmount(CurrencyUtils.toStripeCents(order.shippingPrice))
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

        val totalTravelFees = order.items.sumOf { it.appointment?.travelFee ?: 0.0 }
        if (totalTravelFees > 0) {
             paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("brl")
                            .setUnitAmount(CurrencyUtils.toStripeCents(totalTravelFees))
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

    fun createPaymentIntent(
        order: Order,
        secretKey: String,
        connectedAccountId: String? = null
    ): String {
        val client = clientProvider(secretKey)

        val totalInCents = CurrencyUtils.toStripeCents(order.total + order.shippingPrice)

        val paramsBuilder = PaymentIntentCreateParams.builder()
            .setAmount(totalInCents)
            .setCurrency("brl")
            .putMetadata("order_id", order.id)
            .putMetadata("store_id", order.storeId)
            .setReceiptEmail(if (order.customerEmail?.contains("@") == true) order.customerEmail else null)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )

        val requestOptions = RequestOptions.builder().apply {
            if (!connectedAccountId.isNullOrBlank()) {
                setStripeAccount(connectedAccountId)
            }
            // IDEMPOTÊNCIA: Garante que o mesmo pedido não gere duas cobranças
            setIdempotencyKey("payment_intent_${order.id}")
        }.build()

        val paymentIntent = client.v1().paymentIntents().create(paramsBuilder.build(), requestOptions)

        return paymentIntent.clientSecret
    }
}
