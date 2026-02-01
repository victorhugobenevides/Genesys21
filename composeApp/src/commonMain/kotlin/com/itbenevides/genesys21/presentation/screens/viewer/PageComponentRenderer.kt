package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.ui.components.badge.GenesysBadge
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.input.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.input.GenesysSearchBar
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.util.AnalyticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PageComponentRenderer(
    component: PageComponent,
    onProductClick: ((Product) -> Unit)? = null,
    filterQuery: String = "",
    onFilterQueryChange: (String) -> Unit = {},
    allAvailableCategories: List<String> = emptyList(),
    isEditMode: Boolean = false,
    onEditClick: (() -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val router: Router = koinInject()
    val backendUrl = remember { getBaseUrl() }
    
    val isCategoryFilterActive = allAvailableCategories.any { it.equals(filterQuery, ignoreCase = true) }

    val shouldShow = if (filterQuery.isBlank() || !component.isFilterable) {
        true
    } else {
        when (component) {
            is PageComponent.ProductList -> {
                if (isCategoryFilterActive) {
                    component.products.any { it.categoryName?.equals(filterQuery, ignoreCase = true) == true }
                } else {
                    component.products.any { 
                        it.name.contains(filterQuery, ignoreCase = true) || 
                        (it.categoryName?.contains(filterQuery, ignoreCase = true) == true)
                    }
                }
            }
            is PageComponent.Header -> !isCategoryFilterActive && component.title.contains(filterQuery, ignoreCase = true)
            is PageComponent.Text -> !isCategoryFilterActive && (component as? PageComponent.Text)?.content?.contains(filterQuery, ignoreCase = true) == true
            is PageComponent.Image -> !isCategoryFilterActive && component.url.contains(filterQuery, ignoreCase = true)
            else -> true
        }
    }

    if (!shouldShow && component !is PageComponent.Filter && component !is PageComponent.CategoryFilter) return

    GenesysBox(modifier = Modifier.fillMaxWidth()) {
        when (component) {
            is PageComponent.CategoryFilter -> {
                GenesysColumn(usePadding = true) {
                    GenesysText(
                        text = GenesysStrings.Categories,
                        style = GenesysTextStyle.Label,
                        fontWeight = GenesysFontWeight.Bold
                    )
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysRow(modifier = Modifier.fillMaxWidth(), useHorizontalScroll = true) {
                        GenesysFilterChip(
                            selected = filterQuery.isEmpty(),
                            onClick = { onFilterQueryChange("") },
                            label = GenesysStrings.All
                        )
                        
                        allAvailableCategories.forEach { category ->
                            GenesysSpacer(GenesysSpacing.Small)
                            GenesysFilterChip(
                                selected = filterQuery.equals(category, ignoreCase = true),
                                onClick = { 
                                    if (filterQuery.equals(category, ignoreCase = true)) {
                                        onFilterQueryChange("")
                                    } else {
                                        onFilterQueryChange(category)
                                        AnalyticsManager.logEvent("select_category", mapOf("item_id" to category))
                                    }
                                },
                                label = category
                            )
                        }
                    }
                }
            }
            is PageComponent.Filter -> {
                GenesysBox(Modifier.padding(vertical = GenesysDimens.SpacingMedium)) {
                    GenesysSearchBar(
                        value = filterQuery,
                        onValueChange = { onFilterQueryChange(it) },
                        placeholder = component.placeholder,
                        onClear = { onFilterQueryChange("") }
                    )
                }
            }
            is PageComponent.ProductList -> {
                val productsToDisplay = if (filterQuery.isBlank() || !component.isFilterable) {
                    component.products
                } else {
                    if (isCategoryFilterActive) {
                        component.products.filter { it.categoryName?.equals(filterQuery, ignoreCase = true) == true }
                    } else {
                        component.products.filter { 
                            it.name.contains(filterQuery, ignoreCase = true) || 
                            (it.categoryName?.contains(filterQuery, ignoreCase = true) == true)
                        }
                    }
                }

                if (productsToDisplay.isNotEmpty()) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        val maxColumns = if (maxWidth > 900.dp) 4 else if (maxWidth > 600.dp) 3 else 2
                        val horizontalItemWidth = if (maxWidth > 900.dp) 220.dp else if (maxWidth > 600.dp) 180.dp else 160.dp

                        GenesysColumn(usePadding = false) {
                            if (component.isHorizontal) {
                                val listState = rememberLazyListState()
                                GenesysBox(modifier = Modifier.fillMaxWidth()) {
                                    LazyRow(
                                        state = listState,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        items(productsToDisplay) { product ->
                                            ProductCard(
                                                product = product,
                                                modifier = Modifier.width(horizontalItemWidth),
                                                onClick = onProductClick,
                                                onAddToCart = { router.viewModel.addToCart(product) },
                                                isEditMode = isEditMode
                                            )
                                        }
                                    }
                                }
                            } else {
                                productsToDisplay.chunked(maxColumns).forEach { rowProducts ->
                                    GenesysRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        rowProducts.forEach { product ->
                                            GenesysWeightBox(1f) {
                                                ProductCard(
                                                    product = product,
                                                    onClick = onProductClick,
                                                    onAddToCart = { router.viewModel.addToCart(product) },
                                                    isEditMode = isEditMode
                                                )
                                            }
                                        }
                                        if (rowProducts.size < maxColumns) {
                                            repeat(maxColumns - rowProducts.size) { GenesysWeightSpacer(1f) }
                                        }
                                    }
                                    GenesysSpacer(GenesysSpacing.Medium)
                                }
                            }
                        }
                    }
                }
            }
            is PageComponent.Header -> {
                GenesysText(
                    text = if (component.isUppercase) component.title.uppercase().ifBlank { GenesysStrings.UpdateTitle } else component.title.ifBlank { GenesysStrings.UpdateTitle }, 
                    style = GenesysTextStyle.Headline,
                    fontWeight = when(component.fontWeight) {
                        "BOLD" -> GenesysFontWeight.Bold
                        "EXTRA_BOLD" -> GenesysFontWeight.ExtraBold
                        else -> GenesysFontWeight.Normal
                    },
                    textAlign = when(component.textAlign) {
                        "CENTER" -> GenesysTextAlign.Center
                        "RIGHT" -> GenesysTextAlign.End
                        else -> GenesysTextAlign.Start
                    },
                    color = if (component.usePrimaryColor) MaterialTheme.colorScheme.primary else Color.Unspecified,
                    fontSize = component.fontSize.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp)
                )
            }
            is PageComponent.Text -> {
                GenesysText(
                    text = if (component.isUppercase) component.content.uppercase().ifBlank { GenesysStrings.Description } else component.content.ifBlank { GenesysStrings.Description }, 
                    style = GenesysTextStyle.Body,
                    fontWeight = when(component.fontWeight) {
                        "BOLD" -> GenesysFontWeight.Bold
                        else -> GenesysFontWeight.Normal
                    },
                    textAlign = when(component.textAlign) {
                        "CENTER" -> GenesysTextAlign.Center
                        "RIGHT" -> GenesysTextAlign.End
                        else -> GenesysTextAlign.Start
                    },
                    fontSize = component.fontSize.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
            is PageComponent.Image -> {
                val scope = rememberCoroutineScope()
                // CORREÇÃO: Resolução de URL para imagens carregadas no servidor
                val displayUrl = remember(component.url, backendUrl) {
                    if (component.url.startsWith("/") && !component.url.startsWith("http")) "$backendUrl${component.url}" else component.url
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (component.isFullWidth) 0.dp else 16.dp), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GenesysCard(
                        onClick = {
                            if (isEditMode) {
                                onEditClick?.invoke()
                                return@GenesysCard
                            }
                            val destId = component.destinationPageId
                            if (!destId.isNullOrBlank()) {
                                scope.launch {
                                    router.viewModel.loadPublicPage(destId)?.let { targetPage ->
                                        router.navigateTo(Route.PublicViewer(targetPage))
                                    }
                                }
                            } else if (displayUrl.startsWith("http")) {
                                uriHandler.openUri(displayUrl)
                            }
                        },
                        elevation = 0.dp,
                        backgroundColor = Color.Transparent,
                        modifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()
                    ) {
                        GenesysImage(
                            url = displayUrl,
                            size = component.size.dp,
                            isCircular = component.isCircular,
                            modifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            else -> {}
        }

        if (isEditMode && component !is PageComponent.ProductList) {
            GenesysBox(Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                GenesysIconButton(
                    icon = GenesysIcons.Edit,
                    onClick = { onEditClick?.invoke() },
                    tint = Color.White,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: ((Product) -> Unit)? = null,
    onAddToCart: (() -> Unit)? = null,
    isEditMode: Boolean = false
) {
    var isAdded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val backendUrl = remember { getBaseUrl() }
    
    val scale by animateFloatAsState(
        targetValue = if (isAdded) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    GenesysCard(
        modifier = modifier,
        onClick = if (onClick != null) { { onClick.invoke(product) } } else null,
        elevation = 2.dp
    ) {
        GenesysColumn(usePadding = false) {
            GenesysBox(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = remember(product.imageUrls) {
                    val first = product.imageUrls.firstOrNull() ?: ""
                    if (first.startsWith("/")) "$backendUrl$first" else first
                }
                
                GenesysImage(
                    url = imageUrl,
                    size = 200.dp,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (isEditMode) {
                    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.TopEnd) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White,
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(GenesysIcons.Edit, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                } else if (onAddToCart != null && product.stock > 0) {
                    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(40.dp).scale(scale),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (isAdded) Color(0xFF388E3C) else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                GenesysIconButton(
                                    icon = if (isAdded) GenesysIcons.Check else GenesysIcons.Add,
                                    onClick = {
                                        if (!isAdded) {
                                            isAdded = true
                                            onAddToCart()
                                            scope.launch { delay(800); isAdded = false }
                                        }
                                    },
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            GenesysColumn(modifier = Modifier.padding(12.dp), usePadding = false) {
                GenesysText(
                    text = product.name, 
                    fontWeight = GenesysFontWeight.Bold, 
                    modifier = Modifier.fillMaxWidth()
                )
                
                GenesysSpacer(GenesysSpacing.Small)
                
                GenesysRow(horizontalArrangement = Arrangement.SpaceBetween) {
                    GenesysText(
                        text = "${GenesysStrings.PricePrefix}${product.price}", 
                        fontWeight = GenesysFontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        weightValue = 1f
                    )
                    
                    if (product.stock <= 0) {
                        GenesysBadge(label = "ESGOTADO", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
