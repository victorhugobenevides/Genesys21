package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
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

fun String.toComposeColor(): Color {
    return try {
        val hex = if (this.startsWith("#")) this.substring(1) else this
        if (hex.length == 6) Color(red = hex.substring(0, 2).toInt(16), green = hex.substring(2, 4).toInt(16), blue = hex.substring(4, 6).toInt(16), alpha = 255)
        else if (hex.length == 8) Color(red = hex.substring(2, 4).toInt(16), green = hex.substring(4, 6).toInt(16), blue = hex.substring(6, 8).toInt(16), alpha = hex.substring(0, 2).toInt(16))
        else Color.Unspecified
    } catch (e: Exception) { Color.Unspecified }
}

@Composable
fun PageComponentRenderer(
    component: PageComponent,
    onProductClick: ((Product) -> Unit)? = null,
    filterQuery: String = "",
    onFilterQueryChange: (String) -> Unit = {},
    allAvailableCategories: List<String> = emptyList(),
    isEditMode: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    onManageCategories: (() -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val router: Router = koinInject()
    val backendUrl = remember { getBaseUrl() }
    val scope = rememberCoroutineScope()
    
    val isCategoryFilterActive = allAvailableCategories.any { it.equals(filterQuery, ignoreCase = true) }

    val onComponentClick: () -> Unit = {
        if (isEditMode) {
            onEditClick?.invoke()
        } else {
            val destPageId = component.destinationPageId
            val destUrl = component.destinationUrl
            if (!destPageId.isNullOrBlank()) {
                scope.launch {
                    router.viewModel.loadPublicPage(destPageId)?.let { router.navigateTo(Route.PublicViewer(it)) }
                }
            } else if (!destUrl.isNullOrBlank()) {
                uriHandler.openUri(destUrl)
            }
        }
    }

    val componentBg = remember(component.backgroundColor) { 
        component.backgroundColor?.toComposeColor() ?: Color.Transparent 
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(componentBg)
    ) {
        when (component) {
            is PageComponent.Typography -> {
                val textAlign = when(component.textAlign) {
                    "CENTER" -> TextAlign.Center
                    "RIGHT" -> TextAlign.End
                    else -> TextAlign.Start
                }
                val fontWeight = when(component.fontWeight) {
                    "BOLD" -> FontWeight.Bold
                    "BLACK" -> FontWeight.Black
                    "EXTRA_BOLD" -> FontWeight.ExtraBold
                    else -> FontWeight.Normal
                }
                val text = if (component.isUppercase) component.text.uppercase() else component.text
                val color = if (component.usePrimaryColor) MaterialTheme.colorScheme.primary else Color.Unspecified

                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp).clickable { onComponentClick() }, contentAlignment = Alignment.Center) {
                    if (component.style == "SHADOW") {
                        Text(text = text, fontSize = component.fontSize.sp, fontWeight = fontWeight, color = Color.Black.copy(alpha = 0.1f), textAlign = textAlign, modifier = Modifier.offset(4.dp, 4.dp).fillMaxWidth())
                    }
                    Text(text = text, fontSize = component.fontSize.sp, fontWeight = fontWeight, color = color, textAlign = textAlign, modifier = Modifier.fillMaxWidth())
                }
            }

            is PageComponent.Media -> {
                val displayUrl = remember(component.url, backendUrl) {
                    if (component.url.startsWith("/") && !component.url.startsWith("http")) "$backendUrl${component.url}" else component.url
                }
                val shape = if (component.hasBottomArc) RoundedCornerShape(0.dp, 0.dp, 100.dp, 100.dp)
                            else if (component.isRounded) RoundedCornerShape(16.dp)
                            else androidx.compose.ui.graphics.RectangleShape

                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    val isSideLayout = component.layout == "SIDE_TEXT" && this@BoxWithConstraints.maxWidth > 600.dp
                    
                    if (isSideLayout) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            if (!component.imageOnRight) GenesysImage(url = displayUrl, size = component.size.dp, modifier = Modifier.clip(shape))
                            Column(modifier = Modifier.weight(1f)) {
                                component.title?.let { Text(text = it, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                                component.description?.let { Text(text = it, style = MaterialTheme.typography.bodyLarge) }
                            }
                            if (component.imageOnRight) GenesysImage(url = displayUrl, size = component.size.dp, modifier = Modifier.clip(shape))
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val imgSize = if (component.layout == "FULL_WIDTH") this@BoxWithConstraints.maxWidth else component.size.dp
                             GenesysImage(url = displayUrl, size = imgSize, modifier = Modifier.clip(shape))
                            if (component.title != null || component.description != null) {
                                Spacer(Modifier.height(16.dp))
                                component.title?.let { Text(text = it, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
                                component.description?.let { Text(text = it, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center) }
                            }
                        }
                    }
                }
            }

            is PageComponent.Highlight -> {
                when (component.type) {
                    "BUTTON" -> {
                        Box(modifier = Modifier.padding(16.dp)) {
                            GenesysLoadingButton(text = component.text, onClick = { if(!isEditMode) component.url?.let { uriHandler.openUri(it) } else onComponentClick() }, fillWidth = true)
                        }
                    }
                    "MARQUEE" -> {
                        val bg = component.backgroundColor?.toComposeColor() ?: MaterialTheme.colorScheme.primary
                        val tc = component.textColor?.toComposeColor() ?: Color.White
                        Box(modifier = Modifier.fillMaxWidth().background(bg).padding(vertical = 12.dp).basicMarquee(iterations = Int.MAX_VALUE)) {
                            Text(text = (component.text + " • ").repeat(10), color = tc, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                    "BADGE" -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(100.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(GenesysIcons.Magic, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = component.text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(GenesysIcons.Magic, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
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
                        val isMobile = this@BoxWithConstraints.maxWidth < 600.dp
                        val maxColumns = if (this@BoxWithConstraints.maxWidth > 900.dp) 4 else if (this@BoxWithConstraints.maxWidth > 600.dp) 3 else 2
                        val horizontalItemWidth = if (this@BoxWithConstraints.maxWidth > 900.dp) 220.dp else if (this@BoxWithConstraints.maxWidth > 600.dp) 180.dp else 150.dp
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
                                            ProductCardUI(
                                                product = product,
                                                modifier = Modifier.width(horizontalItemWidth),
                                                onClick = onProductClick,
                                                onAddToCart = { router.viewModel.addToCart(product) },
                                                isEditMode = isEditMode
                                            )
                                        }
                                    }
                                    
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
                                    GenesysRow(horizontalArrangement = Arrangement.spacedBy(spacing), modifier = Modifier.padding(horizontal = 16.dp)) {
                                        rowProducts.forEach { product ->
                                            GenesysWeightBox(1f) {
                                                ProductCardUI(
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

            is PageComponent.CategoryFilter -> {
                val listState = rememberLazyListState()
                val isMobile = true // Simplificação para lógica de setas, pode ser maxWidth < 600.dp
                
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    val actualIsMobile = this.maxWidth < 600.dp
                    
                    GenesysColumn(usePadding = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GenesysText(
                                text = GenesysStrings.Categories,
                                style = GenesysTextStyle.Label,
                                fontWeight = GenesysFontWeight.Bold
                            )
                            if (isEditMode) {
                                GenesysIconButton(
                                    icon = GenesysIcons.Settings, 
                                    onClick = { onManageCategories?.invoke() },
                                    contentDescription = "Gerenciar Categorias"
                                )
                            }
                        }
                        
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            LazyRow(
                                state = listState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = if(actualIsMobile) 0.dp else 40.dp)
                            ) {
                                item {
                                    GenesysFilterChip(
                                        selected = filterQuery.isEmpty(),
                                        onClick = { onFilterQueryChange("") },
                                        label = GenesysStrings.All
                                    )
                                }
                                
                                items(allAvailableCategories) { category ->
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

                            if (!actualIsMobile && allAvailableCategories.size > 3) {
                                Surface(
                                    modifier = Modifier.align(Alignment.CenterStart).size(32.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    tonalElevation = 2.dp
                                ) {
                                    GenesysIconButton(
                                        icon = GenesysIcons.ArrowLeft, 
                                        onClick = { scope.launch { listState.animateScrollToItem((listState.firstVisibleItemIndex - 1).coerceAtLeast(0)) } }
                                    )
                                }
                                Surface(
                                    modifier = Modifier.align(Alignment.CenterEnd).size(32.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    tonalElevation = 2.dp
                                ) {
                                    GenesysIconButton(
                                        icon = GenesysIcons.ArrowRight, 
                                        onClick = { scope.launch { listState.animateScrollToItem((listState.firstVisibleItemIndex + 1).coerceAtMost(allAvailableCategories.size)) } }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is PageComponent.Testimonial -> {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(vertical = 48.dp, horizontal = 24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(GenesysIcons.FormatQuote, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                        Text(text = component.quote, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        Spacer(Modifier.height(16.dp))
                        Text(text = component.author, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            is PageComponent.StepProcess -> {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                    component.steps.forEachIndexed { index, step ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = (index + 1).toString(), fontSize = 80.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.offset(y = (-20).dp))
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = step.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                Text(text = step.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            is PageComponent.SocialLinks -> {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    component.instagram?.let { SocialLinkItem(icon = GenesysIcons.Instagram, label = "Instagram", onClick = { if(!isEditMode) uriHandler.openUri(it) }) }
                    Spacer(Modifier.width(16.dp))
                    component.whatsapp?.let { SocialLinkItem(icon = GenesysIcons.WhatsApp, label = "WhatsApp", onClick = { if(!isEditMode) uriHandler.openUri(it) }) }
                    Spacer(Modifier.width(16.dp))
                    component.email?.let { SocialLinkItem(icon = GenesysIcons.Email, label = "E-mail", onClick = { if(!isEditMode) uriHandler.openUri("mailto:$it") }) }
                }
            }
            
            is PageComponent.ProfileHeader -> {
                val displayUrl = remember(component.imageUrl, backendUrl) { if (component.imageUrl.startsWith("/") && !component.imageUrl.startsWith("http")) "$backendUrl${component.imageUrl}" else component.imageUrl }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).clickable { onComponentClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
                    GenesysImage(url = displayUrl, size = component.imageSize.dp, isCircular = component.isCircular)
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysText(text = component.name, style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold, textAlign = GenesysTextAlign.Center)
                    if (component.bio.isNotBlank()) { GenesysText(text = component.bio, style = GenesysTextStyle.Body, textAlign = GenesysTextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp)) }
                }
            }
            is PageComponent.Search -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    GenesysSearchBar(
                        value = filterQuery,
                        onValueChange = onFilterQueryChange,
                        placeholder = component.placeholder
                    )
                }
            }
            else -> { }
        }

        if (isEditMode) {
            Surface(onClick = { onEditClick?.invoke() }, shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), contentColor = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(GenesysIcons.Edit, null, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

@Composable
private fun SocialLinkItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(4.dp)) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ProductCardUI(
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
        val isMobile = this@BoxWithConstraints.maxWidth < 180.dp
        
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
                    } else if (onAddToCart != null && product.stock > 0 && product.price > 0) {
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
                    
                    if (product.price > 0) {
                        val priceFormatted = (product.price * 100.0).roundToLong() / 100.0
                        GenesysText(
                            text = "${GenesysStrings.PricePrefix}$priceFormatted", 
                            fontWeight = GenesysFontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            style = if (isMobile) GenesysTextStyle.Label else GenesysTextStyle.Body,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        if (product.stock <= 0) {
                            GenesysSpacer(GenesysSpacing.Small)
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
