package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.util.Analytics

@Composable
fun PageComponentRenderer(
    component: PageComponent,
    allComponents: List<PageComponent> = emptyList(),
    isEditMode: Boolean = false,
    onProductClick: (Product) -> Unit = {},
    selectedCategory: String? = null,
    onCategorySelect: (String?) -> Unit = {}
) {
    val backendUrl = remember { getBaseUrl() }

    // UX IMPROVEMENT: Adicionando animação de entrada para os componentes
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("page_component_${component::class.simpleName}")
                .then(
                    if (component.backgroundColor != null) {
                        Modifier.background(parseHexColor(component.backgroundColor!!))
                    } else Modifier
                )
        ) {
            when (component) {
                is PageComponent.Typography -> TypographyRenderer(component)
                is PageComponent.Header -> HeaderRenderer(component)
                is PageComponent.Media -> MediaRenderer(component, backendUrl)
                is PageComponent.Image -> ImageRenderer(component, backendUrl)
                is PageComponent.Highlight -> HighlightRenderer(component)
                is PageComponent.ProductList -> ProductListRenderer(component, backendUrl, onProductClick, selectedCategory)
                is PageComponent.CategoryFilter -> CategoryFilterRenderer(
                    allComponents = allComponents,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { 
                        Analytics.logEvent("select_category", mapOf("category" to (it ?: "all")))
                        onCategorySelect(it) 
                    }
                )
                is PageComponent.StepProcess -> StepProcessRenderer(component)
                is PageComponent.Testimonial -> TestimonialRenderer(component)
                is PageComponent.SocialLinks -> SocialLinksRenderer(component)
                is PageComponent.ProfileHeader -> ProfileHeaderRenderer(component, backendUrl)
                is PageComponent.Search -> SearchBarRenderer(component)
                else -> { /* Unknown component */ }
            }
        }
    }
}

// ... restante das funções auxiliares permanecem as mesmas (TypographyRenderer, HeaderRenderer, etc.)
// ... (Omitido para brevidade, mas o código original é mantido abaixo)

@Composable
private fun TypographyRenderer(comp: PageComponent.Typography) {
    val textAlign = when (comp.textAlign) {
        "CENTER" -> TextAlign.Center
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Left
    }
    
    val fontWeight = when (comp.fontWeight) {
        "BOLD" -> FontWeight.Bold
        "EXTRA_BOLD" -> FontWeight.ExtraBold
        else -> FontWeight.Normal
    }

    Text(
        text = if (comp.isUppercase) comp.text.uppercase() else comp.text,
        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("typography_text"),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = comp.fontSize.sp,
            textAlign = textAlign,
            fontWeight = fontWeight,
            color = if (comp.usePrimaryColor) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    )
}

@Composable
private fun HeaderRenderer(comp: PageComponent.Header) {
    val textAlign = if (comp.textAlign == "CENTER") TextAlign.Center else TextAlign.Left
    Text(
        text = if (comp.isUppercase) comp.title.uppercase() else comp.title,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp).testTag("header_text"),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = comp.fontSize.sp,
            textAlign = textAlign,
            fontWeight = FontWeight.ExtraBold
        )
    )
}

@Composable
private fun MediaRenderer(comp: PageComponent.Media, baseUrl: String) {
    val imageUrl = if (comp.url.startsWith("/")) "$baseUrl${comp.url}" else comp.url
    
    when (comp.layout) {
        "SIDE_TEXT" -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("media_side_text"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!comp.imageOnRight) {
                    MediaImageItem(imageUrl, comp.size.dp, comp.isRounded)
                    Spacer(Modifier.width(16.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    comp.title?.let { Text(it, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    comp.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }

                if (comp.imageOnRight) {
                    Spacer(Modifier.width(16.dp))
                    MediaImageItem(imageUrl, comp.size.dp, comp.isRounded)
                }
            }
        }
        "CIRCULAR" -> {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("media_circular"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(comp.size.dp)
                        .clip(CircleShape)
                ) {
                    GenesysImage(url = imageUrl, contentScale = ContentScale.Crop)
                }
                comp.title?.let { 
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) 
                }
            }
        }
        else -> { // FULL_WIDTH
            Box(modifier = Modifier.fillMaxWidth().testTag("media_full_width")) {
                GenesysImage(
                    url = imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(comp.size.dp)
                        .then(if (comp.isRounded) Modifier.clip(RoundedCornerShape(12.dp)) else Modifier),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun MediaImageItem(url: String, size: androidx.compose.ui.unit.Dp, isRounded: Boolean) {
    Box(
        modifier = Modifier
            .size(size)
            .then(if (isRounded) Modifier.clip(RoundedCornerShape(12.dp)) else Modifier)
    ) {
        GenesysImage(url = url, contentScale = ContentScale.Crop)
    }
}

@Composable
private fun ImageRenderer(comp: PageComponent.Image, baseUrl: String) {
    val imageUrl = if (comp.url.startsWith("/")) "$baseUrl${comp.url}" else comp.url
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (comp.isFullWidth) 0.dp else 16.dp)
            .testTag("image_renderer"),
        contentAlignment = Alignment.Center
    ) {
        GenesysImage(
            url = imageUrl,
            modifier = Modifier
                .then(if (comp.isFullWidth) Modifier.fillMaxWidth() else Modifier.size(comp.size.dp))
                .then(if (comp.isCircular) Modifier.clip(CircleShape) else Modifier),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun HighlightRenderer(comp: PageComponent.Highlight) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { /* Action handled elsewhere */ },
            modifier = Modifier.fillMaxWidth().testTag("highlight_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (comp.usePrimaryColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(comp.text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProductListRenderer(
    comp: PageComponent.ProductList,
    baseUrl: String,
    onProductClick: (Product) -> Unit,
    selectedCategory: String?
) {
    val filteredProducts = remember(comp.products, selectedCategory) {
        if (selectedCategory == null) comp.products
        else comp.products.filter { it.categoryName == selectedCategory }
    }

    if (comp.isHorizontal) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag("product_list_horizontal")
        ) {
            items(filteredProducts) { product ->
                ProductCard(product, baseUrl, onProductClick, width = 160.dp)
            }
        }
    } else {
        Column(
            modifier = Modifier.padding(16.dp).testTag("product_list_vertical"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            filteredProducts.forEach { product ->
                ProductCard(product, baseUrl, onProductClick, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    baseUrl: String,
    onClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp? = null
) {
    val imageUrl = if (product.imageUrl.startsWith("/")) "$baseUrl${product.imageUrl}" else product.imageUrl
    
    GenesysCard(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .testTag("product_card_${product.id}"),
        onClick = { 
            Analytics.logEvent("product_click", mapOf("product_id" to product.id))
            onClick(product) 
        }
    ) {
        Column {
            GenesysImage(
                url = imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (width != null) 140.dp else 200.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(12.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "R$ ${product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRenderer(
    allComponents: List<PageComponent>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit
) {
    val categories = remember(allComponents) {
        allComponents.filterIsInstance<PageComponent.ProductList>()
            .flatMap { it.products }
            .mapNotNull { it.categoryName }
            .distinct()
            .sorted()
    }

    if (categories.isEmpty()) return

    Column(Modifier.padding(16.dp).testTag("category_filter")) {
        Text(
            text = "Categorias",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text("Todas") },
                    modifier = Modifier.testTag("chip_all"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            categories.forEach { category ->
                item {
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category) },
                        modifier = Modifier.testTag("chip_$category"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StepProcessRenderer(comp: PageComponent.StepProcess) {
    Column(Modifier.padding(16.dp).testTag("step_process")) {
        comp.steps.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = Color.White, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(step.title, fontWeight = FontWeight.Bold)
                    Text(step.description, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (index < comp.steps.size - 1) {
                Box(Modifier.padding(start = 11.dp).width(2.dp).height(20.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }
        }
    }
}

@Composable
private fun TestimonialRenderer(comp: PageComponent.Testimonial) {
    GenesysCard(modifier = Modifier.padding(16.dp).testTag("testimonial_card")) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("\"${comp.quote}\"", style = MaterialTheme.typography.bodyLarge, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("- ${comp.author}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SocialLinksRenderer(comp: PageComponent.SocialLinks) {
    val uriHandler = LocalUriHandler.current
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("social_links"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        comp.whatsapp?.let { link ->
            SocialIcon(GenesysIcons.Chat, "whatsapp") { 
                Analytics.logEvent("social_click", mapOf("platform" to "whatsapp"))
                uriHandler.openUri(link) 
            }
        }
        comp.instagram?.let { link ->
            SocialIcon(GenesysIcons.Magic, "instagram") { 
                Analytics.logEvent("social_click", mapOf("platform" to "instagram"))
                uriHandler.openUri(link) 
            }
        }
        comp.email?.let { link ->
            SocialIcon(GenesysIcons.Email, "email") { 
                Analytics.logEvent("social_click", mapOf("platform" to "email"))
                uriHandler.openUri("mailto:$link") 
            }
        }
    }
}

@Composable
private fun SocialIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    platform: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag("social_icon_$platform")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProfileHeaderRenderer(comp: PageComponent.ProfileHeader, baseUrl: String) {
    val imageUrl = if (comp.imageUrl.startsWith("/")) "$baseUrl${comp.imageUrl}" else comp.imageUrl
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).testTag("profile_header"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(comp.imageSize.dp)
                .then(if (comp.isCircular) Modifier.clip(CircleShape) else Modifier.clip(RoundedCornerShape(16.dp)))
        ) {
            GenesysImage(url = imageUrl, contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.height(16.dp))
        Text(comp.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(comp.bio, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SearchBarRenderer(comp: PageComponent.Search) {
    var query by remember { mutableStateOf("") }
    
    Box(Modifier.padding(16.dp).testTag("search_bar")) {
        GenesysTextField(
            value = query,
            onValueChange = { 
                query = it 
                Analytics.logEvent("search_type", mapOf("query_length" to it.length))
            },
            placeholder = comp.placeholder,
            icon = GenesysIcons.Search,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun parseHexColor(colorString: String): Color {
    return try {
        val hex = colorString.removePrefix("#")
        when (hex.length) {
            3 -> { 
                val r = hex.substring(0, 1).repeat(2).toInt(16)
                val g = hex.substring(1, 2).repeat(2).toInt(16)
                val b = hex.substring(2, 3).repeat(2).toInt(16)
                Color(red = r, green = g, blue = b)
            }
            6 -> Color(
                red = hex.substring(0, 2).toInt(16),
                green = hex.substring(2, 4).toInt(16),
                blue = hex.substring(4, 6).toInt(16)
            )
            8 -> Color(
                alpha = hex.substring(0, 2).toInt(16),
                red = hex.substring(2, 4).toInt(16),
                green = hex.substring(4, 6).toInt(16),
                blue = hex.substring(6, 8).toInt(16)
            )
            else -> Color.Transparent
        }
    } catch (e: Exception) {
        Color.Transparent
    }
}
