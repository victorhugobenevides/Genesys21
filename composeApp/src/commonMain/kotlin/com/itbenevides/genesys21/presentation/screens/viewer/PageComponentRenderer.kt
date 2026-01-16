package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product

@Composable
fun PageComponentRenderer(
    component: PageComponent,
    onProductClick: ((Product) -> Unit)? = null
) {
    val commonShape = if (component.isRounded) CircleShape else RoundedCornerShape(8.dp)
    
    when (component) {
        is PageComponent.Logo -> {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(component.size.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    if (component.url.isNotEmpty()) {
                        Box(contentAlignment = Alignment.Center) { Text("LOGO", style = MaterialTheme.typography.labelSmall) }
                    } else {
                        Icon(Icons.Default.Store, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
        is PageComponent.ProductList -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Produtos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (component.products.isEmpty()) {
                    Text("Sem produtos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                } else {
                    component.products.chunked(2).forEach { rowProducts ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowProducts.forEach { product ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .let { m -> 
                                            if (onProductClick != null) m.clickable { onProductClick(product) } 
                                            else m 
                                        },
                                    shape = commonShape,
                                    color = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
                                    border = if (component.isTransparent) null else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            Modifier.fillMaxWidth().aspectRatio(1f).clip(commonShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(product.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("R$ ${product.price}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            if (rowProducts.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        is PageComponent.Header -> Text(
            text = component.title.ifBlank { "Título" }, 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        is PageComponent.Text -> Text(
            text = component.content.ifBlank { "Conteúdo..." }, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        is PageComponent.Image -> {
            val imgShape = if (component.isRounded) CircleShape else RoundedCornerShape(12.dp)
            Column(horizontalAlignment = if (component.isRounded) Alignment.CenterHorizontally else Alignment.Start) {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(if (component.isRounded) 1f else 1.7f).clip(imgShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                }
                if (component.string.isNotEmpty()) {
                    Text(component.string, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start)
                }
            }
        }
    }
}
