package com.itbenevides.genesys21.screenshot.components

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.PageThemeConfig
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
import com.itbenevides.genesys21.ui.components.feedback.*
import com.itbenevides.genesys21.ui.components.image.GenesysAvatar
import com.itbenevides.genesys21.ui.components.image.GenesysColorCircle
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.input.*
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.screenshot.base.TestImageProvider
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes específicos para componentes individuais com múltiplos estados e variações.
 * Cada teste cobre um componente isolado com todas as suas variações.
 */
class ComponentIsolatedScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testGenesysLoadingButtonAllStates() {
        paparazzi.snapshot(name = "button_loading_all_states") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Botão Primário", style = GenesysTextStyle.Title)
                        GenesysLoadingButton(text = "Normal", onClick = {})
                        GenesysLoadingButton(text = "Loading", onClick = {}, isLoading = true)
                        GenesysLoadingButton(text = "Desabilitado", onClick = {}, enabled = false)
                        GenesysLoadingButton(text = "Com Ícone", onClick = {}, icon = GenesysIcons.Check)
                        GenesysLoadingButton(text = "Largo", onClick = {}, fillWidth = true)
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysTextFieldAllStates() {
        paparazzi.snapshot(name = "textfield_all_states") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("TextField", style = GenesysTextStyle.Title)
                        GenesysTextField(
                            value = "",
                            onValueChange = {},
                            label = "Vazio",
                            placeholder = "Digite aqui"
                        )
                        GenesysTextField(
                            value = "Texto preenchido",
                            onValueChange = {},
                            label = "Preenchido"
                        )
                        GenesysTextField(
                            value = "",
                            onValueChange = {},
                            label = "Com Erro",
                            isError = true,
                            supportingText = "Campo obrigatório"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysOutlinedTextFieldAllStates() {
        paparazzi.snapshot(name = "outlined_textfield_all_states") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Outlined TextField", style = GenesysTextStyle.Title)
                        GenesysOutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = "Senha",
                            isPasswordToggleEnabled = true
                        )
                        GenesysOutlinedTextField(
                            value = "Texto",
                            onValueChange = {},
                            label = "Normal"
                        )
                        GenesysOutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = "Erro",
                            isError = true,
                            errorMessage = "Valor inválido"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysCardAllVariations() {
        paparazzi.snapshot(name = "card_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Cards", style = GenesysTextStyle.Title)
                        GenesysCard {
                            Column(Modifier.padding(16.dp)) {
                                GenesysText("Card Padrão", style = GenesysTextStyle.Title)
                                GenesysText("Conteúdo do card", style = GenesysTextStyle.Body)
                            }
                        }
                        GenesysCard(
                            onClick = {},
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                GenesysText("Card Clicável", style = GenesysTextStyle.Title)
                                GenesysText("Toque para interagir", style = GenesysTextStyle.Body)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysStatsCardAllVariations() {
        paparazzi.snapshot(name = "stats_card_variations") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Stats Cards", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysStatsCard(label = "Vendas", value = "R$ 12.450", color = MaterialTheme.colorScheme.primary)
                            GenesysStatsCard(label = "Pedidos", value = "328", color = MaterialTheme.colorScheme.secondary)
                            GenesysStatsCard(label = "Clientes", value = "1.234", color = MaterialTheme.colorScheme.tertiary)
                        }
                        GenesysStatsCard(label = "Total Geral", value = "R$ 45.678,90", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysBadgeAllStyles() {
        paparazzi.snapshot(name = "badge_all_styles") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Badges", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysBadge(label = "NOVO", color = MaterialTheme.colorScheme.primary)
                            GenesysBadge(label = "5", color = MaterialTheme.colorScheme.error)
                            GenesysBadge(label = "HOT", color = MaterialTheme.colorScheme.tertiary, showDot = false)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysBadge(label = "SUCCESS", color = Color.Green, showDot = true)
                            GenesysBadge(label = "WARNING", color = Color.Yellow, showDot = true)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysStatusBadgeAllStatuses() {
        paparazzi.snapshot(name = "status_badge_all_statuses") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Status Badges", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysStatusBadge(status = OrderStatus.PENDING)
                            GenesysStatusBadge(status = OrderStatus.PAYMENT_PENDING)
                            GenesysStatusBadge(status = OrderStatus.PROCESSING)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysStatusBadge(status = OrderStatus.COMPLETED)
                            GenesysStatusBadge(status = OrderStatus.CANCELLED)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysStockBadgeAllLevels() {
        paparazzi.snapshot(name = "stock_badge_all_levels") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Stock Badges", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysStockBadge(stock = 50)
                            GenesysStockBadge(stock = 10)
                            GenesysStockBadge(stock = 3)
                            GenesysStockBadge(stock = 0)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysImageAllVariations() {
        paparazzi.snapshot(name = "image_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Imagem - Placeholder", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            GenesysImage(url = "", size = 64.dp)
                            GenesysImage(url = "", size = 80.dp)
                            GenesysImage(url = "", size = 96.dp)
                        }
                        GenesysText("Imagem - Retangular", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            GenesysImage(url = TestImageProvider.mockImageUrl(), size = 64.dp, isCircular = false)
                            GenesysImage(url = TestImageProvider.mockImageUrl(), size = 80.dp, isCircular = false)
                        }
                        GenesysText("Imagem - Circular", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            GenesysImage(url = TestImageProvider.mockImageUrl(), size = 64.dp, isCircular = true)
                            GenesysImage(url = TestImageProvider.mockImageUrl(), size = 80.dp, isCircular = true)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysAvatar() {
        paparazzi.snapshot(name = "avatar_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Avatar", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            GenesysAvatar(icon = GenesysIcons.Person)
                            GenesysAvatar(icon = GenesysIcons.Person, backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            GenesysAvatar(icon = GenesysIcons.Person, backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysColorCircleAllColors() {
        paparazzi.snapshot(name = "color_circle_all_colors") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Color Circles", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysColorCircle(color = MaterialTheme.colorScheme.primary, size = 32.dp)
                            GenesysColorCircle(color = MaterialTheme.colorScheme.secondary, size = 32.dp)
                            GenesysColorCircle(color = MaterialTheme.colorScheme.tertiary, size = 32.dp)
                            GenesysColorCircle(color = MaterialTheme.colorScheme.error, size = 32.dp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysColorCircle(color = Color.Red, size = 40.dp)
                            GenesysColorCircle(color = Color.Green, size = 40.dp)
                            GenesysColorCircle(color = Color.Blue, size = 40.dp)
                            GenesysColorCircle(color = Color.Yellow, size = 40.dp)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysQuantitySelectorAllStates() {
        paparazzi.snapshot(name = "quantity_selector_all_states") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Quantity Selector", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            GenesysQuantitySelector(quantity = 1, onIncrease = {}, onDecrease = {})
                            GenesysQuantitySelector(quantity = 5, onIncrease = {}, onDecrease = {})
                            GenesysQuantitySelector(quantity = 10, onIncrease = {}, onDecrease = {})
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysSearchBarAllStates() {
        paparazzi.snapshot(name = "search_bar_all_states") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Search Bar", style = GenesysTextStyle.Title)
                        GenesysSearchBar(value = "", onValueChange = {}, placeholder = "Buscar produtos...")
                        GenesysSearchBar(value = "Fone", onValueChange = {}, placeholder = "Buscar...")
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysFilterChipAllStates() {
        paparazzi.snapshot(name = "filter_chip_all_states") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Filter Chips", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysFilterChip(label = "Todos", selected = true, onClick = {})
                            GenesysFilterChip(label = "Ativos", selected = false, onClick = {})
                            GenesysFilterChip(label = "Inativos", selected = false, onClick = {})
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysDropdownAllStates() {
        paparazzi.snapshot(name = "dropdown_all_states") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Dropdown Field", style = GenesysTextStyle.Title)
                        GenesysDropdownField(
                            value = "",
                            onValueChange = {},
                            label = "Categoria",
                            options = listOf("Eletrônicos", "Roupas", "Acessórios"),
                            placeholder = "Selecione"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysSliderAllValues() {
        paparazzi.snapshot(name = "slider_all_values") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Slider", style = GenesysTextStyle.Title)
                        GenesysSlider(value = 50f, onValueChange = {}, label = "Valor Mínimo", valueRange = 0f..100f)
                        GenesysSlider(value = 75f, onValueChange = {}, label = "Valor Médio", valueRange = 0f..100f)
                        GenesysSlider(value = 100f, onValueChange = {}, label = "Valor Máximo", valueRange = 0f..100f)
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysTabRowAllStates() {
        paparazzi.snapshot(name = "tab_row_all_states") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Tab Row", style = GenesysTextStyle.Title)
                        GenesysTabRow(
                            selectedTabIndex = 0,
                            tabs = listOf(
                                GenesysTabData("Produtos", badgeCount = 5),
                                GenesysTabData("Pedidos")
                            ),
                            onTabSelected = {}
                        )
                        GenesysTabRow(
                            selectedTabIndex = 1,
                            tabs = listOf(
                                GenesysTabData("Todos"),
                                GenesysTabData("Ativos", badgeCount = 3),
                                GenesysTabData("Inativos")
                            ),
                            onTabSelected = {}
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysPagerIndicatorAllPages() {
        paparazzi.snapshot(name = "pager_indicator_all_pages") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Pager Indicator", style = GenesysTextStyle.Title)
                        GenesysPagerIndicator(count = 3, currentPage = 0)
                        GenesysPagerIndicator(count = 4, currentPage = 1)
                        GenesysPagerIndicator(count = 5, currentPage = 2)
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysEmptyStateAllVariations() {
        paparazzi.snapshot(name = "empty_state_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Empty States", style = GenesysTextStyle.Title)
                        GenesysEmptyState(
                            icon = GenesysIcons.Search,
                            title = "Nenhum resultado",
                            description = "Tente uma busca diferente"
                        )
                        GenesysEmptyState(
                            icon = GenesysIcons.ShoppingBag,
                            title = "Carrinho vazio",
                            description = "Adicione produtos",
                            action = { GenesysTextButton(text = "Continuar comprando", onClick = {}) }
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysLoadingIndicatorAllSizes() {
        paparazzi.snapshot(name = "loading_indicator_all_sizes") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Loading Indicators", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            GenesysLoadingIndicator(size = 16.dp)
                            GenesysLoadingIndicator(size = 24.dp)
                            GenesysLoadingIndicator(size = 32.dp)
                            GenesysLoadingIndicator(size = 48.dp)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysTextAllStyles() {
        paparazzi.snapshot(name = "text_all_styles") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysText("Estilos de Texto", style = GenesysTextStyle.Title)
                        GenesysText("Headline", style = GenesysTextStyle.Headline)
                        GenesysText("Title", style = GenesysTextStyle.Title)
                        GenesysText("Body", style = GenesysTextStyle.Body)
                        GenesysText("Label", style = GenesysTextStyle.Label)
                        GenesysText("Error", style = GenesysTextStyle.Error)
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysTextAllWeights() {
        paparazzi.snapshot(name = "text_all_weights") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysText("Pesos de Fonte", style = GenesysTextStyle.Title)
                        GenesysText("Normal", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Normal)
                        GenesysText("Bold", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                        GenesysText("ExtraBold", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysTopAppBarAllVariants() {
        paparazzi.snapshot(name = "top_app_bar_all_variants") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Column {
                    GenesysTopAppBar(
                        title = "Título Simples",
                        onBack = {}
                    )
                    Spacer(Modifier.height(16.dp))
                    GenesysTopAppBar(
                        title = "Com Ações",
                        onBack = {},
                        actions = {
                            GenesysIconButton(icon = GenesysIcons.Search, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.ShoppingBag, onClick = {})
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    GenesysTopAppBar(
                        title = "Admin Dashboard",
                        onBack = {},
                        actions = {
                            GenesysIconButton(icon = GenesysIcons.Add, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.Settings, onClick = {})
                        }
                    )
                }
            }
        }
    }

    @Test
    fun testGenesysSectionHeaderAllVariations() {
        paparazzi.snapshot(name = "section_header_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Section Headers", style = GenesysTextStyle.Title)
                        GenesysSectionHeader(
                            title = "Produtos em Destaque",
                            subtitle = "Ofertas especiais",
                            trailingContent = { GenesysTextButton(text = "Ver Todos", onClick = {}) }
                        )
                        GenesysSectionHeader(
                            title = "Últimos Pedidos",
                            subtitle = null,
                            trailingContent = { GenesysBadge(label = "3", color = MaterialTheme.colorScheme.primary) }
                        )
                        GenesysSectionHeader(
                            title = "Configurações",
                            subtitle = "Ajustes da conta",
                            trailingContent = null
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysStatusPickerAllStatuses() {
        paparazzi.snapshot(name = "status_picker_all_statuses") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Status Picker", style = GenesysTextStyle.Title)
                        GenesysStatusPicker(currentStatus = OrderStatus.PENDING, onStatusSelected = {})
                        GenesysStatusPicker(currentStatus = OrderStatus.PROCESSING, onStatusSelected = {})
                        GenesysStatusPicker(currentStatus = OrderStatus.COMPLETED, onStatusSelected = {})
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysDialogAllVariants() {
        paparazzi.snapshot(name = "dialog_all_variants") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Dialogs", style = GenesysTextStyle.Title)
                        GenesysDialog(
                            onDismissRequest = {},
                            title = "Confirmação",
                            confirmButton = { GenesysTextButton(text = "OK", onClick = {}) },
                            dismissButton = { GenesysTextButton(text = "Cancelar", onClick = {}) }
                        ) {
                            GenesysText("Deseja prosseguir?")
                        }
                        GenesysConfirmDialog(
                            onDismissRequest = {},
                            title = "Excluir",
                            text = "Esta ação não pode ser desfeita",
                            icon = GenesysIcons.Check,
                            confirmButton = { GenesysTextButton(text = "Sim", onClick = {}) },
                            dismissButton = { GenesysTextButton(text = "Não", onClick = {}) }
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysBottomSheetAllVariants() {
        paparazzi.snapshot(name = "bottom_sheet_all_variants") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Bottom Sheets", style = GenesysTextStyle.Title)
                        GenesysBottomSheet(
                            onDismiss = {},
                            title = "Filtros",
                            actions = { GenesysIconButton(icon = GenesysIcons.Close, onClick = {}) }
                        ) {
                            GenesysText("Conteúdo do filtro")
                            GenesysSpacer(GenesysSpacing.Medium)
                            GenesysLoadingButton(text = "Aplicar", onClick = {}, fillWidth = true)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysTrackingTimelineAllStatuses() {
        paparazzi.snapshot(name = "tracking_timeline_all_statuses") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Tracking Timeline", style = GenesysTextStyle.Title)
                        GenesysTrackingTimeline(currentStatus = OrderStatus.PENDING)
                        GenesysTrackingTimeline(currentStatus = OrderStatus.PROCESSING)
                        GenesysTrackingTimeline(currentStatus = OrderStatus.COMPLETED)
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysIconButtonAllStates() {
        paparazzi.snapshot(name = "icon_button_all_states") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Icon Buttons", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysIconButton(icon = GenesysIcons.Add, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.Check, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.Close, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.Edit, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.Search, onClick = {})
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysIconButton(icon = GenesysIcons.List, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.Person, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.ShoppingBag, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.Settings, onClick = {})
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysFabAllVariations() {
        paparazzi.snapshot(name = "fab_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("FABs", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            GenesysFab(icon = GenesysIcons.Add, onClick = {})
                            GenesysFab(icon = GenesysIcons.Edit, onClick = {})
                            GenesysFab(icon = GenesysIcons.ShoppingBag, onClick = {})
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysPhotoPickerAllStates() {
        paparazzi.snapshot(name = "photo_picker_all_states") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Photo Picker", style = GenesysTextStyle.Title)
                        GenesysPhotoPicker(
                            urls = emptyList(),
                            onAddClick = {},
                            onRemoveClick = {},
                            isUploading = false,
                            maxPhotos = 5
                        )
                        GenesysPhotoPicker(
                            urls = listOf(TestImageProvider.mockImageUrl()),
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
    fun testGenesysLayoutHelpers() {
        paparazzi.snapshot(name = "layout_helpers") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Layout Helpers", style = GenesysTextStyle.Title)
                        
                        GenesysText("Box", style = GenesysTextStyle.Body)
                        GenesysBox(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text("Center")
                        }
                        
                        GenesysText("Spacers", style = GenesysTextStyle.Body)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(20.dp).background(MaterialTheme.colorScheme.primary))
                            GenesysSpacer(GenesysSpacing.Small)
                            Box(Modifier.size(20.dp).background(MaterialTheme.colorScheme.secondary))
                            GenesysSpacer(GenesysSpacing.Medium)
                            Box(Modifier.size(20.dp).background(MaterialTheme.colorScheme.tertiary))
                        }
                        
                        GenesysText("Divider", style = GenesysTextStyle.Body)
                        GenesysDivider()
                    }
                }
            }
        }
    }

    @Test
    fun testGenesysLazyLayouts() {
        paparazzi.snapshot(name = "lazy_layouts") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Lazy Column", style = GenesysTextStyle.Title)
                        GenesysLazyColumn(
                            items = listOf("Item 1", "Item 2", "Item 3"),
                            usePadding = false,
                            spacing = GenesysSpacing.Small
                        ) { item ->
                            GenesysCard {
                                GenesysText(item, style = GenesysTextStyle.Body, modifier = Modifier.padding(8.dp))
                            }
                        }
                        
                        GenesysText("Lazy Row", style = GenesysTextStyle.Title)
                        GenesysLazyRow(items = listOf("A", "B", "C")) { item ->
                            GenesysBadge(label = item, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}