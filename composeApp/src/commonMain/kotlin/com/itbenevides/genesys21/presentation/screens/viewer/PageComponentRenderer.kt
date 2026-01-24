package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.scale
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
import com.itbenevides.genesys21.util.AnalyticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
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
    val commonShape = RoundedCornerShape(16.dp)
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
            is PageComponent.Image -> !isCategoryFilterActive && (component as PageComponent.Image).url.contains(filterQuery, ignoreCase = true)
            is PageComponent.Button -> !isCategoryFilterActive && component.text.contains(filterQuery, ignoreCase = true)
            else -> true
        }
    }

    if (!shouldShow && component !is PageComponent.Filter && component !is PageComponent.CategoryFilter) return

    Box(modifier = Modifier.fillMaxWidth()) {
        when (component) {
            is PageComponent.CategoryFilter -> {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Text(
                        text = "Categorias",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = filterQuery.isEmpty(),
                            onClick = { onFilterQueryChange("") },
                            label = { Text("Todos") },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = Color.Transparent
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filterQuery.isEmpty(), borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                        
                        allAvailableCategories.forEach { category ->
                            FilterChip(
                                selected = filterQuery.equals(category, ignoreCase = true),
                                onClick = { 
                                    if (filterQuery.equals(category, ignoreCase = true)) {
                                        onFilterQueryChange("")
                                    } else {
                                        onFilterQueryChange(category)
                                        AnalyticsManager.logEvent("select_content", mapOf(
                                            "content_type" to "category",
                                            "item_id" to category
                                        ))
                                    }
                                },
                                label = { Text(category) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = Color.Transparent
                                ),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filterQuery.equals(category, ignoreCase = true), borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
            is PageComponent.Filter -> {
                Box(Modifier.padding(vertical = 16.dp)) {
                    OutlinedTextField(
                        value = filterQuery,
                        onValueChange = { 
                            onFilterQueryChange(it)
                            if (it.length > 3) {
                                AnalyticsManager.logEvent("search", mapOf("search_term" to it))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        textStyle = TextStyle(fontSize = 16.sp),
                        placeholder = { Text(component.placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
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
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                }
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
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        // Ajuste dinâmico de colunas para evitar cards muito largos
                        val maxColumns = if (maxWidth > 900.dp) 4 else if (maxWidth > 600.dp) 3 else 2
                        val horizontalItemWidth = if (maxWidth > 900.dp) 220.dp else if (maxWidth > 600.dp) 180.dp else 160.dp

                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            if (component.isHorizontal) {
                                val listState = rememberLazyListState()
                                val coroutineScope = rememberCoroutineScope()
                                
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    LazyRow(
                                        state = listState,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        items(productsToDisplay) { product ->
                                            val fullUrls = product.imageUrls.map { url ->
                                                if (url.startsWith("/")) "$backendUrl$url" else url
                                            }
                                            ProductCard(
                                                product = product.copy(imageUrls = fullUrls),
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier.width(horizontalItemWidth),
                                                onClick = onProductClick,
                                                onAddToCart = { viewModel.addToCart(product) },
                                                isEditMode = isEditMode
                                            )
                                        }
                                    }
                                    
                                    if (productsToDisplay.size > 1) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            if (listState.firstVisibleItemIndex > 0) {
                                                IconButton(
                                                    onClick = { coroutineScope.launch { listState.animateScrollToItem(listState.firstVisibleItemIndex - 1) } },
                                                    modifier = Modifier.padding(start = 4.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                                                ) {
                                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            } else { Spacer(Modifier.width(48.dp)) }

                                            if (listState.canScrollForward) {
                                                IconButton(
                                                    onClick = { coroutineScope.launch { listState.animateScrollToItem(listState.firstVisibleItemIndex + 1) } },
                                                    modifier = Modifier.padding(end = 4.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                                                ) {
                                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            } else { Spacer(Modifier.width(48.dp)) }
                                        }
                                    }
                                }
                            } else {
                                // LISTA VERTICAL (GRID)
                                productsToDisplay.chunked(maxColumns).forEach { rowProducts ->
                                    Row(
                                        Modifier.fillMaxWidth(), 
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        rowProducts.forEach { product ->
                                            val fullUrls = product.imageUrls.map { url ->
                                                if (url.startsWith("/")) "$backendUrl$url" else url
                                            }
                                            ProductCard(
                                                product = product.copy(imageUrls = fullUrls),
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier.weight(1f).widthIn(max = 250.dp), // LIMITA LARGURA MÁXIMA PARA NÃO ACHATAR
                                                onClick = onProductClick,
                                                onAddToCart = { viewModel.addToCart(product) },
                                                isEditMode = isEditMode
                                            )
                                        }
                                        // Preenche o espaço vazio se a linha não estiver cheia
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
            // ... (restante dos componentes permanece igual)
            is PageComponent.Header -> {
                val alignment = when (component.textAlign) {
                    "CENTER" -> TextAlign.Center
                    "RIGHT" -> TextAlign.Right
                    else -> TextAlign.Start
                }
                Text(
                    text = component.title.ifBlank { "Título" }, 
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = component.fontSize.sp
                    ), 
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = alignment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 8.dp)
                )
            }
            is PageComponent.Text -> {
                val alignment = when (component.textAlign) {
                    "CENTER" -> TextAlign.Center
                    "RIGHT" -> TextAlign.Right
                    else -> TextAlign.Start
                }
                Text(
                    text = component.content.ifBlank { "Conteúdo..." }, 
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = (component.fontSize * 1.5).sp,
                        fontSize = component.fontSize.sp
                    ), 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = alignment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
            is PageComponent.Image -> {
                val scope = rememberCoroutineScope()
                val imgModifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.widthIn(max = 600.dp).wrapContentSize()
                
                val imgShape = when {
                    component.isRounded -> CircleShape
                    component.isFullWidth -> RoundedCornerShape(0.dp)
                    else -> RoundedCornerShape(28.dp)
                }

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = if (component.isFullWidth) 0.dp else 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = imgModifier.clickable {
                            if (isEditMode) {
                                onEditClick?.invoke()
                                return@clickable
                            }
                            val destId = (component as? PageComponent.Image)?.destinationPageId
                            if (!destId.isNullOrBlank()) {
                                scope.launch {
                                    router.viewModel.loadPublicPage(destId)?.let { targetPage ->
                                        if (router.currentRoute is Route.WhiteLabel) router.navigateTo(Route.WhiteLabel(targetPage))
                                        else router.navigateTo(Route.PublicViewer(targetPage))
                                    }
                                }
                            } else {
                                val currentUrl = (component as? PageComponent.Image)?.url ?: ""
                                if (currentUrl.startsWith("http")) {
                                    uriHandler.openUri(currentUrl)
                                    AnalyticsManager.logEvent("click_external_link", mapOf("url" to currentUrl, "type" to "image"))
                                }
                            }
                        },
                        shape = imgShape,
                        color = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (component.url.isNotEmpty()) {
                                val fullImageUrl = if (component.url.startsWith("/")) "$backendUrl${component.url}" else component.url
                                AsyncImage(
                                    model = fullImageUrl,
                                    contentDescription = null,
                                    modifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.size(component.size.dp).clip(imgShape),
                                    contentScale = if (component.isFullWidth) ContentScale.FillWidth else ContentScale.Crop
                                )
                            } else {
                                Box(Modifier.size(component.size.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), imgShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                }
                            }
                        }
                    }
                }
            }
            is PageComponent.Button -> {
                Box(Modifier.padding(vertical = 12.dp)) {
                    Button(
                        onClick = { 
                            if (isEditMode) {
                                onEditClick?.invoke()
                                return@Button
                            }
                            uriHandler.openUri(component.url)
                            AnalyticsManager.logEvent("click_external_link", mapOf(
                                "url" to component.url,
                                "button_text" to component.text,
                                "icon" to (component.iconName ?: "none")
                            ))
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        elevation = null
                    ) {
                        val icon = when (component.iconName?.lowercase()) {
                            "whatsapp" -> Icons.AutoMirrored.Filled.Chat
                            "instagram" -> Icons.Default.CameraAlt
                            else -> null
                        }
                        if (icon != null) { Icon(icon, null); Spacer(Modifier.width(12.dp)) }
                        Text(component.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
            }
            else -> {}
        }

        if (isEditMode && component !is PageComponent.ProductList) {
            IconButton(
                onClick = { onEditClick?.invoke() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                    .size(32.dp)
            ) {
                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    onClick: ((Product) -> Unit)? = null,
    onAddToCart: (() -> Unit)? = null,
    isEditMode: Boolean = false
) {
    var isAdded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val scale by animateFloatAsState(
        targetValue = if (isAdded) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Card(
        modifier = modifier.clickable(enabled = onClick != null) { 
            onClick?.invoke(product)
            AnalyticsManager.logEvent("select_item", mapOf(
                "item_id" to product.id,
                "item_name" to product.name,
                "item_category" to product.category
            ))
        },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) 
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop 
                    )
                } else {
                    Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.size(56.dp))
                }
                
                if (isEditMode) {
                    Box(Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.TopEnd) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White,
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                } else if (onAddToCart != null && product.stock > 0) {
                    Box(Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            onClick = {
                                if (!isAdded) {
                                    isAdded = true
                                    onAddToCart()
                                    scope.launch {
                                        delay(800)
                                        isAdded = false
                                    }
                                }
                            },
                            modifier = Modifier.size(40.dp).scale(scale),
                            shape = CircleShape,
                            color = if (isAdded) Color(0xFF388E3C) else MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            tonalElevation = 4.dp,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AnimatedContent(
                                    targetState = isAdded,
                                    transitionSpec = {
                                        scaleIn() togetherWith scaleOut()
                                    }
                                ) { added ->
                                    if (added) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(24.dp))
                                    } else {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = product.name, 
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "R$ ${product.price}", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    if (product.stock <= 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "ESGOTADO", 
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else if (product.stock < 5) {
                        Text(
                            "Só ${product.stock} un!", 
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
