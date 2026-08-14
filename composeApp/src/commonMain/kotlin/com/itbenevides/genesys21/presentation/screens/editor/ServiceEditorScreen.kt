package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysAlignment
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.rememberImagePicker
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEditorScreen(
    viewModel: PageViewModel,
    service: BookingService?,
    onSave: (BookingService) -> Unit,
    onBack: () -> Unit,
) {
    var imageUrls by remember { mutableStateOf(service?.imageUrls ?: emptyList()) }
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

    ServiceEditorContent(
        viewModel = viewModel,
        service = service,
        onSave = onSave,
        onBack = onBack,
        imageUrls = imageUrls,
        onImageUrlsChange = { imageUrls = it },
        isUploading = isUploading,
        onPickImage = { imagePicker() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEditorContent(
    viewModel: PageViewModel,
    service: BookingService?,
    onSave: (BookingService) -> Unit,
    onBack: () -> Unit,
    imageUrls: List<String>,
    onImageUrlsChange: (List<String>) -> Unit,
    isUploading: Boolean,
    onPickImage: () -> Unit,
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var price by remember { mutableStateOf(service?.price?.toString() ?: "") }
    var duration by remember { mutableStateOf(service?.durationMinutes?.toString() ?: "30") }
    var buffer by remember { mutableStateOf(service?.bufferTimeMinutes?.toString() ?: "0") }

    // Novos campos para Serviço Online/Grupo/Domicílio
    var isOnline by remember { mutableStateOf(service?.isOnline ?: false) }
    var isHomeService by remember { mutableStateOf(service?.isHomeService ?: false) }
    var maxParticipants by remember { mutableStateOf(service?.maxParticipants?.toString() ?: "1") }
    var meetingLink by remember { mutableStateOf(service?.meetingLink ?: "") }

    val isLoading by viewModel.isLoading.collectAsState()

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

                // Configurações Online
                GenesysText(text = "Configurações de Entrega", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysSpacing.Small)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        GenesysText(text = "Serviço Online", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                        GenesysText(text = "Marque se a sessão for via reunião de vídeo", style = GenesysTextStyle.Label)
                    }
                    Switch(checked = isOnline, onCheckedChange = { isOnline = it })
                }

                if (isOnline) {
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysTextField(
                        value = meetingLink,
                        onValueChange = { meetingLink = it },
                        label = "Link da Reunião (Zoom, Google Meet, etc)",
                        icon = GenesysIcons.Language,
                        placeholder = "https://meet.google.com/..."
                    )
                }

                GenesysSpacer(GenesysSpacing.Medium)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        GenesysText(text = "Atendimento a Domicílio", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                        GenesysText(text = "O serviço ocorre no endereço do cliente", style = GenesysTextStyle.Label)
                    }
                    Switch(checked = isHomeService, onCheckedChange = { isHomeService = it })
                }

                GenesysSpacer(GenesysSpacing.Medium)

                GenesysTextField(
                    value = maxParticipants,
                    onValueChange = { maxParticipants = it },
                    label = "Limite de Participantes por sessão",
                    icon = GenesysIcons.People,
                    placeholder = "1"
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
                        onClick = onPickImage,
                        isLoading = isUploading,
                        icon = GenesysIcons.Add
                    )
                }

                GenesysSpacer(GenesysSpacing.Huge)

                GenesysLoadingButton(
                    text = if (service == null) "Cadastrar Serviço" else "Salvar Alterações",
                    onClick = {
                        val newService = BookingService(
                            id = service?.id ?: (1..16).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString(""),
                            storeId = viewModel.pages.value.firstOrNull()?.storeId ?: "admin",
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            durationMinutes = duration.toIntOrNull() ?: 30,
                            bufferTimeMinutes = buffer.toIntOrNull() ?: 0,
                            isOnline = isOnline,
                            isHomeService = isHomeService,
                            maxParticipants = maxParticipants.toIntOrNull() ?: 1,
                            meetingLink = if (isOnline) meetingLink else null,
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
