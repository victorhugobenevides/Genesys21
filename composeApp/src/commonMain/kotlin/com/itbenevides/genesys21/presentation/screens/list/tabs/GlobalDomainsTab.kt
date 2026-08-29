package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.components.AdminTabHeader
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun GlobalDomainsTab(viewModel: PageViewModel) {
    val mappings by viewModel.domainMappings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDomainMappings()
    }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        AdminTabHeader(
            title = "Domínios Globais",
            subtitle = "Gerencie o mapeamento de domínios customizados para as vitrines.",
            action = {
                GenesysLoadingButton(
                    text = "Novo Domínio",
                    onClick = { showAddDialog = true },
                    icon = GenesysIcons.Add,
                    fillWidth = false
                )
            }
        )

        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            if (isLoading && mappings.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GenesysTheme.colors.brand)
                }
            } else if (mappings.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    GenesysText(text = "Nenhum domínio mapeado.", style = GenesysTextStyle.Body)
                }
            } else {
                mappings.forEach { mapping ->
                    DomainMappingCard(mapping, onDelete = { viewModel.deleteDomainMapping(mapping.id) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddDomainDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { domain, pageId ->
                viewModel.saveDomainMapping(domain, pageId)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DomainMappingCard(mapping: DomainMapping, onDelete: () -> Unit) {
    GenesysCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GenesysText(text = mapping.domain, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                GenesysText(text = "Direciona para: ${mapping.targetPageId}", style = GenesysTextStyle.Label)
            }
            GenesysIconButton(
                icon = GenesysIcons.Delete,
                onClick = onDelete,
                tint = GenesysTheme.colors.error
            )
        }
    }
}

@Composable
private fun AddDomainDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var domain by remember { mutableStateOf("") }
    var pageId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { GenesysText("Mapear Novo Domínio", style = GenesysTextStyle.Title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GenesysTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = "Domínio (ex: loja.com)",
                    placeholder = "meu-site.com"
                )
                GenesysTextField(
                    value = pageId,
                    onValueChange = { pageId = it },
                    label = "ID da Vitrine (UUID)",
                    placeholder = "ID da página no Genesys21"
                )
            }
        },
        confirmButton = {
            GenesysLoadingButton(
                text = "Salvar Mapeamento",
                onClick = { onConfirm(domain, pageId) },
                enabled = domain.isNotBlank() && pageId.isNotBlank()
            )
        },
        dismissButton = {
            GenesysTextButton(text = "Cancelar", onClick = onDismiss)
        },
        containerColor = GenesysTheme.colors.surface,
        shape = RoundedCornerShape(GenesysTheme.config.cornerRadius)
    )
}
