package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun ProductListComponentEditor(
    component: PageComponent.ProductList,
    allAvailableProducts: List<Product>,
    onEditProduct: (Product?) -> Unit,
    onProductsUpdated: (List<Product>) -> Unit,
    onSaveLabel: (String, Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var customLabel by remember { mutableStateOf(component.customLabel ?: "") }
    var isHorizontal by remember { mutableStateOf(component.isHorizontal) }
    var searchQuery by remember { mutableStateOf("") }

    GenesysColumn(usePadding = false) {
        // --- SEÇÃO 1: IDENTIFICAÇÃO DO BLOCO ---
        GenesysText(text = "Configurações do Bloco", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysTextField(
            value = customLabel,
            onValueChange = { customLabel = it },
            label = "Nome do Bloco (Opcional)",
            placeholder = "Ex: Promoções, Lançamentos...",
            icon = GenesysIcons.Edit
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysRow(verticalAlignment = Alignment.CenterVertically) {
            GenesysText("Exibir em Lista Horizontal?", style = GenesysTextStyle.Body, weightValue = 1f)
            Switch(checked = isHorizontal, onCheckedChange = { isHorizontal = it })
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysDivider()
        GenesysSpacer(GenesysSpacing.Large)

        // --- SEÇÃO 2: GESTÃO DE ITENS ---
        GenesysText(text = "Gerenciar Produtos", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysSpacer(GenesysSpacing.Medium)

        GenesysTabRow(
            selectedTabIndex = selectedTab,
            tabs = listOf(
                GenesysTabData("Itens na Lista", GenesysIcons.List, component.products.size),
                GenesysTabData("Catálogo", GenesysIcons.Inventory)
            ),
            onTabSelected = { selectedTab = it }
        )

        GenesysSpacer(GenesysSpacing.Medium)

        GenesysTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Buscar produto...",
            icon = GenesysIcons.Search
        )

        GenesysSpacer(GenesysSpacing.Medium)

        Box(modifier = Modifier.heightIn(max = 400.dp)) {
            if (selectedTab == 0) {
                // ABA: PRODUTOS ATUAIS
                val filteredListProducts = component.products.filter { product ->
                    product.name.contains(searchQuery, ignoreCase = true)
                }

                GenesysColumn(usePadding = false, useScroll = true) {
                    if (filteredListProducts.isEmpty()) {
                        GenesysText("Nenhum produto nesta lista.", style = GenesysTextStyle.Label, textAlign = GenesysTextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                    } else {
                        filteredListProducts.forEachIndexed { index, product ->
                            ProductEditorRow(
                                product = product,
                                isFirst = index == 0,
                                isLast = index == filteredListProducts.size - 1,
                                onMoveUp = {
                                    val newList = component.products.toMutableList()
                                    val actualIdx = component.products.indexOf(product)
                                    if (actualIdx > 0) {
                                        val temp = newList[actualIdx]
                                        newList[actualIdx] = newList[actualIdx - 1]
                                        newList[actualIdx - 1] = temp
                                        onProductsUpdated(newList)
                                    }
                                },
                                onMoveDown = {
                                    val newList = component.products.toMutableList()
                                    val actualIdx = component.products.indexOf(product)
                                    if (actualIdx < newList.size - 1) {
                                        val temp = newList[actualIdx]
                                        newList[actualIdx] = newList[actualIdx + 1]
                                        newList[actualIdx + 1] = temp
                                        onProductsUpdated(newList)
                                    }
                                },
                                onRemove = { onProductsUpdated(component.products.filter { it.id != product.id }) },
                                onEdit = { onEditProduct(product) }
                            )
                        }
                    }
                    
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysLoadingButton(
                        text = "Criar Novo Produto",
                        icon = GenesysIcons.Add,
                        onClick = { onEditProduct(null) },
                        fillWidth = true,
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                // ABA: CATÁLOGO
                val catalogToDisplay = allAvailableProducts
                    .filter { p -> component.products.none { it.id == p.id } }
                    .filter { it.name.contains(searchQuery, ignoreCase = true) }
                
                GenesysColumn(usePadding = false, useScroll = true) {
                    if (catalogToDisplay.isEmpty()) {
                        GenesysText("Tudo o que você tem já está nesta lista.", style = GenesysTextStyle.Label, textAlign = GenesysTextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                    } else {
                        catalogToDisplay.forEach { product ->
                            GenesysCard(modifier = Modifier.padding(bottom = 8.dp), backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                                GenesysRow {
                                    GenesysWeightBox(1f) { GenesysText(product.name, fontWeight = GenesysFontWeight.Bold) }
                                    GenesysIconButton(icon = GenesysIcons.Add, onClick = { onProductsUpdated(component.products + product) })
                                }
                            }
                        }
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Huge)
        GenesysLoadingButton(text = "Salvar Todas as Alterações", fillWidth = true, onClick = {
            onSaveLabel(customLabel, isHorizontal)
        })
    }
}

@Composable
private fun ProductEditorRow(
    product: Product,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                GenesysText(product.name, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                GenesysText("R$ ${product.price}", style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.primary)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                GenesysIconButton(icon = GenesysIcons.ArrowUp, enabled = !isFirst, onClick = onMoveUp)
                GenesysIconButton(icon = GenesysIcons.ArrowDown, enabled = !isLast, onClick = onMoveDown)
                GenesysIconButton(icon = GenesysIcons.Edit, onClick = onEdit)
                GenesysIconButton(icon = GenesysIcons.Delete, tint = MaterialTheme.colorScheme.error, onClick = onRemove)
            }
        }
    }
}
