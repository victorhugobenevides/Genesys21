package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun ServiceListComponentEditor(
    component: PageComponent.ServiceList,
    allAvailableServices: List<BookingService>,
    onEditService: (BookingService?) -> Unit,
    onServicesUpdated: (List<BookingService>) -> Unit,
    onSaveTitle: (String) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf(component.title) }
    var searchQuery by remember { mutableStateOf("") }

    GenesysColumn(usePadding = false) {
        GenesysText("Gerenciar Serviços", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysSpacer(GenesysSpacing.Medium)

        GenesysTabRow(
            selectedTabIndex = selectedTab,
            tabs =
                listOf(
                    GenesysTabData("Na Lista", GenesysIcons.List, component.services.size),
                    GenesysTabData("Catálogo", GenesysIcons.Inventory),
                ),
            onTabSelected = { selectedTab = it },
        )

        GenesysSpacer(GenesysSpacing.Medium)

        GenesysTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Buscar serviço...",
            icon = GenesysIcons.Search,
        )

        GenesysSpacer(GenesysSpacing.Medium)

        if (selectedTab == 0) {
            // ABA 1: Serviços na Lista
            val filteredServices =
                component.services.filter { it.name.contains(searchQuery, ignoreCase = true) }

             GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 300.dp), useScroll = true) {
                filteredServices.forEach { service ->
                    val index = component.services.indexOf(service)
                    GenesysCard(modifier = Modifier.padding(bottom = 4.dp)) {
                        GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                            GenesysWeightBox(1f) { GenesysText(service.name) }
                            GenesysRow(fillWidth = false) {
                                 GenesysIconButton(icon = GenesysIcons.Edit, onClick = { onEditService(service) })
                                 GenesysIconButton(
                                    icon = GenesysIcons.Remove,
                                    tint = Color.Red.copy(alpha = 0.6f),
                                    onClick = { onServicesUpdated(component.services.filter { it.id != service.id }) },
                                )
                            }
                        }
                    }
                }
                GenesysSpacer(GenesysSpacing.Medium)
                GenesysLoadingButton(
                    text = "Cadastrar Novo Serviço",
                    icon = GenesysIcons.Add,
                    onClick = { onEditService(null) },
                    fillWidth = true,
                )
             }
        } else {
            // ABA 2: Catálogo Global
            val catalogToDisplay =
                allAvailableServices
                    .filter { s -> component.services.none { it.id == s.id } }
                    .filter { it.name.contains(searchQuery, ignoreCase = true) }

            GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 300.dp), useScroll = true) {
                if (catalogToDisplay.isEmpty()) {
                    GenesysText("Nenhum serviço extra no catálogo", style = GenesysTextStyle.Label)
                } else {
                    catalogToDisplay.forEach { service ->
                        GenesysCard(modifier = Modifier.padding(bottom = 4.dp)) {
                            GenesysRow {
                                GenesysWeightBox(1f) { GenesysText(service.name) }
                                GenesysLoadingButton(
                                    text = "Adicionar",
                                    icon = GenesysIcons.Add,
                                    onClick = { onServicesUpdated(component.services + service) },
                                )
                            }
                        }
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysDivider()
        GenesysSpacer(GenesysSpacing.Large)

        GenesysTextField(
            value = title,
            onValueChange = { title = it },
            label = "Título da Seção",
            icon = GenesysIcons.Edit,
        )

        GenesysSpacer(GenesysSpacing.Huge)
        GenesysLoadingButton(
            text = "Salvar Alterações",
            fillWidth = true,
            onClick = { onSaveTitle(title) }
        )
    }
}
