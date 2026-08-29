package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.components.AdminTabHeader
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.theme.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AuditLogsTab(viewModel: PageViewModel) {
    val logs by viewModel.auditLogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAuditLogs()
    }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        AdminTabHeader(
            title = "Logs de Auditoria",
            subtitle = "Histórico de ações críticas realizadas no sistema."
        )

        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            if (isLoading && logs.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GenesysTheme.colors.brand)
                }
            } else if (logs.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    GenesysText(text = "Nenhum log registrado.", style = GenesysTextStyle.Body)
                }
            } else {
                logs.forEach { log ->
                    AuditLogCard(log)
                    GenesysSpacer(GenesysTheme.spacing.s)
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: Map<String, String>) {
    val timestamp = log["createdAt"]?.toLongOrNull() ?: 0L
    val date = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    val dateStr = "${date.dayOfMonth}/${date.monthNumber} ${date.hour}:${date.minute.toString().padStart(2, '0')}"

    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GenesysText(text = log["action"] ?: "AÇÃO", fontWeight = GenesysFontWeight.Bold, color = GenesysTheme.colors.brand)
                GenesysText(text = dateStr, style = GenesysTextStyle.Label, color = GenesysTheme.colors.outline)
            }
            GenesysSpacer(GenesysTheme.spacing.xs)
            GenesysText(text = "Entidade: ${log["entityName"]}", style = GenesysTextStyle.Label)
            GenesysText(text = "Usuário: ${log["userId"] ?: "Sistema"}", style = GenesysTextStyle.Label)

            log["details"]?.let { details ->
                if (details.isNotBlank()) {
                    GenesysSpacer(GenesysTheme.spacing.s)
                    GenesysText(text = details, style = GenesysTextStyle.Body, color = GenesysTheme.colors.onSurfaceVariant)
                }
            }
        }
    }
}
