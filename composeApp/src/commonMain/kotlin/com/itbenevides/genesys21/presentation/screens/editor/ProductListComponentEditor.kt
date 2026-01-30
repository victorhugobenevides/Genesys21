package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.input.GenesysTextField // IMPORT FALTANTE
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons

/**
 * Editor especializado para listas de produtos.
 */
@Composable
fun ProductListComponentEditor(
    component: PageComponent.ProductList,
    onEditProduct: (Product?) -> Unit,
    onSaveLabel: (String) -> Unit
) {
    var customLabel by remember { mutableStateOf(component.customLabel ?: "") }

    GenesysColumn(usePadding = false) {
        GenesysText("Gerenciamento de Produtos", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
        GenesysSpacer(GenesysSpacing.Small)
        
        GenesysLoadingButton(
            text = "Cadastrar Novo Produto",
            icon = GenesysIcons.Add,
            onClick = { onEditProduct(null) },
            fillWidth = true
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 300.dp), useScroll = true) {
            component.products.forEach { product ->
                GenesysCard(
                    modifier = Modifier.padding(bottom = 4.dp),
                    onClick = { onEditProduct(product) }
                ) {
                    GenesysRow {
                        GenesysWeightBox(1f) {
                            GenesysText(product.name)
                        }
                        GenesysIconButton(icon = GenesysIcons.Edit, onClick = { onEditProduct(product) })
                    }
                }
            }
        }
        
        GenesysSpacer(GenesysSpacing.Large)
        
        // Campo agora reconhecido pelo compilador
        GenesysTextField(
            value = customLabel,
            onValueChange = { customLabel = it },
            label = "Rótulo da Seção",
            placeholder = "Ex: Novidades",
            icon = GenesysIcons.Edit
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysLoadingButton(text = "Salvar Rótulo", fillWidth = true, onClick = {
            onSaveLabel(customLabel)
        })
    }
}
