package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.repository.StoreRepository
import com.itbenevides.genesys21.domain.repository.UserRepository
import com.stripe.Stripe
import com.stripe.model.Account
import com.stripe.model.AccountSession
import com.stripe.param.AccountCreateParams
import com.stripe.param.AccountSessionCreateParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ConnectAccountRequest(val storeId: String, val email: String)

@Serializable
data class AccountSessionRequest(val storeId: String)

@Serializable
data class ConnectLinkResponse(val url: String)

@Serializable
data class AccountSessionResponse(val clientSecret: String)

fun Route.connectRoutes(
    userRepository: UserRepository,
    storeRepository: StoreRepository
) {
    authenticate("firebase") {
        route("/admin/connect") {

            // Criar nova conta Connect (Accounts v2) para o lojista
            post("/accounts") {
                val principal = call.principal<UserIdPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<ConnectAccountRequest>()

                val store = storeRepository.getStore(request.storeId).getOrNull()
                if (store == null || store.ownerId != principal.name) {
                    return@post call.respond(HttpStatusCode.Forbidden, "Acesso negado à loja")
                }

                try {
                    Stripe.apiKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")

                    // Utilizando o padrão Accounts v2 com controle total da plataforma
                    // Merchant of Record: Lojista (Direct Charges)
                    val params = AccountCreateParams.builder()
                        .setEmail(request.email)
                        .setController(
                            AccountCreateParams.Controller.builder()
                                .setFees(AccountCreateParams.Controller.Fees.builder().setPayer(AccountCreateParams.Controller.Fees.Payer.ACCOUNT).build())
                                .setLosses(AccountCreateParams.Controller.Losses.builder().setPayments(AccountCreateParams.Controller.Losses.Payments.ACCOUNT).build())
                                .setRequirementCollection(AccountCreateParams.Controller.RequirementCollection.STRIPE)
                                .setDashboard(
                                    AccountCreateParams.Controller.Dashboard.builder()
                                        .setType(AccountCreateParams.Controller.Dashboard.Type.FULL)
                                        .build()
                                )
                                .build()
                        )
                        .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder().setRequested(true).build())
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build())
                                .build()
                        )
                        .build()

                    val account = Account.create(params)

                    // Salva o ID da conta no banco
                    val updatedStore = store.copy(stripeAccountId = account.id)
                    storeRepository.saveStore(updatedStore, principal.name)

                    call.respond(account.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao criar conta Connect")
                }
            }

            // Criar Account Session para Componentes Incorporados (Onboarding, Payments, etc)
            post("/sessions") {
                val principal = call.principal<UserIdPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<AccountSessionRequest>()

                val store = storeRepository.getStore(request.storeId).getOrNull()
                val accountId = store?.stripeAccountId

                if (accountId == null || store.ownerId != principal.name) {
                    return@post call.respond(HttpStatusCode.Forbidden, "Conta Connect não encontrada")
                }

                try {
                    Stripe.apiKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")

                    val params = AccountSessionCreateParams.builder()
                        .setAccount(accountId)
                        .setComponents(
                            AccountSessionCreateParams.Components.builder()
                                .setAccountOnboarding(AccountSessionCreateParams.Components.AccountOnboarding.builder().setEnabled(true).build())
                                .setPayments(AccountSessionCreateParams.Components.Payments.builder().setEnabled(true).setFeatures(
                                    AccountSessionCreateParams.Components.Payments.Features.builder().setRefundManagement(true).setDisputeManagement(true).build()
                                ).build())
                                .setPayouts(AccountSessionCreateParams.Components.Payouts.builder().setEnabled(true).build())
                                .setNotificationBanner(AccountSessionCreateParams.Components.NotificationBanner.builder().setEnabled(true).build())
                                .build()
                        )
                        .build()

                    val accountSession = AccountSession.create(params)
                    call.respond(AccountSessionResponse(clientSecret = accountSession.clientSecret))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao criar sessão de conta")
                }
            }
        }
    }
}
