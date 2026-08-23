package com.itbenevides.genesys21.ui.components.organisms.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.ChatMessage
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons

@Composable
fun OrderChatComponent(
    messages: List<ChatMessage>,
    currentNick: String,
    isMerchantView: Boolean,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newMessage by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        GenesysText(text = "Chat Interno", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysSpacer(GenesysTheme.spacing.m)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GenesysTheme.colors.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            if (messages.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    GenesysText(text = "Inicie a conversa com a loja.", style = GenesysTextStyle.Label)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        val isMe = (isMerchantView && message.isFromMerchant) || (!isMerchantView && !message.isFromMerchant)
                        ChatBubble(message, isMe)
                    }
                }
            }
        }

        GenesysSpacer(GenesysTheme.spacing.m)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.weight(1f)) {
                GenesysTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    placeholder = "Digite sua mensagem...",
                    singleLine = true
                )
            }
            GenesysLoadingButton(
                text = "Enviar",
                icon = GenesysIcons.ArrowRight,
                onClick = {
                    if (newMessage.isNotBlank()) {
                        onSendMessage(newMessage)
                        newMessage = ""
                    }
                },
                enabled = newMessage.isNotBlank()
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 16.dp
                    )
                )
                .background(if (isMe) GenesysTheme.colors.brand else GenesysTheme.colors.surface)
                .padding(12.dp)
        ) {
            Column {
                if (!isMe) {
                    GenesysText(
                        text = message.senderNick,
                        style = GenesysTextStyle.Label,
                        fontWeight = GenesysFontWeight.Bold,
                        color = GenesysTheme.colors.brand
                    )
                }
                GenesysText(
                    text = message.content,
                    color = if (isMe) GenesysTheme.colors.onBrand else GenesysTheme.colors.onSurface
                )
            }
        }
    }
}
