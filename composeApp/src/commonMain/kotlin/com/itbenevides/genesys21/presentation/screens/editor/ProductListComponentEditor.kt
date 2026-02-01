package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        GenesysText(GenesysStrings.ManageProducts, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysSpacer(GenesysSpacing.Medium)

        GenesysTabRow(
            selectedTabIndex = selectedTab,
            tabs = listOf(
                GenesysTabData(GenesysStrings.TabProductsInList, GenesysIcons.List, component.products.size),
                GenesysTabData(GenesysStrings.TabProductsInCatalog, GenesysIcons.Inventory)
            ),
            onTabSelected = { selectedTab = it }
        )

        GenesysSpacer(GenesysSpacing.Medium)

        // Campo de busca para filtrar produtos nas abas
        GenesysTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = GenesysStrings.SearchPlaceholder,
            icon = GenesysIcons.Search
        )

        GenesysSpacer(GenesysSpacing.Medium)

        if (selectedTab == 0) {
            // ABA 1: Produtos Atuais
            val filteredListProducts = component.products.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true) || 
                (product.categoryName?.contains(searchQuery, ignoreCase = true) == true)
            }

            GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 300.dp), useScroll = true) {
                filteredListProducts.forEach { product ->
                    val index = component.products.indexOf(product)
                    GenesysCard(modifier = Modifier.padding(bottom = 4.dp)) {
                        GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                            GenesysWeightBox(1f) { GenesysText(product.name) }
                            
                            GenesysRow(fillWidth = false) {
                                GenesysIconButton(
                                    icon = GenesysIcons.ArrowUp, 
                                    onClick = { 
                                        if (index > 0) {
                                            val newList = component.products.toMutableList()
                                            val temp = newList[index]
                                            newList[index] = newList[index - 1]
                                            newList[index - 1] = temp
                                            onProductsUpdated(newList)
                                        }
                                    }
                                )
                                GenesysIconButton(
                                    icon = GenesysIcons.ArrowDown, 
                                    onClick = { 
                                        if (index < component.products.size - 1) {
                                            val newList = component.products.toMutableList()
                                            val temp = newList[index]
                                            newList[index] = newList[index + 1]
                                            newList[index + 1] = temp
                                            onProductsUpdated(newList)
                                        }
                                    }
                                )
                                GenesysIconButton(icon = GenesysIcons.Edit, onClick = { onEditProduct(product) })
                                GenesysIconButton(
                                    icon = GenesysIcons.Remove, 
                                    tint = Color.Red.copy(alpha = 0.6f),
                                    onClick = { onProductsUpdated(component.products.filter { it.id != product.id }) }
                                )
                            }
                        }
                    }
                }
                GenesysSpacer(GenesysSpacing.Medium)
                GenesysLoadingButton(
                    text = GenesysStrings.AddNewProduct,
                    icon = GenesysIcons.Add,
                    onClick = { onEditProduct(null) },
                    fillWidth = true
                )
            }
        } else {
            // ABA 2: Catálogo Global
            val catalogToDisplay = allAvailableProducts
                .filter { p -> component.products.none { it.id == p.id } }
                .filter { product -> 
                    product.name.contains(searchQuery, ignoreCase = true) || 
                    (product.categoryName?.contains(searchQuery, ignoreCase = true) == true)
                }
            
            GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 300.dp), useScroll = true) {
                if (catalogToDisplay.isEmpty()) {
                    GenesysText(GenesysStrings.NoProductsInCatalog, style = GenesysTextStyle.Label)
                } else {
                    catalogToDisplay.forEach { product ->
                        GenesysCard(modifier = Modifier.padding(bottom = 4.dp)) {
                            GenesysRow {
                                GenesysWeightBox(1f) { GenesysText(product.name) }
                                GenesysLoadingButton(
                                    text = GenesysStrings.AddToThisList,
                                    icon = GenesysIcons.Add,
                                    onClick = { onProductsUpdated(component.products + product) }
                                )
                            }
                        }
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysDivider()
        GenesysSpacer(GenesysSpacing.Large)

        GenesysTextField(
            value = customLabel,
            onValueChange = { customLabel = it },
            label = GenesysStrings.BlockNameLabel,
            placeholder = GenesysStrings.BlockNamePlaceholder,
            icon = GenesysIcons.Edit
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysRow(verticalAlignment = Alignment.CenterVertically) {
            GenesysText(GenesysStrings.HorizontalListLabel, weightValue = 1f)
            Switch(
                checked = isHorizontal,
                onCheckedChange = { isHorizontal = it }
            )
        }
        
        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysLoadingButton(text = GenesysStrings.SaveLabel, fillWidth = true, onClick = {
            onSaveLabel(customLabel, isHorizontal)
        })
    }
}
