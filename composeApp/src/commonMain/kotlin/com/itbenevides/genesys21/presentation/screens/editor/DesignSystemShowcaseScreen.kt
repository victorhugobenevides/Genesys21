package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
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
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.util.GenesysBrandPresets
import com.itbenevides.genesys21.util.toColor
import com.itbenevides.genesys21.getWebBaseUrl

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
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    val tabs =
        listOf(
            GenesysTabData("Architecture", GenesysIcons.Dashboard),
            GenesysTabData("Foundation", GenesysIcons.Numbers),
            GenesysTabData("Templates", GenesysIcons.GridView),
            GenesysTabData("Inputs", GenesysIcons.Edit),
            GenesysTabData("Action & Nav", GenesysIcons.Category),
            GenesysTabData("Display", GenesysIcons.GridView),
            GenesysTabData("Feedback", GenesysIcons.Feedback),
            GenesysTabData("Booking", GenesysIcons.Schedule),
            GenesysTabData("Quality", GenesysIcons.Check),
            GenesysTabData("Tools", GenesysIcons.Settings),
        )

    AppTheme(themeConfig = currentTheme) {
        GenesysPage(
            topBar = {
                Column {
                    GenesysTopAppBar(
                        title = if (isCompact) "Design System" else "Genesys Design System",
                        onBack = onBack,
                        actions = {
                            Box {
                                if (isCompact) {
                                    GenesysIconButton(icon = GenesysIcons.Palette, onClick = { showThemeMenu = true })
                                } else {
                                    GenesysTextButton(
                                        text = "Tema: ${currentTheme.name}",
                                        onClick = { showThemeMenu = true },
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
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
                        .padding(horizontal = if (isCompact) 16.dp else 24.dp),
            ) {
                when (selectedTab) {
                    0 -> ArchitectureShowcase()
                    1 -> FoundationShowcase()
                    2 -> TemplatesTabShowcase()
                    3 -> InputsShowcase()
                    4 -> ActionNavShowcase()
                    5 -> DisplayShowcase()
                    6 -> FeedbackShowcase()
                    7 -> BookingShowcase()
                    8 -> QualityShowcase()
                    9 -> ToolsShowcase(onOpenEditorShowcase, onOpenTemplateShowcase)
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
private fun TemplatesTabShowcase() {
    val templates = PageTemplateRegistry.templates
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    ShowcaseSection("Ready-to-Use Templates", "Pre-configured layouts for different business models.") {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            templates.forEach { template ->
                GenesysCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val thumb = template.thumbnailUrl
                        if (thumb != null && !isCompact) {
                            GenesysImage(
                                url = thumb,
                                size = 80.dp,
                                modifier = Modifier.clip(MaterialTheme.shapes.medium)
                            )
                            Spacer(Modifier.width(16.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            GenesysText(text = template.title, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                            GenesysText(text = template.description, style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(template.category.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
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
                GenesysDivider()
                ArchitectureLevel(
                    "5. Responsive Adaptation",
                    "Adaptive layouts for Mobile, Tablet and Desktop using WindowSizeClass.",
                    GenesysIcons.Dashboard
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

    ShowcaseSection("Quality & Visual Regression", "Automation for structural and visual integrity.") {
        GenesysCard(modifier = Modifier.fillMaxWidth()) {
            GenesysColumn(usePadding = true) {
                ArchitectureLevel(
                    "Visual Regression",
                    "Paparazzi snapshots for Android, Tablet and Desktop views.",
                    GenesysIcons.Check
                )
                ArchitectureLevel(
                    "Unit Testing",
                    "Cross-platform logic validation in Common/JVM/WasmJS.",
                    GenesysIcons.Straighten
                )
            }
        }
    }
}

@Composable
private fun QualityShowcase() {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    ShowcaseSection("Test Reports", "Access detailed execution reports for each module.") {
        val baseUrl = getWebBaseUrl()
        val reports = listOf(
            "Visual Regression (Paparazzi)" to "$baseUrl/reports/paparazzi/index.html",
            "App Logic Coverage" to "$baseUrl/reports/coverage/app/index.html",
            "Shared Logic Coverage" to "$baseUrl/reports/coverage/shared/index.html",
            "Server Logic Coverage" to "$baseUrl/reports/coverage/server/index.html",
            "Shared Logic Unit Tests" to "$baseUrl/reports/shared/index.html",
            "App Logic Unit Tests" to "$baseUrl/reports/app/index.html",
            "Backend (Server) Tests" to "$baseUrl/reports/server/index.html"
        )

        if (isCompact) {
            reports.forEach { (name, path) ->
                GenesysCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // Forçando abertura em nova aba no browser
                        uriHandler.openUri(path)
                    }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(GenesysIcons.Description, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Icon(GenesysIcons.ArrowRight, null)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        } else {
            reports.chunked(2).forEach { rowReports ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowReports.forEach { (name, path) ->
                        GenesysCard(
                            modifier = Modifier.weight(1f),
                            onClick = { uriHandler.openUri(path) }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(GenesysIcons.Description, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Icon(GenesysIcons.ArrowRight, null)
                            }
                        }
                    }
                    if (rowReports.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    ShowcaseSection("Snapshot Coverage", "Visual status of key components across resolutions.") {
        val coverage = listOf(
            "Phone (393dp)" to "100%",
            "Tablet (600dp)" to "100%",
            "Desktop (1200dp)" to "100%",
            "Dark Mode" to "85%",
            "Component Editors" to "100%"
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            coverage.take(if (isCompact) 2 else 5).forEach { (label, value) ->
                GenesysStatsCard(
                    label = label,
                    value = value,
                    color = if (value == "100%") Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        GenesysCard(modifier = Modifier.fillMaxWidth()) {
            GenesysColumn(usePadding = true) {
                ArchitectureLevel("Adaptive Layouts", "Validated Phone, Tablet and Desktop views.", GenesysIcons.Numbers)
                ArchitectureLevel("Component Editors", "100% coverage for block editors (Header, Text, Bio, etc).", GenesysIcons.Edit)
                ArchitectureLevel("Core Screens", "Snapshots for Product, Service and Page editors.", GenesysIcons.Web)
            }
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
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    ShowcaseSection("Typography", "Semantic text styles across the system.") {
        GenesysText(text = "Headline Large (34sp)", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(text = "Title Medium (22sp)", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysText(text = "Body Large (17sp)", style = GenesysTextStyle.Body)
        GenesysText(text = "Label Small (11sp)", style = GenesysTextStyle.Label)
        GenesysText(text = "Error Message Style", style = GenesysTextStyle.Error)
    }

    ShowcaseSection("Colors & Brand", "Brand presets and color atoms.") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenesysBrandPresets.forEach { hex ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GenesysColorCircle(color = hex.toColor(), size = if (isCompact) 40.dp else 48.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(hex, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    ShowcaseSection("Icons", "Custom GenesysIcons tokens.") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf(
                GenesysIcons.Magic, GenesysIcons.ShoppingBag, GenesysIcons.Schedule,
                GenesysIcons.Favorite, GenesysIcons.AdminPanelSettings, GenesysIcons.CloudUpload
            ).forEach { icon ->
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isCompact) 20.dp else 24.dp))
            }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DisplayShowcase() {
    val sampleProduct = Product("s1", "store-1", "Modern Desk Lamp", 129.0, imageUrls = listOf("https://images.unsplash.com/photo-1534073828943-f801091bb18c?q=80&w=800"))
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    ShowcaseSection("Cards & Stats", "Information display containers.") {
        if (isCompact) {
            GenesysStatsCard(label = "Daily Sales", value = "$1,280", color = Color(0xFF4CAF50), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            GenesysStatsCard(label = "Active Orders", value = "12", color = Color(0xFFFF9800), modifier = Modifier.fillMaxWidth())
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GenesysStatsCard(label = "Daily Sales", value = "$1,280", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                GenesysStatsCard(label = "Active Orders", value = "12", color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
            }
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
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.align(Alignment.CenterVertically)) { GenesysBadge(label = "NEW", color = MaterialTheme.colorScheme.primary) }
            Box(Modifier.align(Alignment.CenterVertically)) { GenesysStatusBadge(status = OrderStatus.PENDING) }
            Box(Modifier.align(Alignment.CenterVertically)) { GenesysStatusBadge(status = OrderStatus.COMPLETED) }
            Box(Modifier.align(Alignment.CenterVertically)) { GenesysStockBadge(stock = 3) }
            Box(Modifier.align(Alignment.CenterVertically)) { GenesysAvatar(icon = GenesysIcons.Person) }
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
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    Column(modifier = Modifier.padding(vertical = if (isCompact) 16.dp else 24.dp)) {
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
        Spacer(Modifier.height(if (isCompact) 16.dp else 24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}
