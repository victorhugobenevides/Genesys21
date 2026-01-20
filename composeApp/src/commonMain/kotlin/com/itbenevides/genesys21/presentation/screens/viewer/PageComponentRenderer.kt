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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val commonShape = if (component.isRounded) CircleShape else RoundedCornerShape(8.dp)
    val uriHandler = LocalUriHandler.current
    val router: Router = koinInject()
    val viewModel = router.viewModel
    val scope = rememberCoroutineScope()
    
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Categorias",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
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
                        shape = if (component.isRounded) CircleShape else RoundedCornerShape(8.dp)
                    )
                    
                    allAvailableCategories.forEach { category ->
                        FilterChip(
                            selected = filterQuery.equals(category, ignoreCase = true),
                            onClick = { 
                                if (filterQuery.equals(category, ignoreCase = true)) onFilterQueryChange("")
                                else onFilterQueryChange(category)
                            },
                            label = { Text(category) },
                            shape = if (component.isRounded) CircleShape else RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
        is PageComponent.Filter -> {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = onFilterQueryChange,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                placeholder = { Text(component.placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (filterQuery.isNotEmpty()) {
                        IconButton(onClick = { onFilterQueryChange("") }) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = if (component.isRounded) CircleShape else RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (component.isHorizontal) {
                            val listState = rememberLazyListState()
                            val coroutineScope = rememberCoroutineScope()
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                LazyRow(
                                    state = listState,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(productsToDisplay) { product ->
                                        ProductCard(
                                            product = product,
                                            shape = commonShape,
                                            isTransparent = component.isTransparent,
                                            modifier = Modifier.width(160.dp),
                                            onClick = onProductClick,
                                            onAddToCart = { 
                                                viewModel.addToCart(product)
                                            }
                                        )
                                    }
                                }

                                if (productsToDisplay.size > 1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (listState.firstVisibleItemIndex > 0) {
                                            FilledIconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        listState.animateScrollToItem(maxOf(0, listState.firstVisibleItemIndex - 1))
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp).padding(start = 4.dp),
                                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        } else { Spacer(Modifier.width(32.dp)) }

                                        if (listState.canScrollForward) {
                                            FilledIconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        listState.animateScrollToItem(listState.firstVisibleItemIndex + 1)
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp).padding(end = 4.dp),
                                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        } else { Spacer(Modifier.width(32.dp)) }
                                    }
                                }
                            }
                        } else {
                            productsToDisplay.chunked(maxColumns).forEach { rowProducts ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    rowProducts.forEach { product ->
                                        ProductCard(
                                            product = product,
                                            shape = commonShape,
                                            isTransparent = component.isTransparent,
                                            modifier = Modifier.weight(1f),
                                            onClick = onProductClick,
                                            onAddToCart = { 
                                                viewModel.addToCart(product)
                                            }
                                        )
                                    }
                                    if (rowProducts.size < maxColumns) {
                                        repeat(maxColumns - rowProducts.size) {
                                            Spacer(Modifier.weight(1f))
                                        }
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
                style = MaterialTheme.typography.headlineMedium, 
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        is PageComponent.Text -> {
            Text(
                text = component.content.ifBlank { "Conteúdo..." }, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        is PageComponent.Image -> {
            val scope = rememberCoroutineScope()
            
            val modifier = if (component.isFullWidth) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.wrapContentSize().padding(horizontal = 16.dp)
            }

            val imgShape = when {
                component.isRounded -> CircleShape
                component.isFullWidth -> RoundedCornerShape(0.dp)
                else -> RoundedCornerShape(12.dp)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = modifier.clickable {
                        val destId = (component as? PageComponent.Image)?.destinationPageId
                        if (!destId.isNullOrBlank()) {
                            scope.launch {
                                router.viewModel.loadPublicPage(destId)?.let { targetPage ->
                                    if (router.currentRoute is Route.WhiteLabel) {
                                        router.navigateTo(Route.WhiteLabel(targetPage))
                                    } else {
                                        router.navigateTo(Route.PublicViewer(targetPage))
                                    }
                                }
                            }
                        } else {
                            val currentUrl = (component as? PageComponent.Image)?.url ?: ""
                            if (currentUrl.startsWith("http")) {
                                uriHandler.openUri(currentUrl)
                            }
                        }
                    },
                    shape = imgShape,
                    color = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    Box(Modifier.padding(if (component.url.isNotEmpty() || component.isFullWidth) 0.dp else 20.dp), contentAlignment = Alignment.Center) {
                        if (component.url.isNotEmpty()) {
                            AsyncImage(
                                model = component.url,
                                contentDescription = null,
                                modifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.size(component.size.dp).clip(imgShape),
                                contentScale = if (component.isFullWidth) ContentScale.FillWidth else ContentScale.Fit
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(component.size.dp)
                            )
                        }
                    }
                }
                if (component.string.isNotEmpty() && !component.isFullWidth) {
                    Text(
                        text = component.string,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        is PageComponent.Button -> {
            val btnShape = if (component.isRounded) CircleShape else RoundedCornerShape(12.dp)
            Button(
                onClick = { uriHandler.openUri(component.url) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = btnShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.primary,
                    contentColor = if (component.isTransparent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                ),
                border = if (component.isTransparent) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (component.iconName?.lowercase()) {
                        "phone" -> Icons.Default.Phone
                        "email" -> Icons.Default.Email
                        "web" -> Icons.Default.Public
                        "whatsapp" -> Icons.Default.Chat
                        "instagram" -> Icons.Default.CameraAlt
                        "linkedin" -> Icons.Default.BusinessCenter
                        else -> null
                    }
                    if (icon != null) {
                        Icon(icon, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(component.text, fontWeight = FontWeight.Bold)
                }
            }
        }
        is PageComponent.Unknown -> {
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.QuestionMark, null, tint = MaterialTheme.colorScheme.outline)
            }
        }
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
    Surface(
        modifier = modifier.let { m -> 
            if (onClick != null) m.clickable { onClick(product) } 
            else m 
        },
        shape = shape,
        color = if (isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (isTransparent) null else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Container da imagem com tamanho e proporção garantidos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Mantém o Box quadrado
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
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
                    Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
                }
                
                if (onAddToCart != null && product.stock > 0) {
                    Box(modifier = Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.BottomEnd) {
                        FilledIconButton(
                            onClick = { onAddToCart() },
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            
            // Textos fora do Box da imagem para garantir o fluxo vertical
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = product.name, 
                style = MaterialTheme.typography.labelLarge, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis, 
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "R$ ${product.price}", 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                if (product.stock > 0) {
                    Text(
                        text = "${product.stock} un", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Esgotado", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
