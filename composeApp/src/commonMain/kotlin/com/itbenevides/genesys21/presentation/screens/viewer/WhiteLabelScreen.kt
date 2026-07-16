package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.BrandingEffects
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.*
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.feedback.*
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.rememberImagePicker
import kotlin.random.Random

@Composable
fun WhiteLabelScreen(
    viewModel: PageViewModel,
    page: Page,
    onPageChange: (Page) -> Unit,
    onBack: () -> Unit,
    onEditProduct: (Product?, Int?) -> Unit,
    onEditService: (BookingService?, Int?) -> Unit,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val serverProducts by viewModel.allAvailableProducts.collectAsState()
    val savedCategories by viewModel.allAvailableCategories.collectAsState()
    val userPages by viewModel.pages.collectAsState()

    val pristinePage = remember(page.id) { page }
    var showCategoryManagement by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    var state by remember {
        val draft = viewModel.getDraft(page.id)
        mutableStateOf(
            WhiteLabelState(
                page = draft ?: page,
                isLoading = isLoading,
                availableProducts = serverProducts,
                allAvailableCategories = savedCategories,
                userPages = userPages,
            ),
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadPages()
        viewModel.loadCategories()

        viewModel.getDraft(page.id)?.let { updatedDraft ->
            state = state.copy(page = updatedDraft)
        }
    }

    LaunchedEffect(isLoading, serverProducts, savedCategories, userPages) {
        state =
            state.copy(
                isLoading = isLoading,
                availableProducts = serverProducts,
                allAvailableCategories = savedCategories,
                userPages = userPages,
            )
    }

    LaunchedEffect(state.page) {
        viewModel.saveDraft(state.page)
    }

    val effectiveCategories =
        remember(savedCategories, state.page) {
            val categoriesInDraft =
                state.page.components
                    .filterIsInstance<PageComponent.ProductList>()
                    .flatMap { it.products }
                    .mapNotNull { it.categoryName }
            (savedCategories + categoriesInDraft).filter { it.isNotBlank() }.distinct().sorted()
        }

    val imagePicker =
        rememberImagePicker { bytes: ByteArray? ->
            bytes?.let {
                state = state.copy(isUploading = true)
                viewModel.uploadImage(it, "profile_${Random.nextInt(10000)}.jpg") { uploadedUrl ->
                    state.editingComponentIndex?.let { index ->
                        val component = state.page.components[index]
                        val updated =
                            when (component) {
                                is PageComponent.Image -> component.copy(url = uploadedUrl)
                                is PageComponent.ProfileHeader -> component.copy(imageUrl = uploadedUrl)
                                else -> component
                            }
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        state =
                            state.copy(
                                page = state.page.copy(components = newList),
                                isUploading = false,
                            )
                    }
                }
            }
        }

    fun onEvent(event: WhiteLabelEvent) {
        when (event) {
            is WhiteLabelEvent.OnPageUpdated -> {
                state = state.copy(page = event.newPage)
                onPageChange(event.newPage)
            }
            is WhiteLabelEvent.OnPublishClicked -> {
                viewModel.savePage(state.page, isDraft = false) {
                    viewModel.clearDraft(state.page.id)
                    onBack()
                }
            }
            is WhiteLabelEvent.OnBackClicked -> onBack()
            is WhiteLabelEvent.OnEditProductClicked -> onEditProduct(event.product, event.componentIndex)
            is WhiteLabelEvent.OnEditServiceClicked -> onEditService(event.service, event.componentIndex)
            is WhiteLabelEvent.OnShowCatalogChanged -> state = state.copy(showCatalog = event.show)
            is WhiteLabelEvent.OnShowThemeSelectorChanged -> state = state.copy(showThemeSelector = event.show)
            is WhiteLabelEvent.OnShowPageSettingsChanged -> state = state.copy(showPageSettings = event.show)
            is WhiteLabelEvent.OnShowThemeLabChanged -> state = state.copy(showThemeLab = event.show)
            is WhiteLabelEvent.OnEditingComponentIndexChanged -> state = state.copy(editingComponentIndex = event.index)
            is WhiteLabelEvent.OnPendingNewComponentChanged -> state = state.copy(pendingNewComponent = event.component)
            is WhiteLabelEvent.OnFilterQueryChanged -> state = state.copy(filterQuery = event.query)
            is WhiteLabelEvent.OnImageUploadStarted -> state = state.copy(isUploading = event.isUploading)

            is WhiteLabelEvent.OnDeleteComponent -> {
                val newList = state.page.components.toMutableList().apply { removeAt(event.index) }
                onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                if (state.editingComponentIndex == event.index) {
                    state = state.copy(editingComponentIndex = null)
                }
            }
            is WhiteLabelEvent.OnDuplicateComponent -> {
                val component = state.page.components[event.index]
                val newList = state.page.components.toMutableList().apply { add(event.index + 1, component) }
                onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
            }
            is WhiteLabelEvent.OnMoveComponentUp -> {
                if (event.index > 0) {
                    val newList = state.page.components.toMutableList()
                    val temp = newList[event.index]
                    newList[event.index] = newList[event.index - 1]
                    newList[event.index - 1] = temp
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (state.editingComponentIndex == event.index) {
                        state = state.copy(editingComponentIndex = event.index - 1)
                    } else if (state.editingComponentIndex == event.index - 1) {
                        state = state.copy(editingComponentIndex = event.index)
                    }
                }
            }
            is WhiteLabelEvent.OnMoveComponentDown -> {
                if (event.index < state.page.components.size - 1) {
                    val newList = state.page.components.toMutableList()
                    val temp = newList[event.index]
                    newList[event.index] = newList[event.index + 1]
                    newList[event.index + 1] = temp
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (state.editingComponentIndex == event.index) {
                        state = state.copy(editingComponentIndex = event.index + 1)
                    } else if (state.editingComponentIndex == event.index + 1) {
                        state = state.copy(editingComponentIndex = event.index)
                    }
                }
            }
        }
    }

    val allProducts by viewModel.allAvailableProducts.collectAsState()

    AppTheme(themeConfig = state.page.theme, customTheme = state.page.customTheme) {
        BrandingEffects(page = state.page)
        WhiteLabelContent(
            state = state,
            viewModel = viewModel,
            onEvent = ::onEvent,
            originalPage = pristinePage,
            displayCategories = effectiveCategories,
            allProducts = allProducts,
            onManageCategories = { showCategoryManagement = true },
            onPickImage = { imagePicker() },
            onDiscardClicked = { showDiscardDialog = true },
        )

        if (state.showThemeSelector) {
            ThemeSelectorBottomSheet(
                currentTheme = state.page.theme,
                onThemeSelected = { newTheme ->
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(theme = newTheme)))
                    state = state.copy(showThemeSelector = false)
                },
                onDismiss = { state = state.copy(showThemeSelector = false) },
            )
        }

        if (showCategoryManagement) {
            CategoryManagementDialog(
                viewModel = viewModel,
                onDismiss = { showCategoryManagement = false },
            )
        }

        if (state.showThemeLab) {
            ThemeLabDialog(
                initialConfig = state.page.customTheme,
                onSave = { newConfig ->
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(customTheme = newConfig)))
                    onEvent(WhiteLabelEvent.OnShowThemeLabChanged(false))
                },
                onDismiss = { onEvent(WhiteLabelEvent.OnShowThemeLabChanged(false)) },
            )
        }

        if (showDiscardDialog) {
            GenesysConfirmDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = GenesysStrings.DiscardDraft,
                text = "Tem certeza que deseja descartar todas as alterações não publicadas?",
                icon = GenesysIcons.Delete,
                confirmButton = {
                    GenesysLoadingButton(
                        text = "Descartar",
                        onClick = {
                            viewModel.clearDraft(state.page.id)
                            onEvent(WhiteLabelEvent.OnPageUpdated(pristinePage))
                            showDiscardDialog = false
                        },
                        containerColor = MaterialTheme.colorScheme.error,
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Cancelar")
                    }
                },
            )
        }

        if (state.showCatalog) {
            ComponentCatalogUI(state, ::onEvent)
        }
    }
}

@Composable
fun ComponentCatalogUI(
    state: WhiteLabelState,
    onEvent: (WhiteLabelEvent) -> Unit,
) {
    GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(false)) },
        title = "Adicionar Novo Bloco",
    ) {
        GenesysColumn(usePadding = true) {
            val catalogItems =
                listOf(
                    CatalogItem("Cabeçalho", GenesysIcons.Description) { PageComponent.Header(title = "Novo Cabeçalho") },
                    CatalogItem("Texto", GenesysIcons.Edit) { PageComponent.Text(content = "Seu texto aqui...") },
                    CatalogItem("Imagem", GenesysIcons.Image) { PageComponent.Image(url = "https://picsum.photos/800/400") },
                    CatalogItem("Botão", GenesysIcons.Language) { PageComponent.Button(text = "Clique Aqui", url = "https://") },
                    CatalogItem("Lista de Produtos", GenesysIcons.Inventory) { PageComponent.ProductList(products = emptyList()) },
                    CatalogItem("Grade de Produtos", GenesysIcons.Inventory) { PageComponent.ProductGrid(productIds = emptyList()) },
                    CatalogItem("Barra de Busca", GenesysIcons.Search) { PageComponent.Filter() },
                    CatalogItem("Filtro de Categorias", GenesysIcons.Category) { PageComponent.CategoryFilter() },
                    CatalogItem("Perfil", GenesysIcons.Person) { PageComponent.ProfileHeader(imageUrl = "", name = "Seu Nome") },
                    CatalogItem("Links Sociais", GenesysIcons.Share) { PageComponent.SocialLinks() },
                    CatalogItem("Carrinho", GenesysIcons.ShoppingBag) { PageComponent.CartComponent() },
                    CatalogItem("Rastreio", GenesysIcons.List) { PageComponent.OrderTrackingComponent() },
                    CatalogItem("Lista de Serviços", GenesysIcons.Schedule) { PageComponent.ServiceList() },
                )

            catalogItems.chunked(2).forEach { rowItems ->
                GenesysRow(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { item ->
                        GenesysWeightBox(1f) {
                            GenesysCard(
                                onClick = {
                                    val newList = state.page.components + item.createComponent()
                                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                                    onEvent(WhiteLabelEvent.OnShowCatalogChanged(false))
                                },
                                modifier = Modifier.height(100.dp),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(8.dp))
                                    Text(item.name, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    if (rowItems.size < 2) Spacer(Modifier.weight(weight = 1f))
                }
                GenesysSpacer(GenesysSpacing.Small)
            }
        }
    }
}

data class CatalogItem(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val createComponent: () -> PageComponent,
)
