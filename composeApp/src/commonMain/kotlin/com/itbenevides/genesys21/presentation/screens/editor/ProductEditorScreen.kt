package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysDropdownField
import com.itbenevides.genesys21.ui.components.molecules.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.organisms.input.GenesysPhotoPicker
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
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
    onBack: () -> Unit,
) {
    var state by remember { mutableStateOf(ProductEditorState.initial(product)) }

    val imagePicker =
        rememberImagePicker { bytes ->
            bytes?.let {
                if (state.imageUrls.size < 5) {
                    state = state.copy(isUploading = true)
                    viewModel.uploadImage(it, "prod_${Random.nextInt(10000)}.jpg") { uploadedUrl ->
                        state =
                            state.copy(
                                imageUrls = state.imageUrls + uploadedUrl,
                                isUploading = false,
                            )
                    }
                }
            }
        }

    ProductEditorContent(
        viewModel = viewModel,
        page = page,
        product = product,
        onSave = onSave,
        onBack = onBack,
        state = state,
        onStateChange = { state = it },
        onPickImage = { imagePicker() }
    )
}

@Composable
fun ProductEditorContent(
    viewModel: PageViewModel,
    page: Page,
    product: Product?,
    onSave: (Product) -> Unit,
    onBack: () -> Unit,
    state: ProductEditorState,
    onStateChange: (ProductEditorState) -> Unit,
    onPickImage: () -> Unit,
) {
    val isGlobalLoading by viewModel.isLoading.collectAsState()
    val backendUrl = remember { getBaseUrl() }
    val categories by viewModel.categories.collectAsState()

    var showCategoryManagement by remember { mutableStateOf(false) }

    LaunchedEffect(isGlobalLoading) {
        onStateChange(state.copy(isLoading = isGlobalLoading))
    }

    val onEvent: (ProductEditorEvent) -> Unit = { event ->
        when (event) {
            is ProductEditorEvent.OnNameChanged -> onStateChange(state.copy(name = event.name))
            is ProductEditorEvent.OnPriceChanged -> onStateChange(state.copy(price = InputValidator.validatePrice(event.price)))
            is ProductEditorEvent.OnDescriptionChanged -> onStateChange(state.copy(description = event.description))
            is ProductEditorEvent.OnCategoryChanged -> {
                onStateChange(state.copy(categoryId = event.categoryId, categoryName = event.categoryName))
            }
            is ProductEditorEvent.OnStockChanged -> onStateChange(state.copy(stock = InputValidator.validateStock(event.stock)))
            is ProductEditorEvent.OnAddPhotoClicked -> if (!state.isUploading && state.imageUrls.size < 5) onPickImage()
            is ProductEditorEvent.OnRemovePhotoClicked -> {
                val toRemove =
                    state.imageUrls.find {
                        (if (it.startsWith("/") && !it.startsWith("http")) "$backendUrl$it" else it) == event.url
                    } ?: event.url
                onStateChange(state.copy(imageUrls = state.imageUrls.filter { it != toRemove }))
            }
            is ProductEditorEvent.OnSaveClicked -> {
                val finalProduct =
                    Product(
                        id = product?.id ?: (1..16).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString(""),
                        storeId = page.storeId,
                        name = state.name.trim(),
                        price = InputValidator.parsePrice(state.price.replace(",", ".")),
                        imageUrls = state.imageUrls,
                        description = state.description.trim(),
                        categoryId = state.categoryId,
                        categoryName = state.categoryName,
                        stock = InputValidator.parseStock(state.stock),
                    )
                onSave(finalProduct)
            }
        }
    }

    AppTheme(themeConfig = page.theme) {
        ProductEditorMainLayout(
            state = state,
            backendUrl = backendUrl,
            categoryOptions = categories.map { it.name },
            onEvent = onEvent,
            onBack = onBack,
            onManageCategories = { showCategoryManagement = true },
        )

        if (showCategoryManagement) {
            CategoryManagementDialog(
                viewModel = viewModel,
                onDismiss = { showCategoryManagement = false },
            )
        }
    }
}

@Composable
private fun ProductEditorMainLayout(
    state: ProductEditorState,
    backendUrl: String,
    categoryOptions: List<String>,
    onEvent: (ProductEditorEvent) -> Unit,
    onBack: () -> Unit,
    onManageCategories: () -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == GenesysWindowSizeClass.EXPANDED

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = if (state.isEditing) GenesysStrings.EditProduct else GenesysStrings.NewProduct,
                onBack = onBack,
                actions = {
                    GenesysLoadingButton(
                        text = if (isExpanded) GenesysStrings.Save else "",
                        icon = if (isExpanded) GenesysIcons.Check else GenesysIcons.Check,
                        onClick = { onEvent(ProductEditorEvent.OnSaveClicked) },
                        isLoading = state.isLoading,
                        enabled = state.canSave,
                    )
                },
            )
        },
    ) {
        val scrollState = rememberScrollState()

        // Root Container com largura máxima centralizada
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 1200.dp)
                        .fillMaxSize()
                        .then(if (!isExpanded) Modifier.verticalScroll(scrollState) else Modifier),
            ) {
                if (isExpanded) {
                    // DESKTOP: Lado a Lado
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        // Seção Fotos
                        Box(modifier = Modifier.weight(0.45f)) {
                            PhotosSection(state, backendUrl, onEvent)
                        }

                        Spacer(Modifier.width(24.dp))

                        // Seção Dados (com scroll interno no desktop)
                        Box(modifier = Modifier.weight(0.55f).fillMaxHeight()) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                DataFormSection(state, categoryOptions, onEvent, onManageCategories)
                            }
                        }
                    }
                } else {
                    // MOBILE: Um abaixo do outro (Scroll unificado no pai)
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        PhotosSection(state, backendUrl, onEvent)
                        Spacer(Modifier.height(24.dp))
                        DataFormSection(state, categoryOptions, onEvent, onManageCategories)
                        Spacer(Modifier.height(64.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotosSection(
    state: ProductEditorState,
    backendUrl: String,
    onEvent: (ProductEditorEvent) -> Unit,
) {
    GenesysColumn(usePadding = false) {
        GenesysSectionHeader(
            title = GenesysStrings.PhotosTitle,
            subtitle = "${state.imageUrls.size}/5",
        )
        GenesysSpacer(GenesysSpacing.Small)
        val displayUrls =
            remember(state.imageUrls, backendUrl) {
                state.imageUrls.map { if (it.startsWith("/") && !it.startsWith("http")) "$backendUrl$it" else it }
            }
        GenesysPhotoPicker(
            urls = displayUrls,
            onAddClick = { onEvent(ProductEditorEvent.OnAddPhotoClicked) },
            onRemoveClick = { onEvent(ProductEditorEvent.OnRemovePhotoClicked(it)) },
            isUploading = state.isUploading,
        )
    }
}

@Composable
private fun DataFormSection(
    state: ProductEditorState,
    categoryOptions: List<String>,
    onEvent: (ProductEditorEvent) -> Unit,
    onManageCategories: () -> Unit,
) {
    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(text = GenesysStrings.ProductGeneralInfo, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysSpacing.Medium)

            GenesysTextField(
                value = state.name,
                onValueChange = { onEvent(ProductEditorEvent.OnNameChanged(it)) },
                label = GenesysStrings.ProductName,
                icon = GenesysIcons.Inventory,
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysRow {
                GenesysWeightBox(1f) {
                    GenesysTextField(
                        value = state.price,
                        onValueChange = { onEvent(ProductEditorEvent.OnPriceChanged(it)) },
                        label = GenesysStrings.ProductPrice,
                        icon = GenesysIcons.Payments,
                    )
                }
                GenesysSpacer(GenesysSpacing.Medium)
                GenesysWeightBox(1f) {
                    GenesysTextField(
                        value = state.stock,
                        onValueChange = { onEvent(ProductEditorEvent.OnStockChanged(it)) },
                        label = GenesysStrings.ProductStock,
                        icon = GenesysIcons.Numbers,
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
                        icon = GenesysIcons.Category,
                    )
                }
                GenesysSpacer(GenesysSpacing.Small)
                GenesysIconButton(
                    icon = GenesysIcons.Settings,
                    onClick = onManageCategories,
                    contentDescription = "Gerenciar Categorias",
                )
            }

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysTextField(
                value = state.description,
                onValueChange = { onEvent(ProductEditorEvent.OnDescriptionChanged(it)) },
                label = GenesysStrings.ProductDescription,
                icon = GenesysIcons.Description,
                singleLine = false,
                minLines = 6,
            )
        }
    }
}
