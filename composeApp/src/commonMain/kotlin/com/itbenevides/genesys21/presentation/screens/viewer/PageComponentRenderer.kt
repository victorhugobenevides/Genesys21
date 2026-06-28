package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysBadge
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.atoms.images.GenesysImage
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysSearchBar
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysMotion
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.staggeredEntry
import com.itbenevides.genesys21.util.AnalyticsManager
import org.koin.compose.koinInject
import kotlin.math.roundToLong
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PageComponentRenderer(
    component: PageComponent,
    onProductClick: ((Product) -> Unit)? = null,
    filterQuery: String = "",
    onFilterQueryChange: (String) -> Unit = {},
    allAvailableCategories: List<String> = emptyList(),
    allProducts: List<Product> = emptyList(),
    isEditMode: Boolean = false,
    onEditClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val router: Router = koinInject()
    val backendUrl = remember { getBaseUrl() }
    val scope = rememberCoroutineScope()

    val isCategoryFilterActive = allAvailableCategories.any { it.equals(filterQuery, ignoreCase = true) }

    // Helper para lidar com cliques nos componentes de forma genérica
    val onComponentClick: () -> Unit = {
        if (isEditMode) {
            onEditClick?.invoke()
        } else {
            val destPageId = component.destinationPageId
            val destUrl = if (component is PageComponent.Button) component.url else component.destinationUrl

            if (!destPageId.isNullOrBlank()) {
                scope.launch {
                    router.viewModel.loadPublicPage(destPageId)?.let { targetPage ->
                        router.navigateTo(Route.PublicViewer(targetPage))
                    }
                }
            } else if (!destUrl.isNullOrBlank()) {
                uriHandler.openUri(destUrl)
            }
        }
    }

    val shouldShow =
        if (filterQuery.isBlank() || !component.isFilterable) {
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
                is PageComponent.ProductGrid -> {
                    val gridProducts = allProducts.filter { it.id in component.productIds }
                    if (isCategoryFilterActive) {
                        gridProducts.any { it.categoryName?.equals(filterQuery, ignoreCase = true) == true }
                    } else {
                        gridProducts.any {
                            it.name.contains(filterQuery, ignoreCase = true) ||
                                (it.categoryName?.contains(filterQuery, ignoreCase = true) == true)
                        }
                    }
                }
                is PageComponent.FeaturedProductsComponent -> {
                    val featProducts = allProducts.filter { it.id in component.productIds }
                    if (isCategoryFilterActive) {
                        featProducts.any { it.categoryName?.equals(filterQuery, ignoreCase = true) == true }
                    } else {
                        featProducts.any {
                            it.name.contains(filterQuery, ignoreCase = true) ||
                                (it.categoryName?.equals(filterQuery, ignoreCase = true) == true)
                        }
                    }
                }
                is PageComponent.CategoryComponent -> {
                    if (isCategoryFilterActive) {
                        component.categoryName.equals(filterQuery, ignoreCase = true)
                    } else {
                        val catProducts = allProducts.filter { it.categoryName == component.categoryName }
                        catProducts.any { it.name.contains(filterQuery, ignoreCase = true) }
                    }
                }
                is PageComponent.Header -> !isCategoryFilterActive && component.title.contains(filterQuery, ignoreCase = true)
                is PageComponent.Text -> !isCategoryFilterActive && component.content.contains(filterQuery, ignoreCase = true)
                is PageComponent.Image -> !isCategoryFilterActive && component.url.contains(filterQuery, ignoreCase = true)
                else -> true
            }
        }

    if (!shouldShow && component !is PageComponent.Filter && component !is PageComponent.CategoryFilter) return

    Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        when (component) {
            is PageComponent.ProfileHeader -> {
                val displayUrl =
                    remember(component.imageUrl, backendUrl) {
                        if (component.imageUrl.startsWith("/") && !component.imageUrl.startsWith("http")) "$backendUrl${component.imageUrl}" else component.imageUrl
                    }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).clickable { onComponentClick() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    GenesysImage(
                        url = displayUrl,
                        size = component.imageSize.dp,
                        isCircular = component.isCircular,
                    )
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysText(
                        text = component.name,
                        style = GenesysTextStyle.Headline,
                        fontWeight = GenesysFontWeight.ExtraBold,
                        textAlign = GenesysTextAlign.Center,
                    )
                    if (component.bio.isNotBlank()) {
                        GenesysText(
                            text = component.bio,
                            style = GenesysTextStyle.Body,
                            textAlign = GenesysTextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                }
            }
            is PageComponent.SocialLinks -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    component.instagram?.let { url ->
                        SocialLinkItem(
                            icon = GenesysIcons.Instagram,
                            label = "Instagram",
                            onClick = { if (!isEditMode) uriHandler.openUri(url) },
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                    component.whatsapp?.let { url ->
                        SocialLinkItem(
                            icon = GenesysIcons.WhatsApp,
                            label = "WhatsApp",
                            onClick = { if (!isEditMode) uriHandler.openUri(url) },
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                    component.youtube?.let { url ->
                        SocialLinkItem(
                            icon = GenesysIcons.YouTube,
                            label = "YouTube",
                            onClick = { if (!isEditMode) uriHandler.openUri(url) },
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                    component.email?.let { email ->
                        SocialLinkItem(
                            icon = GenesysIcons.Email,
                            label = "E-mail",
                            onClick = { if (!isEditMode) uriHandler.openUri("mailto:$email") },
                        )
                    }
                }
            }
            is PageComponent.Button -> {
                Box(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    GenesysLoadingButton(
                        text = component.text,
                        onClick = onComponentClick,
                        fillWidth = true,
                    )
                }
            }
            is PageComponent.CategoryFilter -> {
                GenesysColumn(usePadding = true) {
                    GenesysText(
                        text = GenesysStrings.Categories,
                        style = GenesysTextStyle.Label,
                        fontWeight = GenesysFontWeight.Bold,
                    )
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysRow(modifier = Modifier.fillMaxWidth(), useHorizontalScroll = true) {
                        GenesysFilterChip(
                            selected = filterQuery.isEmpty(),
                            onClick = { onFilterQueryChange("") },
                            label = GenesysStrings.All,
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
                                label = category,
                            )
                        }
                    }
                }
            }
            is PageComponent.CategoryComponent -> {
                val productsToDisplay =
                    remember(component.categoryName, allProducts) {
                        allProducts.filter { it.categoryName == component.categoryName }
                    }
                if (productsToDisplay.isNotEmpty()) {
                    GenesysColumn(usePadding = true) {
                        component.title?.let {
                            GenesysText(text = it, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                            GenesysSpacer(GenesysSpacing.Medium)
                        }
                        if (component.layout == "HORIZONTAL") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = 8.dp),
                            ) {
                                items(productsToDisplay) { product ->
                                    ProductCard(
                                        product = product,
                                        modifier = Modifier.width(180.dp),
                                        onClick = onProductClick,
                                        onAddToCart = { router.viewModel.addToCart(product) },
                                        onHover = { router.viewModel.prefetchProductDetails(it) },
                                        isEditMode = isEditMode,
                                    )
                                }
                            }
                        } else {
                            ProductGridLayout(
                                products = productsToDisplay,
                                columns = 2,
                                onProductClick = onProductClick,
                                onAddToCart = { router.viewModel.addToCart(it) },
                                onHover = { router.viewModel.prefetchProductDetails(it) },
                                isEditMode = isEditMode,
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
                        onClear = { onFilterQueryChange("") },
                    )
                }
            }
            is PageComponent.ProductList -> {
                val productsToDisplay =
                    if (filterQuery.isBlank() || !component.isFilterable) {
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
                        val maxColumns =
                            if (maxWidth > 900.dp) {
                                4
                            } else if (maxWidth > 600.dp) {
                                3
                            } else {
                                2
                            }
                        val horizontalItemWidth =
                            if (maxWidth > 900.dp) {
                                220.dp
                            } else if (maxWidth > 600.dp) {
                                180.dp
                            } else {
                                150.dp
                            }
                        val spacing = if (isMobile) 8.dp else 16.dp

                        GenesysColumn(usePadding = false) {
                            if (component.isHorizontal) {
                                val listState = rememberLazyListState()
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    LazyRow(
                                        state = listState,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(spacing),
                                        contentPadding = PaddingValues(horizontal = if (isMobile) 0.dp else 48.dp, vertical = 8.dp),
                                    ) {
                                        itemsIndexed(productsToDisplay) { index, product ->
                                            ProductCard(
                                                product = product,
                                                modifier = Modifier.width(horizontalItemWidth),
                                                onClick = onProductClick,
                                                onAddToCart = { router.viewModel.addToCart(product) },
                                                onHover = { router.viewModel.prefetchProductDetails(it) },
                                                isEditMode = isEditMode,
                                                index = index,
                                            )
                                        }
                                    }

                                    if (!isMobile && productsToDisplay.size > 1) {
                                        Surface(
                                            modifier = Modifier.align(Alignment.CenterStart).size(40.dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            tonalElevation = 4.dp,
                                        ) {
                                            GenesysIconButton(
                                                icon = GenesysIcons.ArrowLeft,
                                                onClick = {
                                                    scope.launch {
                                                        listState.animateScrollToItem(
                                                            (listState.firstVisibleItemIndex - 1).coerceAtLeast(0),
                                                        )
                                                    }
                                                },
                                            )
                                        }
                                        Surface(
                                            modifier = Modifier.align(Alignment.CenterEnd).size(40.dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            tonalElevation = 4.dp,
                                        ) {
                                            GenesysIconButton(
                                                icon = GenesysIcons.ArrowRight,
                                                onClick = {
                                                    scope.launch {
                                                        listState.animateScrollToItem(
                                                            (listState.firstVisibleItemIndex + 1).coerceAtMost(productsToDisplay.size - 1),
                                                        )
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            } else {
                                productsToDisplay.chunked(maxColumns).forEachIndexed { rowIndex, rowProducts ->
                                    GenesysRow(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                                        rowProducts.forEachIndexed { colIndex, product ->
                                            val overallIndex = rowIndex * maxColumns + colIndex
                                            GenesysWeightBox(1f) {
                                                ProductCard(
                                                    product = product,
                                                    onClick = onProductClick,
                                                    onAddToCart = { router.viewModel.addToCart(product) },
                                                    onHover = { router.viewModel.prefetchProductDetails(it) },
                                                    isEditMode = isEditMode,
                                                    index = overallIndex,
                                                )
                                            }
                                        }
                                        if (rowProducts.size < maxColumns) {
                                            val rowScope = this
                                            repeat(maxColumns - rowProducts.size) {
                                                rowScope.GenesysWeightSpacer(1f)
                                            }
                                        }
                                    }
                                    GenesysSpacer(if (isMobile) GenesysSpacing.Small else GenesysSpacing.Medium)
                                }
                            }
                        }
                    }
                }
            }
            is PageComponent.ProductGrid -> {
                val productsToDisplay =
                    remember(component.productIds, allProducts) {
                        allProducts.filter { it.id in component.productIds }
                    }
                if (productsToDisplay.isNotEmpty()) {
                    ProductGridLayout(
                        products = productsToDisplay,
                        columns = component.columns,
                        showPrice = component.showPrice,
                        onProductClick = onProductClick,
                        onAddToCart = { router.viewModel.addToCart(it) },
                        onHover = { router.viewModel.prefetchProductDetails(it) },
                        isEditMode = isEditMode,
                    )
                }
            }
            is PageComponent.FeaturedProductsComponent -> {
                val productsToDisplay =
                    remember(component.productIds, allProducts) {
                        allProducts.filter { it.id in component.productIds }
                    }
                if (productsToDisplay.isNotEmpty()) {
                    GenesysColumn(usePadding = true) {
                        GenesysText(text = component.title, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                        GenesysSpacer(GenesysSpacing.Medium)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(productsToDisplay) { product ->
                                ProductCard(
                                    product = product,
                                    modifier = Modifier.width(200.dp),
                                    onClick = onProductClick,
                                    onAddToCart = { router.viewModel.addToCart(product) },
                                    onHover = { router.viewModel.prefetchProductDetails(it) },
                                    isEditMode = isEditMode,
                                )
                            }
                        }
                    }
                }
            }
            is PageComponent.CartComponent -> {
                GenesysCard(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    onClick = {
                        if (!isEditMode) {
                            val page = router.viewModel.pages.value.find { it.id == component.destinationPageId } ?: router.viewModel.pages.value.first()
                            router.navigateTo(Route.Cart(page))
                        }
                    },
                ) {
                    GenesysRow(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(GenesysIcons.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary)
                        GenesysSpacer(GenesysSpacing.Medium)
                        GenesysRowText(text = component.title, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold, weightValue = 1f)
                        Icon(GenesysIcons.ArrowRight, null)
                    }
                }
            }
            is PageComponent.OrderTrackingComponent -> {
                GenesysCard(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    onClick = {
                        if (!isEditMode) {
                            val page = router.viewModel.pages.value.firstOrNull()
                            if (page != null) router.navigateTo(Route.CustomerOrderHistory(page))
                        }
                    },
                ) {
                    GenesysRow(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(GenesysIcons.List, null, tint = MaterialTheme.colorScheme.primary)
                        GenesysSpacer(GenesysSpacing.Medium)
                        GenesysRowText(text = component.title, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold, weightValue = 1f)
                        Icon(GenesysIcons.ArrowRight, null)
                    }
                }
            }
            is PageComponent.Header -> {
                GenesysText(
                    text =
                        if (component.isUppercase) {
                            component.title.uppercase().ifBlank {
                                GenesysStrings.UpdateTitle
                            }
                        } else {
                            component.title.ifBlank { GenesysStrings.UpdateTitle }
                        },
                    style = GenesysTextStyle.Headline,
                    fontWeight =
                        when (component.fontWeight) {
                            "BOLD" -> GenesysFontWeight.Bold
                            "EXTRA_BOLD" -> GenesysFontWeight.ExtraBold
                            else -> GenesysFontWeight.Normal
                        },
                    textAlign =
                        when (component.textAlign) {
                            "CENTER" -> GenesysTextAlign.Center
                            "RIGHT" -> GenesysTextAlign.End
                            else -> GenesysTextAlign.Start
                        },
                    color = if (component.usePrimaryColor) MaterialTheme.colorScheme.primary else Color.Unspecified,
                    fontSize = component.fontSize.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp).clickable { onComponentClick() },
                )
            }
            is PageComponent.Text -> {
                GenesysText(
                    text =
                        if (component.isUppercase) {
                            component.content.uppercase().ifBlank {
                                GenesysStrings.Description
                            }
                        } else {
                            component.content.ifBlank { GenesysStrings.Description }
                        },
                    style = GenesysTextStyle.Body,
                    fontWeight =
                        when (component.fontWeight) {
                            "BOLD" -> GenesysFontWeight.Bold
                            else -> GenesysFontWeight.Normal
                        },
                    textAlign =
                        when (component.textAlign) {
                            "CENTER" -> GenesysTextAlign.Center
                            "RIGHT" -> GenesysTextAlign.End
                            else -> GenesysTextAlign.Start
                        },
                    fontSize = component.fontSize.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onComponentClick() },
                )
            }
            is PageComponent.Image -> {
                val displayUrl =
                    remember(component.url, backendUrl) {
                        if (component.url.startsWith("/") && !component.url.startsWith("http")) "$backendUrl${component.url}" else component.url
                    }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = if (component.isFullWidth) 0.dp else 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    GenesysCard(
                        onClick = onComponentClick,
                        elevation = 0.dp,
                        backgroundColor = Color.Transparent,
                        modifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth(),
                    ) {
                        GenesysImage(
                            url = displayUrl,
                            size = component.size.dp,
                            isCircular = component.isCircular,
                            modifier = if (component.isFullWidth) Modifier.fillMaxWidth() else Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }
        }

        if (isEditMode) {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp),
            ) {
                val isMobile = maxWidth < 400.dp
                Surface(
                    onClick = { onEditClick?.invoke() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    contentColor = Color.White,
                    tonalElevation = 4.dp,
                    modifier = Modifier.size(if (isMobile) 32.dp else 36.dp),
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
private fun ProductGridLayout(
    products: List<Product>,
    columns: Int,
    showPrice: Boolean = true,
    onProductClick: ((Product) -> Unit)? = null,
    onAddToCart: ((Product) -> Unit)? = null,
    onHover: ((Product) -> Unit)? = null,
    isEditMode: Boolean = false,
) {
    GenesysColumn(usePadding = true) {
                products.chunked(columns).forEach { rowProducts ->
                    GenesysRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val rowScope = this
                        rowProducts.forEach { product ->
                            GenesysWeightBox(1f) {
                                ProductCard(
                                    product = product,
                                    showPrice = showPrice,
                                    onClick = onProductClick,
                                    onAddToCart = { onAddToCart?.invoke(product) },
                                    onHover = onHover,
                                    isEditMode = isEditMode,
                                )
                            }
                        }
                        if (rowProducts.size < columns) {
                            repeat(columns - rowProducts.size) { rowScope.GenesysWeightSpacer(1f) }
                        }
                    }
                    GenesysSpacer(GenesysSpacing.Medium)
                }
    }
}

@Composable
private fun SocialLinkItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    showPrice: Boolean = true,
    onClick: ((Product) -> Unit)? = null,
    onAddToCart: (() -> Unit)? = null,
    onHover: ((Product) -> Unit)? = null,
    isEditMode: Boolean = false,
    index: Int = 0,
) {
    var isAdded by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val backendUrl = remember { getBaseUrl() }

    val interactionScale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = GenesysMotion.interactiveSpring,
    )

    val successScale by animateFloatAsState(
        targetValue = if (isAdded) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    )

    BoxWithConstraints(
        modifier =
            modifier
                .staggeredEntry(index)
                .scale(interactionScale)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Enter -> {
                                    isHovered = true
                                    onHover?.invoke(product)
                                }
                                PointerEventType.Exit -> isHovered = false
                            }
                        }
                    }
                },
    ) {
        val isMobile = maxWidth < 180.dp

        GenesysCard(
            onClick =
                if (onClick != null) {
                    { onClick.invoke(product) }
                } else {
                    null
                },
            elevation = if (isHovered) 4.dp else 0.dp,
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MaterialTheme.colorScheme.surface,
            border =
                androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color =
                        if (isHovered) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        },
                ),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.9f) // Mais vertical para destaque
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    val imageUrl =
                        remember(product.imageUrls) {
                            val first = product.imageUrls.firstOrNull() ?: ""
                            if (first.startsWith("/")) "$backendUrl$first" else first
                        }

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )

                    // Glass Badge para Categoria
                    product.categoryName?.let { cat ->
                        Surface(
                            modifier =
                                Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.8f),
                            tonalElevation = 2.dp,
                        ) {
                            Text(
                                text = cat.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.Black,
                            )
                        }
                    }

                    if (isEditMode) {
                        Box(Modifier.fillMaxSize().padding(if (isMobile) 4.dp else 8.dp), contentAlignment = Alignment.TopEnd) {
                            Surface(
                                modifier = Modifier.size(if (isMobile) 24.dp else 32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White,
                                tonalElevation = 4.dp,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(GenesysIcons.Edit, null, modifier = Modifier.size(if (isMobile) 16.dp else 20.dp))
                                }
                            }
                        }
                    } else if (onAddToCart != null && product.stock > 0 && product.price > 0) {
                        Box(Modifier.fillMaxSize().padding(if (isMobile) 4.dp else 8.dp), contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(if (isMobile) 32.dp else 44.dp).scale(successScale),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAdded) Color(0xFF388E3C) else MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                shadowElevation = 8.dp,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    IconButton(
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
                                    ) {
                                        Icon(
                                            imageVector = if (isAdded) GenesysIcons.Check else GenesysIcons.Add,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(if (isMobile) 16.dp else 24.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = product.name,
                        style = if (isMobile) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (product.price > 0 && showPrice) {
                        GenesysSpacer(GenesysSpacing.Small)

                        GenesysRow(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val priceFormatted = (product.price * 100.0).roundToLong() / 100.0
                            Text(
                                text = "${GenesysStrings.PricePrefix}$priceFormatted",
                                style = if (isMobile) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                            )

                            if (product.stock <= 0) {
                                GenesysBadge(
                                    label = "ESGOTADO",
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    textColor = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
