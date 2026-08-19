package com.itbenevides.genesys21.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.util.StripeBridge
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

/**
 * Componente para renderizar Componentes Incorporados do Stripe Connect no Wasm.
 * Ele cria um elemento DOM dinamicamente e o sobrepõe ou anexa ao corpo.
 *
 * NOTA: Para uma integração visual perfeita, o ideal seria usar a API HtmlView do Compose 1.7+,
 * mas aqui utilizamos uma abordagem de bridge manual garantida.
 */
@Composable
actual fun StripeConnectComponent(
    componentName: String,
    publishableKey: String,
    clientSecret: String,
    modifier: Modifier
) {
    val elementId = remember(clientSecret) { "stripe-connect-${componentName}" }

    // Reserva o espaço no layout do Compose
    Box(modifier = modifier.fillMaxWidth().heightIn(min = 500.dp)) {
        // O conteúdo real será renderizado pela Stripe no DOM
    }

    LaunchedEffect(clientSecret) {
        if (clientSecret.isBlank()) return@LaunchedEffect

        // Inicializa o SDK do Connect via Bridge
        StripeBridge.initializeConnect(publishableKey, clientSecret).onSuccess {

            // Garante que o container exista no DOM
            var container = document.getElementById(elementId) as? HTMLElement
            if (container == null) {
                container = (document.createElement("div") as HTMLElement).apply {
                    id = elementId
                    // Estilo para garantir visibilidade e scroll
                    setAttribute("style", "width: 100%; min-height: 500.dp; padding: 20px; background: white; border-radius: 8px;")
                }

                // Anexa ao final do body ou em um local específico
                // Para Genesys21, vamos anexar ao final para evitar quebrar o canvas principal
                document.body?.appendChild(container)
            }

            // Monta o componente (ex: 'account-onboarding', 'payments', 'payouts')
            StripeBridge.mountConnectComponent(componentName, elementId).onFailure {
                println("STRIPE ERROR: Falha ao montar componente $componentName: ${it.message}")
            }
        }.onFailure {
            println("STRIPE ERROR: Falha ao inicializar Connect: ${it.message}")
        }
    }

    // Limpeza ao sair da tela
    DisposableEffect(clientSecret) {
        onDispose {
            document.getElementById(elementId)?.remove()
        }
    }
}
