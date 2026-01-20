package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.di.getBaseUrl
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PageComponentRenderer(
    component: PageComponent,
    onProductClick: ((Product) -> Unit)? = null,
    filterQuery: String = "",
    onFilterQueryChange: (String) -> Unit = {},
    allAvailableCategories: List<String> = emptyList()
) {
    val commonShape = if (component.isRounded) CircleShape else RoundedCornerShape(16.dp)
    val uriHandler = LocalUriHandler.current
    val router: Router = koinInject()
    val viewModel = router.viewModel
    val scope = rememberCoroutineScope()
    val backendUrl = remember { getBaseUrl() }
    
    val isCategoryFilterActive = allAvailableCategories.any { it.equals(filterQuery, ignoreCase = true) }

    val shouldShow = if (filterQuery.isBlank() || !component.isFilterable) {
        true
    } else {
        when (component) {
            is PageComponent.ProductList -> {
                if (isCategoryFilterActive) {
                    component.products.any { it.category.equals(filterQuery, ignoreCase = true) }
                } else {
                    component.products.any { 
                        it.name.contains(filterQuery, ignoreCase = true) || 
                        it.category.contains(filterQuery, ignoreCase = true) 
                    }
                }
            }
            is PageComponent.Header -> !isCategoryFilterActive && component.title.contains(filterQuery, ignoreCase = true)
            is PageComponent.Text -> !isCategoryFilterActive && component.content.contains(filterQuery, ignoreCase = true)
            is PageComponent.Image -> !isCategoryFilterActive && component.url.contains(filterQuery, ignoreCase = true)
            is PageComponent.Button -> !isCategoryFilterActive && component.text.contains(filterQuery, ignoreCase = true)
            else -> true
        }
    }

    if (!shouldShow && component !is PageComponent.Filter && component !is PageComponent.CategoryFilter) return

    when (component) {
        is PageComponent.CategoryFilter -> {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                    text = "Categorias",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterQuery.isEmpty(),
                        onClick = { onFilterQueryChange("") },
                        label = { Text("Todos") },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    
                    allAvailableCategories.forEach { category ->
                        FilterChip(
                            selected = filterQuery.equals(category, ignoreCase = true),
                            onClick = { 
                                if (filterQuery.equals(category, ignoreCase = true)) onFilterQueryChange("")
                                else onFilterQueryChange(category)
                            },
                            label = { Text(category) },
                            shape = CircleShape
                        )
                    }
                }
            }
        }
        is PageComponent.Filter -> {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = onFilterQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(if (component.isTransparent) 0.dp else 2.dp, CircleShape),
                textStyle = TextStyle(fontSize = 15.sp),
                placeholder = { Text(component.placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (filterQuery.isNotEmpty()) {
                        IconButton(onClick = { onFilterQueryChange("") }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent // Bordas invisíveis para look moderno
                )
            )
        }
        is PageComponent.ProductList -> {
            val productsToDisplay = if (filterQuery.isBlank() || !component.isFilterable) {
                component.products
            } else {
                if (isCategoryFilterActive) {
                    component.products.filter { it.category.equals(filterQuery, ignoreCase = true) }
                } else {
                    component.products.filter { 
                        it.name.contains(filterQuery, ignoreCase = true) || 
                        it.category.contains(filterQuery, ignoreCase = true)
                    }
                }
            }

            if (productsToDisplay.isNotEmpty()) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val maxColumns = if (maxWidth > 800.dp) 3 else 2
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (component.isHorizontal) {
                            val listState = rememberLazyListState()
                            val coroutineScope = rememberCoroutineScope()
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                LazyRow(
                                    state = listState,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    items(productsToDisplay) { product ->
                                        val fullImageUrl = if (product.imageUrl.startsWith("/")) "$backendUrl${product.imageUrl}" else product.imageUrl
                                        ProductCard(
                                            product = product.copy(imageUrl = fullImageUrl),
                                            shape = RoundedCornerShape(20.dp),
                                            isTransparent = component.isTransparent,
                                            modifier = Modifier.width(180.dp),
                                            onClick = onProductClick,
                                            onAddToCart = { viewModel.addToCart(product) }
                                        )
                                    }
                                }
                                
                                // Setas de navegação mais elegantes
                                if (productsToDisplay.size > 1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (listState.firstVisibleItemIndex > 0) {
                                            IconButton(
                                                onClick = { coroutineScope.launch { listState.animateScrollToItem(listState.firstVisibleItemIndex - 1) } },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                                            }
                                        } else { Spacer(Modifier.width(40.dp)) }

                                        if (listState.canScrollForward) {
                                            IconButton(
                                                onClick = { coroutineScope.launch { listState.animateScrollToItem(listState.firstVisibleItemIndex + 1) } },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                                            }
                                        } else { Spacer(Modifier.width(40.dp)) }
                                    }
                                }
                            }
                        } else {
                            productsToDisplay.chunked(maxColumns).forEach { rowProducts ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    rowProducts.forEach { product ->
                                        val fullImageUrl = if (product.imageUrl.startsWith("/")) "$backendUrl${product.imageUrl}" else product.imageUrl
                                        ProductCard(
                                            product = product.copy(imageUrl = fullImageUrl),
                                            shape = RoundedCornerShape(20.dp),
                                            isTransparent = component.isTransparent,
                                            modifier = Modifier.weight(1f),
                                            onClick = onProductClick,
                                            onAddToCart = { viewModel.addToCart(product) }
                                        )
                                    }
                                    if (rowProducts.size < maxColumns) {
                                        repeat(maxColumns - rowProducts.size) { Spacer(Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        is PageComponent.Header -> {
            Text(
                text = component.title.ifBlank { "Título" }, 
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold), 
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
        is PageComponent.Text -> {
            Text(
                text = component.content.ifBlank { "Conteúdo..." }, 
                style = MaterialTheme.typography.bodyLarge, 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        is PageComponent.Image -> {
            val scope = rememberCoroutineScope()
            val imgModifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.wrapContentSize().padding(16.dp)
            val imgShape = if (component.isRounded) CircleShape else RoundedCornerShape(if (component.isFullWidth) 0.dp else 24.dp)

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = imgModifier.clickable {
                        val destId = (component as? PageComponent.Image)?.destinationPageId
                        if (!destId.isNullOrBlank()) {
                            scope.launch {
                                router.viewModel.loadPublicPage(destId)?.let { targetPage ->
                                    if (router.currentRoute is Route.WhiteLabel) router.navigateTo(Route.WhiteLabel(targetPage))
                                    else router.navigateTo(Route.PublicViewer(targetPage))
                                }
                            }
                        }
                    },
                    shape = imgShape,
                    color = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    tonalElevation = if (component.isTransparent) 0.dp else 2.dp
                ) {
                    Box(Modifier.padding(if (component.url.isNotEmpty() || component.isFullWidth) 0.dp else 40.dp), contentAlignment = Alignment.Center) {
                        if (component.url.isNotEmpty()) {
                            val fullImageUrl = if (component.url.startsWith("/")) "$backendUrl${component.url}" else component.url
                            AsyncImage(
                                model = fullImageUrl,
                                contentDescription = null,
                                modifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.size(component.size.dp).clip(imgShape),
                                contentScale = if (component.isFullWidth) ContentScale.FillWidth else ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.size(component.size.dp))
                        }
                    }
                }
            }
        }
        is PageComponent.Button -> {
            Button(
                onClick = { uriHandler.openUri(component.url) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                val icon = when (component.iconName?.lowercase()) {
                    "whatsapp" -> Icons.AutoMirrored.Filled.Chat
                    "instagram" -> Icons.Default.CameraAlt
                    else -> null
                }
                if (icon != null) { Icon(icon, null); Spacer(Modifier.width(8.dp)) }
                Text(component.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        else -> {}
    }
}

@Composable
fun ProductCard(
    product: Product,
    shape: androidx.compose.ui.graphics.Shape,
    isTransparent: Boolean,
    modifier: Modifier = Modifier,
    onClick: ((Product) -> Unit)? = null,
    onAddToCart: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke(product) },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if (isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTransparent) 0.dp else 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                }
                
                // Botão de adicionar mais elegante
                if (onAddToCart != null && product.stock > 0) {
                    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.BottomEnd) {
                        SmallFloatingActionButton(
                            onClick = onAddToCart,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = product.name, 
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "R$ ${product.price}", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), 
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (product.stock <= 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ESGOTADO", 
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    } else if (product.stock < 5) {
                        Text("Só ${product.stock} un!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
