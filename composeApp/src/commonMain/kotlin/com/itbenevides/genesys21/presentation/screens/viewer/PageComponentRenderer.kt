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

@Composable
fun PageComponentRenderer(
    component: PageComponent,
    onProductClick: ((Product) -> Unit)? = null,
    filterQuery: String = "",
    onFilterQueryChange: (String) -> Unit = {}
) {
    val commonShape = if (component.isRounded) CircleShape else RoundedCornerShape(8.dp)
    
    when (component) {
        is PageComponent.Filter -> {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = onFilterQueryChange,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                textStyle = TextStyle(fontSize = 14.sp),
                placeholder = { Text(component.placeholder, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (filterQuery.isNotEmpty()) {
                        IconButton(onClick = { onFilterQueryChange("") }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = if (component.isRounded) CircleShape else RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface
                )
            )
        }
        is PageComponent.ProductList -> {
            val filteredProducts = if (filterQuery.isBlank()) {
                component.products
            } else {
                component.products.filter { 
                    it.name.contains(filterQuery, ignoreCase = true) || 
                    it.description.contains(filterQuery, ignoreCase = true) ||
                    it.category.contains(filterQuery, ignoreCase = true)
                }
            }

            if (filteredProducts.isNotEmpty()) {
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
                                items(filteredProducts) { product ->
                                    ProductCard(
                                        product = product,
                                        shape = commonShape,
                                        isTransparent = component.isTransparent,
                                        modifier = Modifier.width(160.dp),
                                        onClick = onProductClick
                                    )
                                }
                            }

                            if (filteredProducts.size > 1) {
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
                                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.8f))
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(16.dp))
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
                                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.8f))
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(16.dp))
                                        }
                                    } else { Spacer(Modifier.width(32.dp)) }
                                }
                            }
                        }
                    } else {
                        filteredProducts.chunked(2).forEach { rowProducts ->
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
            if (filterQuery.isBlank() || component.title.contains(filterQuery, ignoreCase = true)) {
                Text(
                    text = component.title.ifBlank { "Título" }, 
                    style = MaterialTheme.typography.headlineMedium, 
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        is PageComponent.Text -> {
            if (filterQuery.isBlank() || component.content.contains(filterQuery, ignoreCase = true)) {
                Text(
                    text = component.content.ifBlank { "Conteúdo..." }, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        is PageComponent.Image -> {
            if (filterQuery.isBlank() || component.string.contains(filterQuery, ignoreCase = true)) {
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
                                tint = MaterialTheme.colorScheme.outline,
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
        }
        is PageComponent.Unknown -> {
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.QuestionMark, null, tint = Color.LightGray)
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
        border = if (isTransparent) null else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f).clip(shape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(product.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("R$ ${product.price}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                if (product.stock > 0) {
                    Text("${product.stock} un", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.Gray)
                } else {
                    Text("Esgotado", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.Red)
                }
            }
        }
    }
}
