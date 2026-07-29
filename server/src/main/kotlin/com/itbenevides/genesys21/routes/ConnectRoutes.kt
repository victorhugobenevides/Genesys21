package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.repository.StoreRepository
import com.itbenevides.genesys21.domain.repository.UserRepository
import com.stripe.Stripe
import com.stripe.StripeClient
import com.stripe.model.AccountSession
import com.stripe.model.LoginLink
import com.stripe.param.AccountSessionCreateParams
import com.stripe.param.LoginLinkCreateOnAccountParams
import com.stripe.param.v2.core.AccountCreateParams
import com.stripe.param.v2.core.AccountLinkCreateParams
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

@Serializable
data class ConnectLinkResponse(val url: String)

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
                    // identity.country é OBRIGATÓRIO antes de definir configuration.merchant
                    val params = AccountCreateParams.builder()
                        .setContactEmail(request.email)
                        .setDashboard(AccountCreateParams.Dashboard.FULL)
                        .setIdentity(
                            AccountCreateParams.Identity.builder()
                                .setCountry("BR") // Default para Brasil
                                .build()
                        )
                        .setDefaults(
                            AccountCreateParams.Defaults.builder()
                                .setResponsibilities(
                                    AccountCreateParams.Defaults.Responsibilities.builder()
                                        .setFeesCollector(AccountCreateParams.Defaults.Responsibilities.FeesCollector.STRIPE)
                                        .setLossesCollector(AccountCreateParams.Defaults.Responsibilities.LossesCollector.STRIPE)
                                        .build()
                                )
                                .build()
                        )
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
                } catch (e: com.stripe.exception.InvalidRequestException) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Erro na requisição Stripe: ${e.message}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao criar conta Connect")
                }
            }

            // Gerar Link de Onboarding (Accounts v2)
            get("/onboarding-link") {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val storeId = call.request.queryParameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "storeId ausente")

                val store = storeRepository.getStore(storeId).getOrNull()
                val accountId = store?.stripeAccountId

                if (accountId == null || store.ownerId != principal.name) {
                    return@get call.respond(HttpStatusCode.Forbidden, "Conta Connect não encontrada")
                }

                try {
                    val secretKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")
                    val client = StripeClient(secretKey)

                    val publicHost = System.getenv("PUBLIC_HOST") ?: "http://localhost"

                    val params = AccountLinkCreateParams.builder()
                        .setAccount(accountId)
                        .setUseCase(
                            AccountLinkCreateParams.UseCase.builder()
                                .setType(AccountLinkCreateParams.UseCase.Type.ACCOUNT_ONBOARDING)
                                .setAccountOnboarding(
                                    AccountLinkCreateParams.UseCase.AccountOnboarding.builder()
                                        .addConfiguration(AccountLinkCreateParams.UseCase.AccountOnboarding.Configuration.MERCHANT)
                                        .setRefreshUrl("$publicHost/list")
                                        .setReturnUrl("$publicHost/list")
                                        .build()
                                )
                                .build()
                        )
                        .build()

                    val accountLink = client.v2().core().accountLinks().create(params)
                    call.respond(ConnectLinkResponse(url = accountLink.url))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao gerar link de onboarding")
                }
            }

            // Gerar Link de Login para o Dashboard
            get("/login-link") {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val storeId = call.request.queryParameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "storeId ausente")

                val store = storeRepository.getStore(storeId).getOrNull()
                val accountId = store?.stripeAccountId

                if (accountId == null || store.ownerId != principal.name) {
                    return@get call.respond(HttpStatusCode.Forbidden, "Conta Connect não encontrada")
                }

                try {
                    val secretKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")
                    Stripe.apiKey = secretKey

                    val params = LoginLinkCreateOnAccountParams.builder().build()
                    val loginLink = LoginLink.createOnAccount(accountId, params)

                    call.respond(ConnectLinkResponse(url = loginLink.url))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao gerar link de login")
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
