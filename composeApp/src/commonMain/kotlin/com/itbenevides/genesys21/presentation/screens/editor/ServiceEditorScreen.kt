package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.rememberImagePicker
import kotlin.random.Random

@Composable
fun ServiceEditorScreen(
    viewModel: PageViewModel,
    service: BookingService?,
    onSave: (BookingService) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var price by remember { mutableStateOf(service?.price?.toString() ?: "") }
    var duration by remember { mutableStateOf(service?.durationMinutes?.toString() ?: "30") }
    var buffer by remember { mutableStateOf(service?.bufferTimeMinutes?.toString() ?: "0") }
    var imageUrls by remember { mutableStateOf(service?.imageUrls ?: emptyList()) }

    val isLoading by viewModel.isLoading.collectAsState()
    var isUploading by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePicker { bytes ->
        bytes?.let {
            isUploading = true
            viewModel.uploadImage(it, "service_${Random.nextInt(10000)}.jpg") { url ->
                imageUrls = imageUrls + url
                isUploading = false
            }
        }
    }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = if (service == null) "Cadastrar Serviço" else "Editar Serviço",
                onBack = onBack
            )
        }
    ) {
        GenesysColumn(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            usePadding = true,
            horizontalAlignment = GenesysAlignment.Center
        ) {
            GenesysColumn(maxWidth = 600.dp, usePadding = false) {
                GenesysTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome do Serviço",
                    icon = GenesysIcons.Edit
                )
                GenesysSpacer(GenesysSpacing.Medium)

                GenesysTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descrição",
                    singleLine = false,
                    minLines = 3,
                    icon = GenesysIcons.Description
                )
                GenesysSpacer(GenesysSpacing.Medium)

                GenesysRow {
                    GenesysWeightBox(1f) {
                        GenesysTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = "Preço (R$)",
                            icon = GenesysIcons.Payments
                        )
                    }
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysWeightBox(1f) {
                        GenesysTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = "Duração (min)",
                            icon = GenesysIcons.Schedule
                        )
                    }
                }
                GenesysSpacer(GenesysSpacing.Medium)

                GenesysTextField(
                    value = buffer,
                    onValueChange = { buffer = it },
                    label = "Tempo de Intervalo/Buffer (min)",
                    icon = GenesysIcons.History
                )

                GenesysSpacer(GenesysSpacing.Large)

                // Imagens do Serviço
                GenesysText(text = "Fotos do Serviço", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysSpacing.Small)

                GenesysRow(modifier = Modifier.fillMaxWidth(), useHorizontalScroll = true) {
                    imageUrls.forEach { url ->
                        com.itbenevides.genesys21.ui.components.atoms.images.GenesysImage(
                            url = url,
                            size = 100.dp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    GenesysLoadingButton(
                        text = "Adicionar Foto",
                        onClick = { imagePicker() },
                        isLoading = isUploading,
                        icon = GenesysIcons.Add
                    )
                }

                GenesysSpacer(GenesysSpacing.Huge)

                GenesysLoadingButton(
                    text = if (service == null) "Cadastrar Serviço" else "Salvar Alterações",
                    onClick = {
                        val newService = BookingService(
                            id = service?.id ?: "s_${Random.nextInt(1000000)}",
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            durationMinutes = duration.toIntOrNull() ?: 30,
                            bufferTimeMinutes = buffer.toIntOrNull() ?: 0,
                            imageUrls = imageUrls
                        )
                        onSave(newService)
                    },
                    isLoading = isLoading,
                    fillWidth = true,
                    enabled = name.isNotBlank() && price.isNotBlank()
                )
            }
        }
    }
}
