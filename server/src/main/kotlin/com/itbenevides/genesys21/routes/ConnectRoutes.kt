package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.repository.StoreRepository
import com.itbenevides.genesys21.domain.repository.UserRepository
import com.stripe.Stripe
import com.stripe.model.Account
import com.stripe.model.AccountLink
import com.stripe.param.AccountCreateParams
import com.stripe.param.AccountLinkCreateParams
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
data class ConnectLinkResponse(val url: String)

fun Route.connectRoutes(
    userRepository: UserRepository,
    storeRepository: StoreRepository
) {
    authenticate("firebase") {
        route("/admin/connect") {

            // Criar nova conta Connect (Express) para o lojista
            post("/accounts") {
                val principal = call.principal<UserIdPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<ConnectAccountRequest>()

                val store = storeRepository.getStore(request.storeId).getOrNull()
                if (store == null || store.ownerId != principal.name) {
                    return@post call.respond(HttpStatusCode.Forbidden, "Acesso negado à loja")
                }

                try {
                    // Configura chave secreta global (deve vir de env var na prática)
                    Stripe.apiKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")

                    val params = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setEmail(request.email)
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
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao criar conta Connect")
                }
            }

            // Gerar Link de Onboarding (para o lojista preencher os dados)
            get("/onboarding-link") {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val storeId = call.request.queryParameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                val store = storeRepository.getStore(storeId).getOrNull()
                val accountId = store?.stripeAccountId

                if (accountId == null || store.ownerId != principal.name) {
                    return@get call.respond(HttpStatusCode.Forbidden, "Conta Connect não encontrada")
                }

                try {
                    Stripe.apiKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")

                    val host = call.request.header(HttpHeaders.Host) ?: "localhost:8081"
                    val scheme = if (host.contains("localhost")) "http" else "https"
                    val baseUrl = "$scheme://$host"

                    val params = AccountLinkCreateParams.builder()
                        .setAccount(accountId)
                        .setRefreshUrl("$baseUrl/#/view/$storeId?status=onboarding_refresh")
                        .setReturnUrl("$baseUrl/#/view/$storeId?status=onboarding_complete")
                        .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                        .build()

                    val accountLink = AccountLink.create(params)
                    call.respond(ConnectLinkResponse(accountLink.url))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao gerar link")
                }
            }

            // Gerar link de login para o Dashboard Express
            get("/login-link") {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val storeId = call.request.queryParameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                val store = storeRepository.getStore(storeId).getOrNull()
                val accountId = store?.stripeAccountId

                if (accountId == null || store.ownerId != principal.name) {
                    return@get call.respond(HttpStatusCode.Forbidden, "Conta Connect não encontrada")
                }

                try {
                    Stripe.apiKey = store.stripeSecretKey ?: System.getenv("STRIPE_SECRET_KEY")
                    val loginLink = com.stripe.model.LoginLink.createOnAccount(accountId, emptyMap<String, Any>(), null)
                    call.respond(ConnectLinkResponse(loginLink.url))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao gerar link de login")
                }
            }
        }
    }
}
