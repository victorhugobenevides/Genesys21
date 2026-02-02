package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.ui.components.badge.GenesysBadge
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
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
import kotlin.math.roundToLong

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
    val scope = rememberCoroutineScope()
    
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

    Box(modifier = Modifier.fillMaxWidth()) {
        when (component) {
            is PageComponent.ProfileHeader -> {
                val displayUrl = remember(component.imageUrl, backendUrl) {
                    if (component.imageUrl.startsWith("/") && !component.imageUrl.startsWith("http")) "$backendUrl${component.imageUrl}" else component.imageUrl
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GenesysImage(
                        url = displayUrl,
                        size = component.imageSize.dp,
                        isCircular = component.isCircular
                    )
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysText(
                        text = component.name, 
                        style = GenesysTextStyle.Headline, 
                        fontWeight = GenesysFontWeight.ExtraBold,
                        textAlign = GenesysTextAlign.Center
                    )
                    if (component.bio.isNotBlank()) {
                        GenesysText(
                            text = component.bio, 
                            style = GenesysTextStyle.Body, 
                            textAlign = GenesysTextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
            is PageComponent.SocialLinks -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    component.instagram?.let { url ->
                        SocialLinkItem(icon = GenesysIcons.Instagram, label = "Instagram", onClick = { if(!isEditMode) uriHandler.openUri(url) })
                        Spacer(Modifier.width(16.dp))
                    }
                    component.whatsapp?.let { url ->
                        SocialLinkItem(icon = GenesysIcons.WhatsApp, label = "WhatsApp", onClick = { if(!isEditMode) uriHandler.openUri(url) })
                        Spacer(Modifier.width(16.dp))
                    }
                    component.youtube?.let { url ->
                        SocialLinkItem(icon = GenesysIcons.YouTube, label = "YouTube", onClick = { if(!isEditMode) uriHandler.openUri(url) })
                        Spacer(Modifier.width(16.dp))
                    }
                    component.email?.let { email ->
                        SocialLinkItem(icon = GenesysIcons.Email, label = "E-mail", onClick = { if(!isEditMode) uriHandler.openUri("mailto:$email") })
                    }
                }
            }
            is PageComponent.Button -> {
                Box(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    GenesysLoadingButton(
                        text = component.text,
                        onClick = { if(!isEditMode) uriHandler.openUri(component.url) },
                        fillWidth = true
                    )
                }
            }
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
                        val isMobile = maxWidth < 600.dp
                        val maxColumns = if (maxWidth > 900.dp) 4 else if (maxWidth > 600.dp) 3 else 2
                        val horizontalItemWidth = if (maxWidth > 900.dp) 220.dp else if (maxWidth > 600.dp) 180.dp else 150.dp
                        val spacing = if (isMobile) 8.dp else 16.dp

                        GenesysColumn(usePadding = false) {
                            if (component.isHorizontal) {
                                val listState = rememberLazyListState()
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    LazyRow(
                                        state = listState,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(spacing),
                                        contentPadding = PaddingValues(horizontal = if(isMobile) 0.dp else 48.dp, vertical = 8.dp)
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
                                    
                                    // SETAS DE NAVEGAÇÃO HORIZONTAL (Somente Desktop)
                                    if (!isMobile && productsToDisplay.size > 1) {
                                        Surface(
                                            modifier = Modifier.align(Alignment.CenterStart).size(40.dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            tonalElevation = 4.dp
                                        ) {
                                            GenesysIconButton(
                                                icon = GenesysIcons.ArrowLeft, 
                                                onClick = { scope.launch { listState.animateScrollToItem((listState.firstVisibleItemIndex - 1).coerceAtLeast(0)) } }
                                            )
                                        }
                                        Surface(
                                            modifier = Modifier.align(Alignment.CenterEnd).size(40.dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            tonalElevation = 4.dp
                                        ) {
                                            GenesysIconButton(
                                                icon = GenesysIcons.ArrowRight, 
                                                onClick = { scope.launch { listState.animateScrollToItem((listState.firstVisibleItemIndex + 1).coerceAtMost(productsToDisplay.size - 1)) } }
                                            )
                                        }
                                    }
                                }
                            } else {
                                productsToDisplay.chunked(maxColumns).forEach { rowProducts ->
                                    GenesysRow(horizontalArrangement = Arrangement.spacedBy(spacing)) {
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
                                    GenesysSpacer(if (isMobile) GenesysSpacing.Small else GenesysSpacing.Medium)
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

        if (isEditMode) {
            BoxWithConstraints(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
            ) {
                val isMobile = maxWidth < 400.dp
                Surface(
                    onClick = { onEditClick?.invoke() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    contentColor = Color.White,
                    tonalElevation = 4.dp,
                    modifier = Modifier.size(if (isMobile) 32.dp else 36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(GenesysIcons.Edit, null, modifier = Modifier.size(if (isMobile) 16.dp else 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialLinkItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

    BoxWithConstraints(modifier = modifier) {
        val isMobile = maxWidth < 180.dp
        
        GenesysCard(
            onClick = if (onClick != null) { { onClick.invoke(product) } } else null,
            elevation = 1.dp
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
                        Box(Modifier.fillMaxSize().padding(if (isMobile) 4.dp else 8.dp), contentAlignment = Alignment.TopEnd) {
                            Surface(
                                modifier = Modifier.size(if (isMobile) 24.dp else 32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White,
                                tonalElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(GenesysIcons.Edit, null, modifier = Modifier.size(if (isMobile) 12.dp else 16.dp))
                                }
                            }
                        }
                    } else if (onAddToCart != null && product.stock > 0 && product.price > 0) { // Oculta add ao carrinho se for post/feed (preço 0)
                        Box(Modifier.fillMaxSize().padding(if (isMobile) 4.dp else 8.dp), contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(if (isMobile) 32.dp else 40.dp).scale(scale),
                                shape = CircleShape,
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
                
                GenesysColumn(modifier = Modifier.padding(if (isMobile) 8.dp else 12.dp), usePadding = false) {
                    GenesysText(
                        text = product.name, 
                        fontWeight = GenesysFontWeight.Bold,
                        style = if (isMobile) GenesysTextStyle.Label else GenesysTextStyle.Body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (product.price > 0) { // Oculta preço se for 0 (usado para feeds sociais no perfil)
                        GenesysSpacer(GenesysSpacing.Small)
                        
                        GenesysRow(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val priceFormatted = (product.price * 100.0).roundToLong() / 100.0
                            GenesysText(
                                text = "${GenesysStrings.PricePrefix}$priceFormatted", 
                                fontWeight = GenesysFontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                style = if (isMobile) GenesysTextStyle.Label else GenesysTextStyle.Body,
                                weightValue = 1f
                            )
                            
                            if (product.stock <= 0) {
                                GenesysBadge(
                                    label = "ESGOTADO", 
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.scale(if (isMobile) 0.7f else 1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
