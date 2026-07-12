package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.ui.components.atoms.buttons.*
import com.itbenevides.genesys21.ui.components.atoms.images.*
import com.itbenevides.genesys21.ui.components.atoms.indicators.*
import com.itbenevides.genesys21.ui.components.atoms.inputs.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.molecules.input.*
import com.itbenevides.genesys21.ui.components.molecules.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.organisms.product.GenesysProductList
import com.itbenevides.genesys21.ui.components.organisms.status.GenesysTrackingTimeline
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.util.GenesysBrandPresets
import com.itbenevides.genesys21.util.toColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignSystemShowcaseScreen(
    onBack: () -> Unit,
    onOpenEditorShowcase: () -> Unit,
    onOpenTemplateShowcase: () -> Unit,
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs =
        listOf(
            GenesysTabData("Atoms", GenesysIcons.Numbers),
            GenesysTabData("Molecules", GenesysIcons.Category),
            GenesysTabData("Organisms", GenesysIcons.GridView),
            GenesysTabData("Tools", GenesysIcons.Settings),
        )

    val sampleProduct =
        remember {
            Product(
                id = "showcase_1",
                name = "SmartWatch Genesys Pro",
                price = 899.90,
                imageUrls = listOf("https://images.unsplash.com/photo-1544117518-30dd5f2f309e?q=80&w=800"),
                description = "High-performance smartwatch with health tracking and elegant design.",
                stock = 15,
            )
        }

    AppTheme(themeConfig = if (isDarkMode) com.itbenevides.genesys21.domain.model.PageThemeConfig.DARK_MODE else com.itbenevides.genesys21.domain.model.PageThemeConfig.ROYAL) {
        GenesysPage(
            topBar = {
                Column {
                    GenesysTopAppBar(
                        title = "Genesys Design System",
                        onBack = onBack,
                        actions = {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                Text("Dark Mode", style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.width(8.dp))
                                Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it })
                            }
                        },
                    )
                    GenesysTabRow(
                        selectedTabIndex = selectedTab,
                        tabs = tabs,
                        onTabSelected = { selectedTab = it },
                    )
                }
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
            ) {
                when (selectedTab) {
                    0 -> AtomsShowcase(sampleProduct)
                    1 -> MoleculesShowcase(sampleProduct)
                    2 -> OrganismsShowcase(sampleProduct)
                    3 -> ToolsShowcase(onOpenEditorShowcase, onOpenTemplateShowcase)
                }

                Spacer(Modifier.height(64.dp))
                GenesysText(
                    text = "End of Catalog • Genesys21 Design System v2.0",
                    style = GenesysTextStyle.Label,
                    textAlign = GenesysTextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(Modifier.height(64.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AtomsShowcase(sampleProduct: Product) {
    ShowcaseSection("Typography", "Semantic text styles across the system.") {
        GenesysText(text = "Headline Large (34sp)", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(text = "Title Medium (22sp)", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysText(text = "Body Large (17sp)", style = GenesysTextStyle.Body)
        GenesysText(text = "Label Small (11sp)", style = GenesysTextStyle.Label)
        GenesysText(text = "Error Message Style", style = GenesysTextStyle.Error)
    }

    ShowcaseSection("Colors & Brand", "Brand presets and color atoms.") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GenesysBrandPresets.forEach { hex ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GenesysColorCircle(color = hex.toColor(), size = 48.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(hex, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    ShowcaseSection("Images & Avatars", "Profile and content image components.") {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            GenesysAvatar(icon = GenesysIcons.Person)
            GenesysImage(
                url = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=800",
                size = 120.dp,
            )
            Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        }
    }

    ShowcaseSection("Indicators", "Badges and loading states.") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GenesysBadge(label = "PREMIUM", color = MaterialTheme.colorScheme.primary)
            GenesysBadge(label = "50% OFF", color = MaterialTheme.colorScheme.error, showDot = false)
            GenesysStatusBadge(status = com.itbenevides.genesys21.domain.model.OrderStatus.PENDING)
            GenesysStatusBadge(status = com.itbenevides.genesys21.domain.model.OrderStatus.COMPLETED)
            GenesysStockBadge(stock = 5)
            GenesysLoadingIndicator(size = 32.dp)
        }
    }

    ShowcaseSection("Inputs", "Sliders, chips and text fields.") {
        GenesysTextField(value = "Input Text", onValueChange = {}, label = "Standard Field", icon = GenesysIcons.Edit)
        Spacer(Modifier.height(16.dp))
        GenesysOutlinedTextField(value = "Outlined Text", onValueChange = {}, label = "Outlined Style")
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenesysFilterChip(label = "Gadgets", selected = true, onClick = {}, badgeCount = 12)
            GenesysFilterChip(label = "Fashion", selected = false, onClick = {})
        }
        Spacer(Modifier.height(16.dp))
        var sliderVal by remember { mutableStateOf(20f) }
        GenesysSlider(value = sliderVal, onValueChange = { sliderVal = it }, label = "Corner Radius: ${sliderVal.toInt()}dp", valueRange = 0f..40f)
    }
}

@Composable
private fun MoleculesShowcase(sampleProduct: Product) {
    ShowcaseSection("Buttons", "High-level interactive buttons.") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GenesysLoadingButton(text = "Submit Order", onClick = {}, icon = GenesysIcons.Check)
            GenesysLoadingButton(text = "Processing...", onClick = {}, isLoading = true)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GenesysTextButton(text = "Cancel Action", onClick = {}, color = MaterialTheme.colorScheme.error)
            GenesysIconButton(icon = GenesysIcons.Favorite, onClick = {}, tint = Color.Red)
            GenesysFab(icon = GenesysIcons.Add, onClick = {})
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenesysStatsCard(label = "Monthly Revenue", value = "$45,200", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
            GenesysStatsCard(label = "New Customers", value = "+128", color = Color(0xFF2CB1FF), modifier = Modifier.weight(1f))
        }
    }

    ShowcaseSection("Navigation & Layout", "Interactive controls and structure.") {
        var search by remember { mutableStateOf("") }
        GenesysSearchBar(value = search, onValueChange = { search = it }, placeholder = "Search the showcase...")
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            GenesysQuantitySelector(quantity = 1, onIncrease = {}, onDecrease = {})
            Spacer(Modifier.width(24.dp))
            GenesysPagerIndicator(count = 5, currentPage = 2)
        }
        Spacer(Modifier.height(16.dp))
        GenesysCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            GenesysSectionHeader(title = "Store Settings", subtitle = "Manage your global preferences here.")
            Spacer(Modifier.height(16.dp))
            GenesysText(text = "Use the Genesys Card to group related information and actions.", style = GenesysTextStyle.Body)
        }
    }
}

@Composable
private fun OrganismsShowcase(sampleProduct: Product) {
    ShowcaseSection("Product Collections", "Complex components managing multiple product molecules.") {
        GenesysSectionHeader(title = "Grid Layout", subtitle = "Responsive grid of product cards.")
        GenesysProductList(
            products =
                listOf(
                    sampleProduct,
                    sampleProduct.copy(id = "s2", name = "Product B", price = 150.0),
                    sampleProduct.copy(id = "s3", name = "Product C", price = 250.0),
                    sampleProduct.copy(id = "s4", name = "Product D", price = 350.0),
                ),
            isHorizontal = false,
        )
        Spacer(Modifier.height(16.dp))
        GenesysSectionHeader(title = "Horizontal Layout", subtitle = "Scrollable carousel for highlights.")
        GenesysProductList(
            products = (1..5).map { sampleProduct.copy(id = "h$it", name = "Highlight $it") },
            isHorizontal = true,
        )
    }

    ShowcaseSection("Business Flows & Feedback", "Complex business logic components.") {
        GenesysTrackingTimeline(currentStatus = com.itbenevides.genesys21.domain.model.OrderStatus.PROCESSING)
        Spacer(Modifier.height(32.dp))
        GenesysEmptyState(
            icon = GenesysIcons.SearchOff,
            title = "No results found",
            description = "Try adjusting your search filters to find what you are looking for.",
            action = {
                GenesysLoadingButton(text = "Clear Filters", onClick = {})
            },
        )
    }
}

@Composable
private fun ToolsShowcase(
    onOpenEditorShowcase: () -> Unit,
    onOpenTemplateShowcase: () -> Unit,
) {
    ShowcaseSection("Developer & Merchant Tools", "Screens for documentation and template management.") {
        GenesysCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenTemplateShowcase) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(GenesysIcons.GridView, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Template Catalog", style = MaterialTheme.typography.titleMedium)
                    Text("Browse our pre-built store templates.", style = MaterialTheme.typography.bodySmall)
                }
                Icon(GenesysIcons.ArrowRight, null)
            }
        }
        Spacer(Modifier.height(16.dp))
        GenesysCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenEditorShowcase) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(GenesysIcons.Edit, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Editor UI Comparison", style = MaterialTheme.typography.titleMedium)
                    Text("Side-by-side view of Editor vs Final result.", style = MaterialTheme.typography.bodySmall)
                }
                Icon(GenesysIcons.ArrowRight, null)
            }
        }
    }
}

@Composable
private fun ShowcaseSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        content()
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}
