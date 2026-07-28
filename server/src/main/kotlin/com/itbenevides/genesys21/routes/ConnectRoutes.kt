package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.repository.StoreRepository
import com.itbenevides.genesys21.domain.repository.UserRepository
import com.stripe.Stripe
import com.stripe.StripeClient
import com.stripe.model.AccountSession
import com.stripe.param.AccountSessionCreateParams
import com.stripe.param.v2.core.AccountCreateParams
import io.ktor.http.*
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
                    val secretKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")
                    if (secretKey.isNullOrBlank() || secretKey.contains("default")) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Configuração do Stripe incompleta: Chave de API ausente ou inválida.")
                    }

                    val client = StripeClient(secretKey)

                    // Utilizando o padrão Accounts v2
                    // Merchant of Record: Lojista (Direct Charges)
                    // Nas contas v2, as capacidades de pagamento são configuradas através do Merchant Configuration
                    val params = AccountCreateParams.builder()
                        .setContactEmail(request.email)
                        .setDashboard(AccountCreateParams.Dashboard.FULL)
                        .setConfiguration(
                            AccountCreateParams.Configuration.builder()
                                .setMerchant(
                                    AccountCreateParams.Configuration.Merchant.builder()
                                        .build()
                                )
                                .build()
                        )
                        .build()

                    val account = client.v2().core().accounts().create(params)

                    // Salva o ID da conta no banco
                    val updatedStore = store.copy(stripeAccountId = account.id)
                    storeRepository.saveStore(updatedStore, principal.name)

                    call.respond(account.id)
                } catch (e: com.stripe.exception.AuthenticationException) {
                    call.respond(HttpStatusCode.Unauthorized, "Erro de Autenticação Stripe: Verifique suas chaves de API.")
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
                    val secretKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")
                    if (secretKey.isNullOrBlank() || secretKey.contains("default")) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Configuração do Stripe incompleta: Chave de API ausente ou inválida.")
                    }
                    Stripe.apiKey = secretKey

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
                } catch (e: com.stripe.exception.AuthenticationException) {
                    call.respond(HttpStatusCode.Unauthorized, "Erro de Autenticação Stripe: Verifique suas chaves de API.")
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao criar sessão de conta")
                }
            }
        }
    }
}
