package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.atoms.buttons.*
import com.itbenevides.genesys21.ui.components.atoms.calendar.GenesysCalendarDay
import com.itbenevides.genesys21.ui.components.atoms.calendar.GenesysTimeChip
import com.itbenevides.genesys21.ui.components.atoms.images.*
import com.itbenevides.genesys21.ui.components.atoms.indicators.*
import com.itbenevides.genesys21.ui.components.atoms.inputs.*
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.booking.ServiceCard
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.calendar.GenesysDatePicker
import com.itbenevides.genesys21.ui.components.molecules.calendar.GenesysTimePicker
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.molecules.input.*
import com.itbenevides.genesys21.ui.components.molecules.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysPagerIndicator
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.molecules.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.components.organisms.calendar.GenesysBookingEngine
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysConfirmDialog
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
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
    initialTab: Int = 0,
) {
    var currentTheme by remember { mutableStateOf(PageThemeConfig.ROYAL) }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val tabs =
        listOf(
            GenesysTabData("Architecture", GenesysIcons.Dashboard),
            GenesysTabData("Foundation", GenesysIcons.Numbers),
            GenesysTabData("Inputs", GenesysIcons.Edit),
            GenesysTabData("Action & Nav", GenesysIcons.Category),
            GenesysTabData("Display", GenesysIcons.GridView),
            GenesysTabData("Feedback", GenesysIcons.Feedback),
            GenesysTabData("Booking", GenesysIcons.Schedule),
            GenesysTabData("Tools", GenesysIcons.Settings),
        )

    AppTheme(themeConfig = currentTheme) {
        GenesysPage(
            topBar = {
                Column {
                    GenesysTopAppBar(
                        title = "Genesys Design System",
                        onBack = onBack,
                        actions = {
                            Box {
                                GenesysTextButton(
                                    text = "Tema: ${currentTheme.name}",
                                    onClick = { showThemeMenu = true },
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                DropdownMenu(
                                    expanded = showThemeMenu,
                                    onDismissRequest = { showThemeMenu = false },
                                    modifier = Modifier.heightIn(max = 400.dp)
                                ) {
                                    PageThemeConfig.entries.forEach { theme ->
                                        DropdownMenuItem(
                                            text = { Text(theme.name) },
                                            onClick = {
                                                currentTheme = theme
                                                showThemeMenu = false
                                            }
                                        )
                                    }
                                }
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
                    0 -> ArchitectureShowcase()
                    1 -> FoundationShowcase()
                    2 -> InputsShowcase()
                    3 -> ActionNavShowcase()
                    4 -> DisplayShowcase()
                    5 -> FeedbackShowcase()
                    6 -> BookingShowcase()
                    7 -> ToolsShowcase(onOpenEditorShowcase, onOpenTemplateShowcase)
                }

                Spacer(Modifier.height(64.dp))
                GenesysText(
                    text = "End of Catalog • Genesys21 Design System v2.1",
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

@Composable
private fun ArchitectureShowcase() {
    ShowcaseSection("Atomic Design Architecture", "The structural levels of our Design System.") {
        GenesysCard(modifier = Modifier.fillMaxWidth()) {
            GenesysColumn(usePadding = true) {
                ArchitectureLevel(
                    "1. Atoms",
                    "Basic tokens: Typography, Colors, Spacers, Icons.",
                    GenesysIcons.Numbers
                )
                GenesysDivider()
                ArchitectureLevel(
                    "2. Molecules",
                    "Combined atoms: Buttons with icons, Input fields with labels.",
                    GenesysIcons.Category
                )
                GenesysDivider()
                ArchitectureLevel(
                    "3. Organisms",
                    "Complex components: Product lists, Booking engine, Top bars.",
                    GenesysIcons.GridView
                )
                GenesysDivider()
                ArchitectureLevel(
                    "4. Templates",
                    "Page layouts: The GenesysPage shell.",
                    GenesysIcons.Web
                )
            }
        }
    }

    ShowcaseSection("Clean Architecture (KMP)", "Layered logic for cross-platform stability.") {
        GenesysCard(modifier = Modifier.fillMaxWidth()) {
            GenesysColumn(usePadding = true) {
                ArchitectureLevel("UI Layer", "Compose Multiplatform + ViewModels", GenesysIcons.Magic)
                ArchitectureLevel("Domain Layer", "UseCases, Entities, and Repository Interfaces", GenesysIcons.Straighten)
                ArchitectureLevel("Data Layer", "Ktor API implementations, SQLDelight/Settings", GenesysIcons.CloudUpload)
            }
        }
    }

    ShowcaseSection("Tech Stack", "The powerful tools behind Genesys21.") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GenesysBadge(label = "Kotlin Multiplatform", color = Color(0xFF7F52FF))
            GenesysBadge(label = "Compose Multiplatform", color = Color(0xFF4285F4))
            GenesysBadge(label = "Ktor", color = Color(0xFF00BFA5))
            GenesysBadge(label = "Koin DI", color = Color(0xFFEEFF41))
            GenesysBadge(label = "Firebase Auth", color = Color(0xFFFFA000))
            GenesysBadge(label = "Exposed SQL", color = Color(0xFFE91E63))
        }
    }
}

@Composable
private fun ArchitectureLevel(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            GenesysText(text = title, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
            GenesysText(text = description, style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoundationShowcase() {
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

    ShowcaseSection("Icons", "Custom GenesysIcons tokens.") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(GenesysIcons.Magic, null, tint = MaterialTheme.colorScheme.primary)
            Icon(GenesysIcons.ShoppingBag, null)
            Icon(GenesysIcons.Schedule, null)
            Icon(GenesysIcons.Favorite, null, tint = Color.Red)
            Icon(GenesysIcons.AdminPanelSettings, null)
            Icon(GenesysIcons.CloudUpload, null)
        }
    }
}

@Composable
private fun InputsShowcase() {
    ShowcaseSection("Standard Inputs", "Basic text input components.") {
        GenesysTextField(value = "Standard Input", onValueChange = {}, label = "Label", placeholder = "Placeholder")
        Spacer(Modifier.height(16.dp))
        GenesysTextField(value = "Input with Error", onValueChange = {}, label = "Validation Error", icon = GenesysIcons.Edit)
        GenesysText(text = "This field is required", style = GenesysTextStyle.Error)
        Spacer(Modifier.height(16.dp))
        GenesysOutlinedTextField(value = "Outlined Text", onValueChange = {}, label = "Outlined Style")
    }

    ShowcaseSection("Selection & Choice", "Chips, Sliders and Selectors.") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenesysFilterChip(label = "Selected Chip", selected = true, onClick = {}, badgeCount = 5)
            GenesysFilterChip(label = "Standard Chip", selected = false, onClick = {})
        }
        Spacer(Modifier.height(24.dp))
        var sliderVal by remember { mutableStateOf(16f) }
        GenesysSlider(
            value = sliderVal,
            onValueChange = { sliderVal = it },
            label = "Corner Radius: ${sliderVal.toInt()}dp",
            valueRange = 0f..40f,
        )
        Spacer(Modifier.height(24.dp))
        GenesysQuantitySelector(quantity = 2, onIncrease = {}, onDecrease = {})
    }

    ShowcaseSection("Specialized Inputs", "Specific data type selectors.") {
        GenesysColorField(value = "#6200EE", onValueChange = {}, label = "Brand Color Picker")
        Spacer(Modifier.height(16.dp))
        GenesysDropdownField(
            value = "Option 1",
            onValueChange = {},
            label = "Category Dropdown",
            options = listOf("Option 1", "Option 2", "Option 3"),
            icon = GenesysIcons.Category
        )
    }
}

@Composable
private fun ActionNavShowcase() {
    ShowcaseSection("Action Buttons", "Primary, secondary and floating actions.") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GenesysLoadingButton(text = "Submit Action", onClick = {}, icon = GenesysIcons.Check)
            GenesysLoadingButton(text = "Loading State", onClick = {}, isLoading = true)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GenesysLoadingButton(text = "Disabled Button", onClick = {}, enabled = false)
            GenesysTextButton(text = "Text Button", onClick = {})
            GenesysIconButton(icon = GenesysIcons.Favorite, onClick = {}, tint = Color.Red)
            GenesysFab(icon = GenesysIcons.Add, onClick = {})
        }
    }

    ShowcaseSection("Navigation Components", "Bars and indicators.") {
        GenesysTopAppBar(title = "Navigation Bar", onBack = {})
        Spacer(Modifier.height(16.dp))
        GenesysTabRow(
            selectedTabIndex = 0,
            tabs = listOf(
                GenesysTabData("Active Tab", GenesysIcons.Web),
                GenesysTabData("With Badge", GenesysIcons.List, badgeCount = 3),
                GenesysTabData("Simple", GenesysIcons.Person)
            ),
            onTabSelected = {}
        )
        Spacer(Modifier.height(16.dp))
        GenesysPagerIndicator(count = 5, currentPage = 2)
    }
}

@Composable
private fun DisplayShowcase() {
    val sampleProduct = Product("s1", "s1", "Modern Desk Lamp", 129.0, imageUrls = listOf("https://images.unsplash.com/photo-1534073828943-f801091bb18c?q=80&w=800"))

    ShowcaseSection("Cards & Stats", "Information display containers.") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenesysStatsCard(label = "Daily Sales", value = "$1,280", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
            GenesysStatsCard(label = "Active Orders", value = "12", color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        GenesysCard(modifier = Modifier.fillMaxWidth()) {
            GenesysSectionHeader(title = "Featured Information", subtitle = "Use GenesysCard for grouping content.")
            Spacer(Modifier.height(8.dp))
            GenesysText(text = "This is a standard card content showing how padding and elevation work together.", style = GenesysTextStyle.Body)
        }
    }

    ShowcaseSection("Collections", "Grids and Lists of items.") {
        GenesysProductList(
            products = listOf(sampleProduct, sampleProduct.copy(id = "s2", name = "Second Product")),
            isHorizontal = true
        )
    }

    ShowcaseSection("Badges & Visuals", "Status and small markers.") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GenesysBadge(label = "NEW", color = MaterialTheme.colorScheme.primary)
            GenesysStatusBadge(status = OrderStatus.PENDING)
            GenesysStatusBadge(status = OrderStatus.COMPLETED)
            GenesysStockBadge(stock = 3)
            GenesysAvatar(icon = GenesysIcons.Person)
        }
    }
}

@Composable
private fun FeedbackShowcase() {
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    ShowcaseSection("Modals & Dialogs", "Overlays for critical information.") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { showDialog = true }) { Text("Standard Dialog") }
            Button(onClick = { showConfirmDialog = true }) { Text("Confirm Dialog") }
            Button(onClick = { showBottomSheet = true }) { Text("Bottom Sheet") }
        }

        if (showDialog) {
            GenesysDialog(
                onDismissRequest = { showDialog = false },
                title = "Showcase Dialog",
                confirmButton = { Button(onClick = { showDialog = false }) { Text("OK") } }
            ) {
                GenesysText(text = "This is a standard dialog component.", style = GenesysTextStyle.Body)
            }
        }

        if (showConfirmDialog) {
            GenesysConfirmDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = "Are you sure?",
                text = "This action cannot be undone.",
                confirmButton = { Button(onClick = { showConfirmDialog = false }) { Text("Yes, Delete") } },
                dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") } }
            )
        }

        if (showBottomSheet) {
            GenesysBottomSheet(
                onDismiss = { showBottomSheet = false },
                title = "Bottom Sheet Title"
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GenesysText(text = "Bottom Sheet Content", style = GenesysTextStyle.Title)
                    Spacer(Modifier.height(16.dp))
                    GenesysText(text = "This component is great for mobile-first actions.", style = GenesysTextStyle.Body)
                    Spacer(Modifier.height(24.dp))
                    GenesysLoadingButton(text = "Close", onClick = { showBottomSheet = false }, fillWidth = true)
                }
            }
        }
    }

    ShowcaseSection("States & Timelines", "Feedback on progress and empty states.") {
        GenesysTrackingTimeline(currentStatus = OrderStatus.PROCESSING)
        Spacer(Modifier.height(24.dp))
        GenesysEmptyState(
            icon = GenesysIcons.SearchOff,
            title = "Nothing to show",
            description = "This is how we handle empty states across the app.",
            action = { GenesysLoadingButton(text = "Retry", onClick = {}) }
        )
    }
}

@Composable
private fun BookingShowcase() {
    val sampleService = BookingService("s1", "s1", "Haircut", "Classic man haircut.", 45.0, 40)

    ShowcaseSection("Booking Atoms", "Day tiles and time slots.") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenesysCalendarDay(day = 10, isSelected = true, isToday = false, isEnabled = true, onClick = {})
            GenesysCalendarDay(day = 11, isSelected = false, isToday = true, isEnabled = true, onClick = {})
            GenesysTimeChip(time = "14:00", isSelected = true, onClick = {})
        }
    }

    ShowcaseSection("Booking Organisms", "The complete scheduling experience.") {
        GenesysBookingEngine(
            selectedDateTime = null,
            availableSlots = listOf("09:00", "11:00", "15:00"),
            onDateSelected = {},
            onDateTimeSelected = {}
        )
        Spacer(Modifier.height(24.dp))
        ServiceCard(service = sampleService, onClick = {})
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
