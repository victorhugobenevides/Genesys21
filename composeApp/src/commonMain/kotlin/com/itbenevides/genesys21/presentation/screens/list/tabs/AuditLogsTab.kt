package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AuditLogsTab(viewModel: PageViewModel) {
    val logs by viewModel.auditLogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    LaunchedEffect(Unit) {
        viewModel.loadAuditLogs()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            AdminTabHeader(
                title = "Logs de Auditoria",
                subtitle = "Histórico de ações críticas realizadas no sistema."
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isCompact) GenesysTheme.spacing.m else GenesysTheme.spacing.l)
            ) {
                if (isLoading && logs.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GenesysTheme.colors.brand)
                    }
                } else if (logs.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        GenesysText(text = "Nenhum log registrado.", style = GenesysTextStyle.Body)
                    }
                } else {
                    logs.forEach { logMap ->
                        AuditLogCard(logMap)
                        GenesysSpacer(GenesysTheme.spacing.s)
                    }
                }

                GenesysSpacer(GenesysTheme.spacing.huge)
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: Map<String, String>) {
    val timestamp = log["timestamp"]?.toLongOrNull() ?: 0L
    val action = log["action"] ?: "AÇÃO DESCONHECIDA"
    val entityName = log["entityName"] ?: "Desconhecido"
    val entityId = log["entityId"] ?: "N/A"
    val details = log["details"] ?: "Sem detalhes."
    val userId = log["userId"] ?: "Sistema"

    val dateTime = remember(timestamp) {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GenesysText(text = action, fontWeight = GenesysFontWeight.ExtraBold, color = GenesysTheme.colors.brand)
                GenesysText(text = "${dateTime.dayOfMonth}/${dateTime.monthNumber} ${dateTime.hour}:${dateTime.minute}", style = GenesysTextStyle.Label)
            }
            GenesysSpacer(GenesysTheme.spacing.xs)
            GenesysText(text = "Entidade: $entityName (#$entityId)", style = GenesysTextStyle.Label)
            GenesysSpacer(GenesysTheme.spacing.xs)
            GenesysText(text = details, style = GenesysTextStyle.Body)
            GenesysSpacer(GenesysTheme.spacing.s)
            GenesysText(text = "Por: $userId", style = GenesysTextStyle.Label, color = GenesysTheme.colors.onSurfaceVariant)
        }
    }
}
