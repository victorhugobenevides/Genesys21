package com.itbenevides.genesys21.screenshot.components

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.*
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.screenshot.base.TestImageProvider
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes de screenshot para variações avançadas de componentes.
 * Cobrindo combinações complexas e casos de uso específicos.
 */
class AdvancedComponentVariationsTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testButtonCombinations() {
        paparazzi.snapshot(name = "advanced_button_combinations") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Combinações de Botões", style = GenesysTextStyle.Title)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysLoadingButton(text = "Sim", onClick = {})
                            GenesysLoadingButton(text = "Não", onClick = {}, containerColor = MaterialTheme.colorScheme.secondary)
                            GenesysLoadingButton(text = "Cancelar", onClick = {}, containerColor = MaterialTheme.colorScheme.tertiary)
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysLoadingButton(text = "Salvar", onClick = {}, icon = GenesysIcons.Check, iconContentDescription = "Confirmar")
                            GenesysLoadingButton(text = "Excluir", onClick = {}, containerColor = MaterialTheme.colorScheme.error)
                        }
                        
                        GenesysLoadingButton(text = "Continuar", onClick = {}, fillWidth = true)
                        GenesysLoadingButton(text = "Desabilitado", onClick = {}, enabled = false, fillWidth = true)
                        GenesysLoadingButton(text = "Carregando...", onClick = {}, isLoading = true, fillWidth = true)
                    }
                }
            }
        }
    }

    @Test
    fun testFormLayouts() {
        paparazzi.snapshot(name = "advanced_form_layouts") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysSectionHeader(
                            title = "Cadastro de Usuário",
                            subtitle = "Preencha todos os campos"
                        )
                        
                        GenesysTextField(
                            value = "João Silva",
                            onValueChange = {},
                            label = "Nome Completo",
                            placeholder = "Digite seu nome"
                        )
                        
                        GenesysTextField(
                            value = "joao@email.com",
                            onValueChange = {},
                            label = "Email",
                            placeholder = "exemplo@email.com"
                        )
                        
                        GenesysTextField(
                            value = "",
                            onValueChange = {},
                            label = "Senha",
                            placeholder = "Mínimo 8 caracteres",
                            isError = true,
                            supportingText = "Senha muito fraca"
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysLoadingButton(text = "Cancelar", onClick = {}, containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            GenesysLoadingButton(text = "Cadastrar", onClick = {}, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testDashboardWidgets() {
        paparazzi.snapshot(name = "advanced_dashboard_widgets") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Dashboard Widgets", style = GenesysTextStyle.Title)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysCard(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    GenesysText("R$ 12.450", style = GenesysTextStyle.Headline)
                                    GenesysText("Total Vendas", style = GenesysTextStyle.Label)
                                }
                            }
                            GenesysCard(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    GenesysText("328", style = GenesysTextStyle.Headline)
                                    GenesysText("Pedidos", style = GenesysTextStyle.Label)
                                }
                            }
                            GenesysCard(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    GenesysText("89%", style = GenesysTextStyle.Headline)
                                    GenesysText("Satisfação", style = GenesysTextStyle.Label)
                                }
                            }
                        }
                        
                        GenesysCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                GenesysText("Gráfico de Vendas", style = GenesysTextStyle.Title)
                                ShimmerRectPlaceholder(height = 120.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testProductCards() {
        paparazzi.snapshot(name = "advanced_product_cards") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Cards de Produto", style = GenesysTextStyle.Title)
                        
                        GenesysCard {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GenesysImage(url = TestImageProvider.mockImageUrl(), size = 80.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    GenesysText("Fone Bluetooth Pro", style = GenesysTextStyle.Title)
                                    GenesysText("Áudio premium", style = GenesysTextStyle.Body)
                                    GenesysText("R$ 299,90", style = GenesysTextStyle.Headline, color = MaterialTheme.colorScheme.primary)
                                }
                                GenesysLoadingButton(text = "Add", onClick = {})
                            }
                        }
                        
                        GenesysCard {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GenesysImage(url = "", size = 80.dp, showShimmer = true)
                                Column(modifier = Modifier.weight(1f)) {
                                    GenesysText("Smartwatch X", style = GenesysTextStyle.Title)
                                    ShimmerTextPlaceholder(width = 0.6f)
                                    ShimmerTextPlaceholder(width = 0.4f)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testNotificationCards() {
        paparazzi.snapshot(name = "advanced_notification_cards") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysText("Notificações", style = GenesysTextStyle.Title)
                        
                        GenesysCard(backgroundColor = MaterialTheme.colorScheme.primaryContainer) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GenesysIcons.Check
                                Column(modifier = Modifier.weight(1f)) {
                                    GenesysText("Pedido Confirmado", style = GenesysTextStyle.Title, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    GenesysText("Seu pedido #1234 foi aprovado", style = GenesysTextStyle.Body, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                        
                        GenesysCard(backgroundColor = MaterialTheme.colorScheme.errorContainer) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GenesysIcons.Close
                                Column(modifier = Modifier.weight(1f)) {
                                    GenesysText("Pagamento Recusado", style = GenesysTextStyle.Title, color = MaterialTheme.colorScheme.onErrorContainer)
                                    GenesysText("Tente outro método de pagamento", style = GenesysTextStyle.Body, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                        
                        GenesysCard(backgroundColor = MaterialTheme.colorScheme.tertiaryContainer) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GenesysIcons.Person
                                Column(modifier = Modifier.weight(1f)) {
                                    GenesysText("Novo Cliente", style = GenesysTextStyle.Title, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    GenesysText("Maria se cadastrou", style = GenesysTextStyle.Body, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testEmptyAndErrorStates() {
        paparazzi.snapshot(name = "advanced_empty_error_states") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Estados Vazios e Erro", style = GenesysTextStyle.Title)
                        
                        GenesysEmptyState(
                            icon = GenesysIcons.Search,
                            title = "Nenhum produto",
                            description = "Tente uma busca diferente",
                            action = { GenesysLoadingButton(text = "Ver Todos", onClick = {}) }
                        )
                        
                        GenesysCard(backgroundColor = MaterialTheme.colorScheme.errorContainer) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                GenesysText("⚠️", style = GenesysTextStyle.Headline)
                                GenesysText("Erro de Conexão", style = GenesysTextStyle.Title, color = MaterialTheme.colorScheme.onErrorContainer)
                                GenesysText("Verifique sua internet", style = GenesysTextStyle.Body, color = MaterialTheme.colorScheme.onErrorContainer)
                                com.itbenevides.genesys21.ui.components.layout.GenesysSpacer(com.itbenevides.genesys21.ui.components.layout.GenesysSpacing.Small)
                                GenesysLoadingButton(
                                    text = "Tentar Novamente",
                                    onClick = {},
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testMenuAndActions() {
        paparazzi.snapshot(name = "advanced_menu_actions") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysText("Menus e Ações", style = GenesysTextStyle.Title)
                        
                        repeat(5) { index ->
                            GenesysCard(onClick = {}) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        when (index) {
                                            0 -> GenesysIcons.Person
                                            1 -> GenesysIcons.Settings
                                            2 -> GenesysIcons.List
                                            3 -> GenesysIcons.ShoppingBag
                                            else -> GenesysIcons.Search
                                        }
                                        GenesysText("Opção ${index + 1}", style = GenesysTextStyle.Body)
                                    }
                                    GenesysText(">", style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testAllThemesComparison() {
        val themes = listOf(
            PageThemeConfig.ROYAL to "Primário",
            PageThemeConfig.OCEAN to "Oceano",
            PageThemeConfig.FOREST to "Floresta",
            PageThemeConfig.SUNSET to "Pôr do Sol",
            PageThemeConfig.BERRY to "Frutas",
            PageThemeConfig.DARK_MODE to "Escuro"
        )
        
        themes.forEach { (theme, label) ->
            paparazzi.snapshot(name = "theme_comparison_${theme.name.lowercase()}") {
                AppTheme(themeConfig = theme) {
                    Surface(modifier = Modifier.padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            GenesysText(label, style = GenesysTextStyle.Title)
                            GenesysLoadingButton(text = "Ação", onClick = {})
                            GenesysCard {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    GenesysText("Card", style = GenesysTextStyle.Body)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}