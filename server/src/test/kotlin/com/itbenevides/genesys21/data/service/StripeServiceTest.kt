package com.itbenevides.genesys21.data.service

import com.itbenevides.genesys21.domain.model.*
import com.stripe.StripeClient
import com.stripe.model.PaymentIntent
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.PaymentIntentCreateParams
import io.mockk.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StripeServiceTest {

    private lateinit var stripeService: StripeService
    private val mockClient = mockk<StripeClient>(relaxed = true)

    @BeforeTest
    fun setup() {
        stripeService = StripeService { mockClient }
    }

    @Test
    fun `createCheckoutSession should call stripe and return url`() {
        val order = Order(id = "o1", storeId = "s1", items = emptyList(), total = 100.0)
        val mockSession = mockk<Session>()
        every { mockSession.url } returns "https://stripe.com/pay"

        every { mockClient.v1().checkout().sessions().create(any<SessionCreateParams>()) } returns mockSession

        val url = stripeService.createCheckoutSession(order, "key", "success", "cancel")

        assertEquals("https://stripe.com/pay", url)
        verify { mockClient.v1().checkout().sessions().create(any<SessionCreateParams>()) }
    }

    @Test
    fun `createPaymentIntent should call stripe and return clientSecret`() {
        val order = Order(id = "o1", storeId = "s1", items = emptyList(), total = 100.0)
        val mockIntent = mockk<PaymentIntent>()
        every { mockIntent.clientSecret } returns "pi_secret_123"

        every { mockClient.v1().paymentIntents().create(any<PaymentIntentCreateParams>(), any()) } returns mockIntent

        val secret = stripeService.createPaymentIntent(order, "key")

        assertEquals("pi_secret_123", secret)
        verify { mockClient.v1().paymentIntents().create(any<PaymentIntentCreateParams>(), any()) }
    }
}
