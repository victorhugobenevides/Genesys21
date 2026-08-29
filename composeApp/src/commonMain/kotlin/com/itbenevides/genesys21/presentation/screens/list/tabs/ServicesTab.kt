package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.presentation.screens.list.components.AdminTabHeader
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.booking.ServiceCard
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

@Composable
fun ServicesTab(
    services: List<BookingService>,
    onAddService: () -> Unit,
    onEditService: (BookingService) -> Unit,
    onDeleteService: (String) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        AdminTabHeader(
            title = "Gestão de Serviços",
            subtitle = "Configure os tratamentos e preços do seu negócio.",
            action = {
                GenesysLoadingButton(
                    text = "Novo Serviço",
                    icon = GenesysIcons.Add,
                    onClick = onAddService,
                    fillWidth = false
                )
            }
        )

        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            if (services.isEmpty()) {
                GenesysEmptyState(
                    icon = GenesysIcons.Inventory,
                    title = "Nenhum serviço cadastrado",
                    description = "Comece adicionando o primeiro serviço do seu negócio.",
                    action = {
                        GenesysLoadingButton(text = "Cadastrar Primeiro Serviço", onClick = onAddService)
                    }
                )
            } else {
                val columns = if (isCompact) 1 else 2

                Column {
                    services.chunked(columns).forEach { rowServices ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.m)
                        ) {
                            rowServices.forEach { service ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ServiceCard(
                                        service = service,
                                        onClick = { onEditService(service) }
                                    )
                                    Row(modifier = Modifier.align(Alignment.TopEnd).padding(GenesysTheme.spacing.xs)) {
                                        GenesysIconButton(
                                            icon = GenesysIcons.Delete,
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            onClick = { onDeleteService(service.id) }
                                        )
                                    }
                                }
                            }
                            if (rowServices.size < columns) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(GenesysTheme.spacing.m))
                    }
                }
            }
        }
    }
}
