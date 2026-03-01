package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.model.StepItem
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.badge.GenesysBadge
import com.itbenevides.genesys21.ui.components.badge.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.badge.GenesysStockBadge
import com.itbenevides.genesys21.ui.components.button.GenesysFab
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.feedback.GenesysConfirmDialog
import com.itbenevides.genesys21.ui.components.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.feedback.GenesysLoadingIndicator
import com.itbenevides.genesys21.ui.components.feedback.GenesysLoadingOverlay
import com.itbenevides.genesys21.ui.components.feedback.GenesysTrackingTimeline
import com.itbenevides.genesys21.ui.components.image.GenesysAvatar
import com.itbenevides.genesys21.ui.components.image.GenesysColorCircle
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.input.GenesysDropdownField
import com.itbenevides.genesys21.ui.components.input.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.input.GenesysOutlinedTextField
import com.itbenevides.genesys21.ui.components.input.GenesysPhotoPicker
import com.itbenevides.genesys21.ui.components.input.GenesysQuantitySelector
import com.itbenevides.genesys21.ui.components.input.GenesysSearchBar
import com.itbenevides.genesys21.ui.components.input.GenesysSlider
import com.itbenevides.genesys21.ui.components.input.GenesysStatusPicker
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysBox
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysDivider
import com.itbenevides.genesys21.ui.components.layout.GenesysLazyColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysLazyColumnIndexed
import com.itbenevides.genesys21.ui.components.layout.GenesysLazyRow
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysRow
import com.itbenevides.genesys21.ui.components.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.layout.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.layout.GenesysWeightSpacer
import com.itbenevides.genesys21.ui.components.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Cobertura total: todos os componentes + variações de tema/estado.
 */
class AllComponentsConfigScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    private fun sampleProducts() = listOf(
        Product(id = "p1", name = "Produto 1", price = 99.9, categoryName = "Eletrônicos", imageUrls = listOf("https://picsum.photos/200/200")),
        Product(id = "p2", name = "Produto 2", price = 49.9, categoryName = "Roupas", imageUrls = listOf("https://picsum.photos/200/201"))
    )

    @Test
    fun testButtonsAndBadgesStates() {
        paparazzi.snapshot(name = "buttons_badges_states") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysLoadingButton(text = "Primário", onClick = {})
                        GenesysLoadingButton(text = "Loading", onClick = {}, isLoading = true)
                        GenesysLoadingButton(text = "Desabilitado", onClick = {}, enabled = false)
                        GenesysLoadingButton(text = "Com ícone", onClick = {}, icon = GenesysIcons.Check)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysIconButton(icon = GenesysIcons.Search, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.Edit, onClick = {})
                            GenesysFab(icon = GenesysIcons.Add, onClick = {})
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysTextButton(text = "Cancelar", onClick = {})
                            GenesysTextButton(text = "Confirmar", onClick = {})
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysBadge(label = "NOVO", color = MaterialTheme.colorScheme.primary)
                            GenesysBadge(label = "5", color = MaterialTheme.colorScheme.error)
                            GenesysBadge(label = "HOT", color = MaterialTheme.colorScheme.tertiary, showDot = false)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysStatusBadge(status = OrderStatus.PENDING)
                            GenesysStatusBadge(status = OrderStatus.PROCESSING)
                            GenesysStatusBadge(status = OrderStatus.COMPLETED)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysStockBadge(stock = 15)
                            GenesysStockBadge(stock = 3)
                            GenesysStockBadge(stock = 0)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testCardsAndTextStyles() {
        paparazzi.snapshot(name = "cards_text_styles") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysCard {
                            GenesysText("Card padrão", style = GenesysTextStyle.Title)
                            GenesysText("Texto de apoio", style = GenesysTextStyle.Body)
                        }
                        GenesysCard(onClick = {}, backgroundColor = MaterialTheme.colorScheme.secondaryContainer) {
                            GenesysText("Card clicável", style = GenesysTextStyle.Body)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysStatsCard(label = "Vendas", value = "R$ 12.450", color = MaterialTheme.colorScheme.primary)
                            GenesysStatsCard(label = "Pedidos", value = "328", color = MaterialTheme.colorScheme.secondary)
                        }
                        GenesysText("Headline", style = GenesysTextStyle.Headline)
                        GenesysText("Title", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                        GenesysText("Body", style = GenesysTextStyle.Body)
                        GenesysText("Label", style = GenesysTextStyle.Label)
                        GenesysText("Error", style = GenesysTextStyle.Error)
                    }
                }
            }
        }
    }

    @Test
    fun testInputsAllConfigs() {
        paparazzi.snapshot(name = "inputs_all_configs") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysTextField(value = "", onValueChange = {}, label = "Nome", placeholder = "Digite seu nome")
                        GenesysTextField(value = "", onValueChange = {}, label = "Email", isError = true, supportingText = "Obrigatório")

                        GenesysOutlinedTextField(value = "", onValueChange = {}, label = "Senha", isPasswordToggleEnabled = true)
                        GenesysOutlinedTextField(value = "", onValueChange = {}, label = "Erro", isError = true, errorMessage = "Senha inválida")

                        GenesysDropdownField(
                            value = "",
                            onValueChange = {},
                            label = "Categoria",
                            options = listOf("Eletrônicos", "Roupas", "Acessórios"),
                            placeholder = "Selecione"
                        )

                        GenesysStatusPicker(currentStatus = OrderStatus.PENDING, onStatusSelected = {})

                        GenesysSlider(value = 120f, onValueChange = {}, label = "Tamanho", valueRange = 50f..300f)

                        GenesysSearchBar(value = "", onValueChange = {}, placeholder = "Buscar...")

                        GenesysQuantitySelector(quantity = 2, onIncrease = {}, onDecrease = {})

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysFilterChip(label = "Todos", selected = true, onClick = {})
                            GenesysFilterChip(label = "Ativos", selected = false, onClick = {})
                            GenesysFilterChip(label = "Inativos", selected = false, onClick = {})
                        }

                        GenesysPhotoPicker(
                            urls = listOf("https://picsum.photos/100/100"),
                            onAddClick = {},
                            onRemoveClick = {},
                            isUploading = false,
                            maxPhotos = 5
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testImagesAndFeedback() {
        paparazzi.snapshot(name = "images_feedback") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GenesysAvatar(icon = GenesysIcons.Person)
                            Spacer(Modifier.width(8.dp))
                            GenesysText("Avatar", style = GenesysTextStyle.Body)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysColorCircle(color = MaterialTheme.colorScheme.primary)
                            GenesysColorCircle(color = MaterialTheme.colorScheme.secondary)
                            GenesysColorCircle(color = MaterialTheme.colorScheme.tertiary)
                        }

                        GenesysText("Imagens", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            GenesysImage(url = "", size = 64.dp)
                            GenesysImage(url = "https://picsum.photos/100/100", size = 64.dp)
                            GenesysImage(url = "https://picsum.photos/120/120", size = 64.dp, isCircular = true)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            GenesysImage(url = "https://picsum.photos/200/140", size = 96.dp, isCircular = false)
                            GenesysImage(
                                url = "https://picsum.photos/160/160",
                                size = 80.dp,
                                isCircular = false,
                                shape = MaterialTheme.shapes.large
                            )
                        }

                        GenesysEmptyState(
icon = GenesysIcons.Search,
                            title = "Nada encontrado",
                            description = "Tente outra busca",
                            action = { GenesysTextButton(text = "Adicionar", onClick = {}) }
                        )

                        GenesysLoadingIndicator(size = 32.dp)
                        Box(modifier = Modifier.size(100.dp)) { GenesysLoadingOverlay() }

                        GenesysTrackingTimeline(currentStatus = OrderStatus.PROCESSING)
                    }
                }
            }
        }
    }

    @Test
    fun testImageComponentVariations() {
        paparazzi.snapshot(name = "image_placeholder") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Imagem (Placeholder)", style = GenesysTextStyle.Title)
                        GenesysImage(url = "", size = 72.dp)
                    }
                }
            }
        }

        paparazzi.snapshot(name = "image_rectangular") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Imagem Retangular", style = GenesysTextStyle.Title)
                        GenesysImage(url = "https://picsum.photos/240/160", size = 96.dp, isCircular = false)
                    }
                }
            }
        }

        paparazzi.snapshot(name = "image_circular") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Imagem Circular", style = GenesysTextStyle.Title)
                        GenesysImage(url = "https://picsum.photos/160/160", size = 80.dp, isCircular = true)
                    }
                }
            }
        }

        paparazzi.snapshot(name = "image_custom_shape") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Imagem com Shape", style = GenesysTextStyle.Title)
                        GenesysImage(
                            url = "https://picsum.photos/200/200",
                            size = 88.dp,
                            isCircular = false,
                            shape = MaterialTheme.shapes.large
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testDialogsAndBottomSheet() {
paparazzi.snapshot(name = "dialogs_bottomsheet") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysDialog(
                            onDismissRequest = {},
                            title = "Título",
                            confirmButton = { GenesysTextButton(text = "OK", onClick = {}) },
                            dismissButton = { GenesysTextButton(text = "Cancelar", onClick = {}) }
                        ) {
                            GenesysText("Conteúdo do diálogo")
                        }

                        GenesysConfirmDialog(
                            onDismissRequest = {},
                            title = "Confirmar",
                            text = "Deseja excluir?",
                            icon = GenesysIcons.Check,
                            confirmButton = { GenesysTextButton(text = "Sim", onClick = {}) },
                            dismissButton = { GenesysTextButton(text = "Não", onClick = {}) }
                        )

                        GenesysBottomSheet(
                            onDismiss = {},
                            title = "Bottom Sheet",
                            actions = { GenesysIconButton(icon = GenesysIcons.Close, onClick = {}) }
                        ) {
                            GenesysText("Conteúdo do sheet")
                            GenesysSpacer(GenesysSpacing.Medium)
                            GenesysLoadingButton(text = "Ação", onClick = {}, fillWidth = true)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testTopAppBarVariants() {
        paparazzi.snapshot(name = "top_app_bar_variants") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysTopAppBar(
                            title = "Página Pública",
                            onBack = {},
                            actions = {
                                GenesysIconButton(icon = GenesysIcons.List, onClick = {})
                                GenesysIconButton(icon = GenesysIcons.ShoppingBag, onClick = {})
                            }
                        )

                        GenesysTopAppBar(
                            title = "Admin",
                            onBack = {},
                            actions = {
                                GenesysIconButton(icon = GenesysIcons.Search, onClick = {})
                                GenesysIconButton(icon = GenesysIcons.Settings, onClick = {})
                            }
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testSectionHeaderVariants() {
        paparazzi.snapshot(name = "section_header_variants") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysSectionHeader(
                            title = "Produtos em Destaque",
                            subtitle = "Ofertas limitadas",
                            trailingContent = { GenesysTextButton(text = "Ver Todos", onClick = {}) }
                        )

                        GenesysSectionHeader(
                            title = "Últimos Pedidos",
                            subtitle = null,
                            trailingContent = { GenesysBadge(label = "3", color = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testLayoutsAndNavigation() {
        paparazzi.snapshot(name = "layouts_navigation") {
            AppTheme(themeConfig = PageThemeConfig.MINIMAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysPage(topBar = { Text("TopBar") }, content = { Text("Conteúdo") })

                        GenesysBox(contentAlignment = Alignment.Center) {
                            Text("GenesysBox")
                        }

                        GenesysCard {
                            GenesysColumn {
                                Text("Coluna")
                                GenesysDivider()
                                Text("Conteúdo")
                            }
                        }

                        GenesysRow {
                            Text("Row")
                            GenesysSpacer(GenesysSpacing.Medium)
                            Text("Item")
                        }

                        GenesysLazyColumn(
                            items = listOf("A", "B", "C"),
                            usePadding = false,
                            spacing = GenesysSpacing.Small
                        ) { item ->
                            Text("Item $item")
                        }

                        GenesysLazyRow(items = listOf("1", "2", "3")) { item ->
                            GenesysBadge(label = item, color = MaterialTheme.colorScheme.primary)
                        }

                        GenesysLazyColumnIndexed(
                            items = listOf("X", "Y"),
                            usePadding = false
                        ) { index, item ->
                            Text("$index - $item")
                        }

                        GenesysRow {
                            GenesysWeightBox(1f) { Text("Weight 1") }
                            GenesysWeightSpacer(0.5f)
                            GenesysWeightBox(1f) { Text("Weight 2") }
                        }

                        GenesysTabRow(
                            selectedTabIndex = 0,
                            tabs = listOf(
                                GenesysTabData("Produtos", badgeCount = 2),
                                GenesysTabData("Pedidos")
                            ),
                            onTabSelected = {}
                        )

                        GenesysPagerIndicator(count = 4, currentPage = 1)
                    }
                }
            }
        }
    }

    @Test
    fun testWhiteLabelComponentsAllConfigs() {
        val components = listOf(
            PageComponent.Typography(text = "Texto", fontSize = 16),
            PageComponent.Header(title = "Título", fontSize = 24),
            PageComponent.Media(url = "https://picsum.photos/800/400", title = "Mídia", layout = "FULL_WIDTH"),
            PageComponent.Image(url = "https://picsum.photos/200/200", isCircular = true),
            PageComponent.Highlight(text = "Destaque", usePrimaryColor = true),
            PageComponent.ProductList(products = sampleProducts(), isHorizontal = true),
            PageComponent.CategoryFilter(),
            PageComponent.StepProcess(steps = listOf(StepItem("Passo 1", "Descrição"))),
            PageComponent.Testimonial(quote = "Excelente!", author = "Cliente"),
            PageComponent.SocialLinks(instagram = "@loja", whatsapp = "5511999999999", email = "contato@loja.com"),
            PageComponent.ProfileHeader(imageUrl = "https://picsum.photos/100/100", name = "Nome", bio = "Bio"),
            PageComponent.Search(placeholder = "Buscar")
        )

        paparazzi.snapshot(name = "whitelabel_all_components") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        components.forEach { component ->
                            PageComponentRenderer(component = component, allComponents = components, isEditMode = true)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testThemesAndStates() {
        val themes = listOf(
            PageThemeConfig.ROYAL,
            PageThemeConfig.OCEAN,
            PageThemeConfig.FOREST,
            PageThemeConfig.DARK_MODE
        )

        themes.forEach { theme ->
            paparazzi.snapshot(name = "theme_${theme.name}") {
                AppTheme(themeConfig = theme) {
                    Surface(modifier = Modifier.padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysText("Tema ${theme.name}", style = GenesysTextStyle.Title)
                            GenesysLoadingButton(text = "Botão", onClick = {})
                            GenesysBadge(label = "Badge", color = MaterialTheme.colorScheme.primary)
                            GenesysTextField(value = "", onValueChange = {}, label = "Input")
                        }
                    }
                }
            }
        }
    }
}
