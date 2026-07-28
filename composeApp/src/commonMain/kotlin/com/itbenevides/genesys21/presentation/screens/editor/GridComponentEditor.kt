package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard

@Composable
fun GridComponentEditor(
    component: PageComponent.Grid,
    allPageComponents: List<PageComponent> = emptyList(),
    allProducts: List<Product> = emptyList(),
    allServices: List<BookingService> = emptyList(),
    onSave: (PageComponent.Grid) -> Unit
) {
    var columns by remember { mutableStateOf(component.columns.toString()) }
    var title by remember { mutableStateOf(component.title ?: "") }
    var items by remember { mutableStateOf(component.items) }
    var showAddMenuIndex by remember { mutableStateOf<Int?>(null) }
    var editingChildInfo by remember { mutableStateOf<Triple<Int, Int, PageComponent>?>(null) }

    GenesysColumn(usePadding = false) {
        // 1. CONFIGURAÇÕES ESTRUTURAIS
        GenesysCard(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            elevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                GenesysText("Estrutura da Grade", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysSpacing.Medium)

                GenesysTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título da Seção (Opcional)",
                    placeholder = "Ex: Galeria de Destaques",
                    icon = GenesysIcons.Edit
                )

                GenesysSpacer(GenesysSpacing.Medium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(GenesysIcons.ViewModule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Colunas:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))

                    GenesysRow(fillWidth = false) {
                        listOf(1, 2, 3, 4).forEach { num ->
                            FilterChip(
                                selected = columns == num.toString(),
                                onClick = { columns = num.toString() },
                                label = { Text(num.toString()) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Large)

        // 2. GESTÃO VISUAL DAS CÉLULAS
        GenesysText("Células da Grade", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
        GenesysText("Cada célula pode conter múltiplos componentes.", style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.outline)
        GenesysSpacer(GenesysSpacing.Medium)

        GenesysColumn(
            usePadding = false,
            modifier = Modifier.heightIn(max = 500.dp),
            useScroll = true
        ) {
            items.forEachIndexed { index, gridItem ->
                CellItemCard(
                    index = index,
                    gridItem = gridItem,
                    maxColumns = columns.toIntOrNull() ?: 2,
                    onAddComponent = { showAddMenuIndex = index },
                    onEditChild = { childIndex, childComp ->
                        editingChildInfo = Triple(index, childIndex, childComp)
                    },
                    onUpdateSpan = { newSpan ->
                        val newItems = items.toMutableList()
                        newItems[index] = gridItem.copy(span = newSpan)
                        items = newItems
                    },
                    onRemoveCell = {
                        items = items.toMutableList().apply { removeAt(index) }
                    },
                    onRemoveChild = { childIndex ->
                        val newItems = items.toMutableList()
                        val newComponents = gridItem.components.toMutableList().apply { removeAt(childIndex) }
                        newItems[index] = gridItem.copy(components = newComponents)
                        items = newItems
                    },
                    onMoveUp = {
                        if (index > 0) {
                            items = items.toMutableList().apply {
                                val tmp = this[index]
                                this[index] = this[index - 1]
                                this[index - 1] = tmp
                            }
                        }
                    },
                    onMoveDown = {
                        if (index < items.size - 1) {
                            items = items.toMutableList().apply {
                                val tmp = this[index]
                                this[index] = this[index + 1]
                                this[index + 1] = tmp
                            }
                        }
                    }
                )
                GenesysSpacer(GenesysSpacing.Small)
            }

            OutlinedButton(
                onClick = { items = items + PageComponent.GridItem() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(GenesysIcons.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Adicionar Nova Célula")
            }
        }

        GenesysSpacer(GenesysSpacing.Huge)

        GenesysLoadingButton(
            text = "Salvar Grade",
            onClick = {
                onSave(
                    component.copy(
                        columns = columns.toIntOrNull() ?: 2,
                        title = title.ifBlank { null },
                        items = items
                    )
                )
            },
            fillWidth = true
        )
    }

    if (showAddMenuIndex != null) {
        AddChildComponentDialog(
            allPageComponents = allPageComponents,
            allProducts = allProducts,
            allServices = allServices,
            onDismiss = { showAddMenuIndex = null },
            onSelect = { newComponent ->
                val index = showAddMenuIndex!!
                val newItems = items.toMutableList()
                val newComponents = newItems[index].components + newComponent
                newItems[index] = newItems[index].copy(components = newComponents)
                items = newItems
            }
        )
    }

    if (editingChildInfo != null) {
        val (itemIdx, childIdx, childComp) = editingChildInfo!!
        AlertDialog(
            onDismissRequest = { editingChildInfo = null },
            title = { Text("Editar ${childComp::class.simpleName ?: "Bloco"}") },
            text = {
                Box(Modifier.heightIn(max = 500.dp)) {
                    ChildComponentEditor(
                        component = childComp,
                        allPageComponents = allPageComponents,
                        onSave = { updated ->
                            val newItems = items.toMutableList()
                            val newComponents = newItems[itemIdx].components.toMutableList().apply {
                                set(childIdx, updated)
                            }
                            newItems[itemIdx] = newItems[itemIdx].copy(components = newComponents)
                            items = newItems
                            editingChildInfo = null
                        }
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun CellItemCard(
    index: Int,
    gridItem: PageComponent.GridItem,
    maxColumns: Int,
    onAddComponent: () -> Unit,
    onEditChild: (Int, PageComponent) -> Unit,
    onUpdateSpan: (Int) -> Unit,
    onRemoveCell: () -> Unit,
    onRemoveChild: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    GenesysCard(
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text((index + 1).toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("Posição ${index + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

                // Seletor de Largura (Span)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    Text("Largura:", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(4.dp))
                    listOf(1, maxColumns).distinct().forEach { s ->
                        FilterChip(
                            selected = gridItem.span == s,
                            onClick = { onUpdateSpan(s) },
                            label = { Text(if (s == 1) "1/2" else "Full") },
                            modifier = Modifier.scale(0.8f).padding(0.dp)
                        )
                    }
                }

                GenesysIconButton(icon = GenesysIcons.ArrowUp, onClick = onMoveUp, modifier = Modifier.size(28.dp))
                GenesysIconButton(icon = GenesysIcons.ArrowDown, onClick = onMoveDown, modifier = Modifier.size(28.dp))
                GenesysIconButton(icon = GenesysIcons.Delete, onClick = onRemoveCell, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
            }

            GenesysSpacer(GenesysSpacing.Small)
            HorizontalDivider(modifier = Modifier.alpha(0.3f))
            GenesysSpacer(GenesysSpacing.Small)

            if (gridItem.components.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAddComponent() }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(GenesysIcons.Add, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Adicionar conteúdo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                gridItem.components.forEachIndexed { childIndex, child ->
                    GenesysRow(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        usePadding = false
                    ) {
                        val icon = when (child) {
                            is PageComponent.Text -> GenesysIcons.Edit
                            is PageComponent.Image -> GenesysIcons.Image
                            is PageComponent.Button -> GenesysIcons.Language
                            is PageComponent.SingleProduct -> GenesysIcons.Inventory
                            is PageComponent.SingleService -> GenesysIcons.Schedule
                            else -> GenesysIcons.Magic
                        }
                        Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = child::class.simpleName ?: "Componente",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f)
                        )

                        GenesysIconButton(
                            icon = GenesysIcons.Edit,
                            onClick = { onEditChild(childIndex, child) },
                            modifier = Modifier.size(24.dp)
                        )

                        GenesysIconButton(
                            icon = GenesysIcons.Close,
                            onClick = { onRemoveChild(childIndex) },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                TextButton(
                    onClick = onAddComponent,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(GenesysIcons.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Adicionar mais", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun AddChildComponentDialog(
    allPageComponents: List<PageComponent>,
    allProducts: List<Product>,
    allServices: List<BookingService>,
    onDismiss: () -> Unit,
    onSelect: (PageComponent) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar ao Item") },
        text = {
            Column(modifier = Modifier.heightIn(max = 500.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Novos") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Existentes") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Produtos") })
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Serviços") })
                }

                Spacer(Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        val options = listOf(
                            "Texto" to { PageComponent.Text(content = "Novo Texto") },
                            "Cabeçalho" to { PageComponent.Header(title = "Novo Cabeçalho") },
                            "Imagem" to { PageComponent.Image(url = "https://picsum.photos/200") },
                            "Botão" to { PageComponent.Button(text = "Botão", url = "#") },
                            "Grade Layout" to { PageComponent.Grid(columns = 2) },
                            "Lista Produtos" to { PageComponent.ProductList() },
                            "Links Sociais" to { PageComponent.SocialLinks() },
                            "Lista Serviços" to { PageComponent.ServiceList() },
                            "Banner Hero" to { PageComponent.Hero(title = "Destaque", imageUrl = "https://picsum.photos/1200/600") }
                        )
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(options.size) { i ->
                                val (name, factory) = options[i]
                                OutlinedButton(
                                    onClick = { onSelect(factory()) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(name, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    1 -> {
                        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
                            items(allPageComponents.size) { i ->
                                val comp = allPageComponents[i]
                                if (comp !is PageComponent.Grid) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        onClick = { onSelect(comp) }
                                    ) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${comp::class.simpleName}: ${comp.customLabel ?: "Sem nome"}",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Icon(GenesysIcons.Add, null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
                            items(allProducts.size) { i ->
                                val prod = allProducts[i]
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    onClick = { onSelect(PageComponent.SingleProduct(product = prod)) }
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(prod.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                        Icon(GenesysIcons.Add, null)
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
                            items(allServices.size) { i ->
                                val serv = allServices[i]
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    onClick = { onSelect(PageComponent.SingleService(service = serv)) }
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(serv.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                        Icon(GenesysIcons.Add, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Concluir") }
        }
    )
}

@Composable
private fun ChildComponentEditor(
    component: PageComponent,
    allPageComponents: List<PageComponent>,
    onSave: (PageComponent) -> Unit
) {
    GenesysColumn(usePadding = false, useScroll = true) {
        when (component) {
            is PageComponent.Header -> HeaderComponentEditor(component, onSave)
            is PageComponent.Text -> TextComponentEditor(component, onSave)
            is PageComponent.Button -> ButtonComponentEditor(component, onSave)
            is PageComponent.Image -> {
                ImageComponentEditor(component, emptyList(), false, {}, onSave)
            }
            is PageComponent.SingleProduct -> {
                // Editor simples para produto único (apenas toggle de preço)
                Column {
                    Text("Exibir Preço", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = component.showPrice, onCheckedChange = {
                        onSave(component.copy(showPrice = it))
                    })
                }
            }
            is PageComponent.Grid -> GridComponentEditor(component, allPageComponents, onSave = onSave)
            else -> {
                Text("Editor não disponível para este tipo dentro da grade.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { onSave(component) }) { Text("Fechar") }
            }
        }
    }
}
