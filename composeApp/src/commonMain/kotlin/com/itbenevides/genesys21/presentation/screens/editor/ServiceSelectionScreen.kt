package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage

@Composable
fun ServiceSelectionScreen(
    viewModel: PageViewModel,
    selectedIds: List<String>,
    onConfirm: (List<String>) -> Unit,
    onBack: () -> Unit,
    onAddNewService: () -> Unit
) {
    val services by viewModel.services.collectAsState()
    var currentSelected by remember { mutableStateOf(selectedIds.toSet()) }

    LaunchedEffect(Unit) {
        viewModel.loadBookingServices()
    }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = "Selecionar Serviços",
                onBack = onBack,
                actions = {
                    GenesysLoadingButton(
                        text = "Novo",
                        icon = GenesysIcons.Add,
                        onClick = onAddNewService,
                        fillWidth = false
                    )
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                GenesysRow(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    GenesysLoadingButton(
                        text = "Confirmar Seleção (${currentSelected.size})",
                        onClick = { onConfirm(currentSelected.toList()) },
                        fillWidth = true
                    )
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(services) { service ->
                ServiceSelectionRow(
                    service = service,
                    isSelected = currentSelected.contains(service.id),
                    onToggle = {
                        currentSelected = if (currentSelected.contains(service.id)) {
                            currentSelected - service.id
                        } else {
                            currentSelected + service.id
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ServiceSelectionRow(
    service: BookingService,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    GenesysCard(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                GenesysText(text = service.name, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                GenesysText(text = "R$ ${service.price} • ${service.durationMinutes} min", style = GenesysTextStyle.Label)
            }
        }
    }
}
