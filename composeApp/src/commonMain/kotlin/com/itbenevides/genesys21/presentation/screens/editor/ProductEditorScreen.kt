package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.input.GenesysDropdownField
import com.itbenevides.genesys21.ui.components.input.GenesysPhotoPicker
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysAlignment
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysRow
import com.itbenevides.genesys21.ui.components.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.layout.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.InputValidator
import com.itbenevides.genesys21.util.rememberImagePicker
import kotlin.random.Random

@Composable
fun ProductEditorScreen(
    viewModel: PageViewModel,
    page: Page,
    product: Product?,
    existingCategories: List<String>,
    onSave: (Product) -> Unit,
    onBack: () -> Unit
) {
    // 1. State Holder
    var state by remember { mutableStateOf(ProductEditorState.initial(product)) }
    val isGlobalLoading by viewModel.isLoading.collectAsState()
    val backendUrl = remember { getBaseUrl() }
    val categories by viewModel.categories.collectAsState()
    
    var showCategoryManagement by remember { mutableStateOf(false) }

    state = state.copy(isLoading = isGlobalLoading)

    // 2. Orquestrador de Eventos
    val imagePicker = rememberImagePicker { bytes ->
        bytes?.let {
            if (state.imageUrls.size < 5) {
                state = state.copy(isUploading = true)
                viewModel.uploadImage(it, "prod_${Random.nextInt(10000)}.jpg") { uploadedUrl ->
                    state = state.copy(
                        imageUrls = state.imageUrls + uploadedUrl,
                        isUploading = false
                    )
                }
            }
        }
    }

    val onEvent: (ProductEditorEvent) -> Unit = { event ->
        when (event) {
            is ProductEditorEvent.OnNameChanged -> state = state.copy(name = event.name)
            is ProductEditorEvent.OnPriceChanged -> state = state.copy(price = InputValidator.validatePrice(event.price))
            is ProductEditorEvent.OnDescriptionChanged -> state = state.copy(description = event.description)
            is ProductEditorEvent.OnCategoryChanged -> {
                state = state.copy(categoryId = event.categoryId, categoryName = event.categoryName)
            }
            is ProductEditorEvent.OnStockChanged -> state = state.copy(stock = InputValidator.validateStock(event.stock))
            is ProductEditorEvent.OnAddPhotoClicked -> if (!state.isUploading && state.imageUrls.size < 5) imagePicker()
            is ProductEditorEvent.OnRemovePhotoClicked -> {
                val toRemove = state.imageUrls.find { 
                    (if (it.startsWith("/") && !it.startsWith("http")) "$backendUrl$it" else it) == event.url 
                } ?: event.url
                state = state.copy(imageUrls = state.imageUrls.filter { it != toRemove })
            }
            is ProductEditorEvent.OnSaveClicked -> {
                val finalProduct = Product(
                    id = product?.id ?: "P-${Random.nextInt(1000, 9999)}",
                    name = state.name.trim(),
                    price = InputValidator.parsePrice(state.price.replace(",", ".")),
                    imageUrls = state.imageUrls,
                    description = state.description.trim(),
                    categoryId = state.categoryId,
                    categoryName = state.categoryName,
                    stock = InputValidator.parseStock(state.stock)
                )
                onSave(finalProduct)
            }
        }
    }

    // 3. Renderização sob o tema da vitrine
    AppTheme(themeConfig = page.theme) {
        ProductEditorContent(
            state = state, 
            backendUrl = backendUrl, 
            categoryOptions = categories.map { it.name }, 
            onEvent = onEvent, 
            onBack = onBack, 
            onManageCategories = { showCategoryManagement = true }
        )
        
        if (showCategoryManagement) {
            CategoryManagementDialog(
                viewModel = viewModel,
                onDismiss = { showCategoryManagement = false }
            )
        }
    }
}

@Composable
private fun ProductEditorContent(
    state: ProductEditorState,
    backendUrl: String,
    categoryOptions: List<String>,
    onEvent: (ProductEditorEvent) -> Unit,
    onBack: () -> Unit,
    onManageCategories: () -> Unit
) {
     GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = if (state.isEditing) GenesysStrings.EditProduct else GenesysStrings.NewProduct,
                onBack = onBack,
                actions = {
                    GenesysLoadingButton(
                        text = GenesysStrings.Save,
                        onClick = { onEvent(ProductEditorEvent.OnSaveClicked) },
                        isLoading = state.isLoading,
                        enabled = state.canSave,
                        icon = GenesysIcons.Check
                    )
                }
            )
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 1000.dp

            GenesysColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = GenesysAlignment.Center,
                usePadding = false
            ) {
                GenesysRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Top,
                    usePadding = false
                ) {
                    // SEÇÃO 1: Galeria de Fotos
                    GenesysWeightBox(if (isWideScreen) 0.45f else 1f) {
                        GenesysColumn(usePadding = true, useScroll = !isWideScreen) {
                            GenesysSectionHeader(
                                title = GenesysStrings.PhotosTitle,
                                subtitle = "${state.imageUrls.size}/5"
                            )

                            GenesysSpacer(GenesysSpacing.Small)

                            val displayUrls = remember(state.imageUrls, backendUrl) {
                                state.imageUrls.map { if (it.startsWith("/") && !it.startsWith("http")) "$backendUrl$it" else it }
                            }

                            GenesysPhotoPicker(
                                urls = displayUrls,
                                onAddClick = { onEvent(ProductEditorEvent.OnAddPhotoClicked) },
                                onRemoveClick = { onEvent(ProductEditorEvent.OnRemovePhotoClicked(it)) },
                                isUploading = state.isUploading
                            )
                        }
                    }

                    if (isWideScreen) GenesysSpacer(GenesysSpacing.Large)

                    // SEÇÃO 2: Formulário de Dados
                    GenesysWeightBox(if (isWideScreen) 0.55f else 1f) {
                        GenesysColumn(usePadding = true, useScroll = true) {
                            GenesysCard {
                                GenesysColumn(usePadding = false) {
                                    GenesysText(text = GenesysStrings.ProductGeneralInfo, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                                    GenesysSpacer(GenesysSpacing.Medium)

                                    GenesysTextField(
                                        value = state.name,
                                        onValueChange = { onEvent(ProductEditorEvent.OnNameChanged(it)) },
                                        label = GenesysStrings.ProductName,
                                        icon = GenesysIcons.Inventory
                                    )

                                    GenesysSpacer(GenesysSpacing.Medium)

                                    GenesysRow {
                                        GenesysWeightBox(1f) {
                                            GenesysTextField(
                                                value = state.price,
                                                onValueChange = { onEvent(ProductEditorEvent.OnPriceChanged(it)) },
                                                label = GenesysStrings.ProductPrice,
                                                icon = GenesysIcons.Payments
                                            )
                                        }
                                        GenesysSpacer(GenesysSpacing.Medium)
                                        GenesysWeightBox(1f) {
                                            GenesysTextField(
                                                value = state.stock,
                                                onValueChange = { onEvent(ProductEditorEvent.OnStockChanged(it)) },
                                                label = GenesysStrings.ProductStock,
                                                icon = GenesysIcons.Numbers
                                            )
                                        }
                                    }

                                    GenesysSpacer(GenesysSpacing.Medium)

                                    GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            GenesysDropdownField(
                                                value = state.categoryName,
                                                onValueChange = { name ->
                                                    onEvent(ProductEditorEvent.OnCategoryChanged(null, name))
                                                },
                                                label = GenesysStrings.ProductCategory,
                                                options = categoryOptions,
                                                icon = GenesysIcons.Category
                                            )
                                        }
                                        GenesysSpacer(GenesysSpacing.Small)
                                        GenesysIconButton(
                                            icon = GenesysIcons.Settings, 
                                            onClick = onManageCategories,
                                            contentDescription = "Gerenciar Categorias"
                                        )
                                    }

                                    GenesysSpacer(GenesysSpacing.Medium)

                                    GenesysTextField(
                                        value = state.description,
                                        onValueChange = { onEvent(ProductEditorEvent.OnDescriptionChanged(it)) },
                                        label = GenesysStrings.ProductDescription,
                                        icon = GenesysIcons.Description,
                                        singleLine = false,
                                        minLines = 6
                                    )
                                }
                            }
                            
                            GenesysSpacer(GenesysSpacing.Huge)
                        }
                    }
                }
            }
        }
    }
}
