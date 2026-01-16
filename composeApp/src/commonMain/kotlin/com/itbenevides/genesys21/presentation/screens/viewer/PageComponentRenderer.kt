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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import kotlinx.coroutines.launch

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
    
    // Verificamos se o componente deve ser filtrado.
    // Agora diferenciamos se é um filtro de texto (Filter) ou de categoria (CategoryFilter).
    // Para simplificar, se a filterQuery for exatamente uma das categorias, tratamos como filtro de categoria.
    val isCategoryFilterActive = allAvailableCategories.any { it.equals(filterQuery, ignoreCase = true) }

    val shouldShow = if (filterQuery.isBlank() || !component.isFilterable) {
        true
    } else {
        when (component) {
            is PageComponent.ProductList -> {
                if (isCategoryFilterActive) {
                    // Filtro de Categoria: Só mostra se algum produto do bloco tiver a categoria exata
                    component.products.any { it.category.equals(filterQuery, ignoreCase = true) }
                } else {
                    // Filtro de Texto: Busca no nome ou categoria
                    component.products.any { 
                        it.name.contains(filterQuery, ignoreCase = true) || 
                        it.category.contains(filterQuery, ignoreCase = true) 
                    }
                }
            }
            is PageComponent.Header -> !isCategoryFilterActive && component.title.contains(filterQuery, ignoreCase = true)
            is PageComponent.Text -> !isCategoryFilterActive && component.content.contains(filterQuery, ignoreCase = true)
            is PageComponent.Image -> !isCategoryFilterActive && component.string.contains(filterQuery, ignoreCase = true)
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
                    // Opção "Todos"
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
            // Filtramos os produtos individualmente
            val productsToDisplay = if (filterQuery.isBlank() || !component.isFilterable) {
                component.products
            } else {
                if (isCategoryFilterActive) {
                    // Filtro por categoria: Busca exata no campo categoria
                    component.products.filter { it.category.equals(filterQuery, ignoreCase = true) }
                } else {
                    // Filtro por texto: Busca no nome ou categoria
                    component.products.filter { 
                        it.name.contains(filterQuery, ignoreCase = true) || 
                        it.category.contains(filterQuery, ignoreCase = true)
                    }
                }
            }

            if (productsToDisplay.isNotEmpty()) {
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
                                        onClick = onProductClick
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
                        productsToDisplay.chunked(2).forEach { rowProducts ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowProducts.forEach { product ->
                                    ProductCard(
                                        product = product,
                                        shape = commonShape,
                                        isTransparent = component.isTransparent,
                                        modifier = Modifier.weight(1f),
                                        onClick = onProductClick
                                    )
                                }
                                if (rowProducts.size == 1) Spacer(Modifier.weight(1f))
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
            val imgShape = if (component.isRounded) CircleShape else RoundedCornerShape(12.dp)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.wrapContentSize(),
                    shape = imgShape,
                    color = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    Box(Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(component.size.dp)
                        )
                    }
                }
                if (component.string.isNotEmpty()) {
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
    onClick: ((Product) -> Unit)? = null
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
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f).clip(shape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(product.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("R$ ${product.price}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                if (product.stock > 0) {
                    Text("${product.stock} un", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Esgotado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
