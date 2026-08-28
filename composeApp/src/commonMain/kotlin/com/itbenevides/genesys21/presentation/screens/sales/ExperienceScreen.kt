package com.itbenevides.genesys21.presentation.screens.sales

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.ui.util.ProvideWindowSizeClass
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreen
import org.koin.compose.koinInject

@Composable
fun ExperienceScreen(
    onStartNow: () -> Unit
) {
    var currentTheme by remember { mutableStateOf(PageThemeConfig.ELEGANCE) }
    var deviceType by remember { mutableStateOf("phone") } // phone, tablet, desktop

    val samplePage = remember(currentTheme) {
        Page.createFromTemplate("premium_store", "demo", "demo", "Minha Loja Interativa").copy(
            theme = currentTheme
        )
    }

    AppTheme(themeConfig = currentTheme) {
        GenesysPage(usePadding = false) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. HERO SECTION
                HeroSection()

                GenesysSpacer(GenesysTheme.spacing.huge)

                // 2. INTERACTIVE CONTROLS
                ControlPanel(
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it },
                    currentDevice = deviceType,
                    onDeviceChange = { deviceType = it }
                )

                GenesysSpacer(GenesysTheme.spacing.l)

                // 3. DEVICE SANDBOX (THE PREVIEW)
                DeviceSandbox(
                    deviceType = deviceType,
                    page = samplePage
                )

                GenesysSpacer(GenesysTheme.spacing.xl)

                // 4. STRIPE DEMO SIMULATOR
                StripeDemoSection()

                GenesysSpacer(GenesysTheme.spacing.huge)

                // 5. CALL TO ACTION
                CTASection(onStartNow)

                GenesysSpacer(GenesysTheme.spacing.huge)
            }
        }
    }
}

@Composable
private fun HeroSection() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { it / 2 })
    ) {
        GenesysColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = true
        ) {
            GenesysText(
                text = "A Revolução White-Label",
                style = GenesysTextStyle.Headline,
                fontWeight = GenesysFontWeight.ExtraBold,
                color = GenesysTheme.colors.brand,
                textAlign = GenesysTextAlign.Center
            )
            GenesysSpacer(GenesysTheme.spacing.s)
            GenesysText(
                text = "Experimente o poder do Genesys21. Customize, visualize e venda em minutos.",
                style = GenesysTextStyle.Title,
                textAlign = GenesysTextAlign.Center,
                color = GenesysTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ControlPanel(
    currentTheme: PageThemeConfig,
    onThemeChange: (PageThemeConfig) -> Unit,
    currentDevice: String,
    onDeviceChange: (String) -> Unit
) {
    GenesysCard(
        modifier = Modifier.widthIn(max = 800.dp).padding(horizontal = 16.dp),
        elevation = 8.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            GenesysText(text = "Magic Customizer", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Theme Selection
                Column(Modifier.weight(1f)) {
                    GenesysText(text = "Escolha um Tema", style = GenesysTextStyle.Label)
                    GenesysSpacer(GenesysTheme.spacing.xs)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeBubble(PageThemeConfig.ELEGANCE, currentTheme == PageThemeConfig.ELEGANCE) { onThemeChange(it) }
                        ThemeBubble(PageThemeConfig.VIBRANT, currentTheme == PageThemeConfig.VIBRANT) { onThemeChange(it) }
                        ThemeBubble(PageThemeConfig.MONO, currentTheme == PageThemeConfig.MONO) { onThemeChange(it) }
                        ThemeBubble(PageThemeConfig.MIDNIGHT, currentTheme == PageThemeConfig.MIDNIGHT) { onThemeChange(it) }
                    }
                }

                // Device Selection
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    GenesysText(text = "Visualizar em", style = GenesysTextStyle.Label)
                    GenesysSpacer(GenesysTheme.spacing.xs)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DeviceIcon(GenesysIcons.ShoppingBag, "phone", currentDevice == "phone") { onDeviceChange(it) }
                        DeviceIcon(GenesysIcons.GridView, "tablet", currentDevice == "tablet") { onDeviceChange(it) }
                        DeviceIcon(GenesysIcons.Web, "desktop", currentDevice == "desktop") { onDeviceChange(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeBubble(theme: PageThemeConfig, isSelected: Boolean, onClick: (PageThemeConfig) -> Unit) {
    val color = when(theme) {
        PageThemeConfig.ELEGANCE -> Color(0xFF1A1C1E)
        PageThemeConfig.VIBRANT -> Color(0xFF0061A4)
        PageThemeConfig.NATURE -> Color(0xFF006D39)
        PageThemeConfig.MONO -> Color(0xFF000000)
        PageThemeConfig.MIDNIGHT -> Color(0xFF191C1E)
        PageThemeConfig.CANDY -> Color(0xFF9042A4)
        PageThemeConfig.DEFAULT -> Color(0xFF1A1C1E)
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) GenesysTheme.colors.brand else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick(theme) }
    )
}

@Composable
private fun DeviceIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, type: String, isSelected: Boolean, onClick: (String) -> Unit) {
    IconButton(
        onClick = { onClick(type) },
        modifier = Modifier.background(
            if (isSelected) GenesysTheme.colors.brandContainer else Color.Transparent,
            RoundedCornerShape(8.dp)
        )
    ) {
        Icon(icon, null, tint = if(isSelected) GenesysTheme.colors.brand else GenesysTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun DeviceSandbox(deviceType: String, page: Page) {
    val width = when(deviceType) {
        "phone" -> 360.dp
        "tablet" -> 600.dp
        else -> 1000.dp
    }

    val height = when(deviceType) {
        "phone" -> 640.dp
        "tablet" -> 800.dp
        else -> 600.dp
    }

    val animatedWidth by animateDpAsState(targetValue = width, animationSpec = spring(stiffness = Spring.StiffnessLow))
    val animatedHeight by animateDpAsState(targetValue = height, animationSpec = spring(stiffness = Spring.StiffnessLow))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GenesysText(text = "Preview Real-Time", style = GenesysTextStyle.Label, color = GenesysTheme.colors.outline)
        GenesysSpacer(GenesysTheme.spacing.s)

        Box(
            modifier = Modifier
                .size(width = animatedWidth + 24.dp, height = animatedHeight + 24.dp)
                .background(Color.DarkGray, RoundedCornerShape(24.dp))
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            ProvideWindowSizeClass(width) {
                // Injetamos um roteador fake para o preview
                PageViewerScreen(
                    page = page,
                    router = koinInject(),
                    onOpenDashboard = {}
                )
            }
        }
    }
}

@Composable
private fun StripeDemoSection() {
    var paymentStatus by remember { mutableStateOf("idle") } // idle, processing, success

    GenesysColumn(
        modifier = Modifier.widthIn(max = 600.dp).padding(horizontal = 16.dp),
        horizontalAlignment = GenesysAlignment.Center,
        usePadding = true
    ) {
        GenesysText(text = "Checkout Transparente", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysText(text = "Seus clientes compram sem sair da sua vitrine.", style = GenesysTextStyle.Label, color = GenesysTheme.colors.outline)

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (paymentStatus == "idle") {
                    Icon(GenesysIcons.Payments, null, tint = GenesysTheme.colors.brand, modifier = Modifier.size(48.dp))
                    GenesysSpacer(GenesysTheme.spacing.m)
                    GenesysText(text = "Resumo da Compra", fontWeight = GenesysFontWeight.Bold)
                    GenesysText(text = "1x Produto Premium - R$ 99,90", style = GenesysTextStyle.Label)

                    GenesysSpacer(GenesysTheme.spacing.l)

                    GenesysLoadingButton(
                        text = "Simular Pagamento Seguro",
                        onClick = { paymentStatus = "processing" },
                        fillWidth = true
                    )
                } else if (paymentStatus == "processing") {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        paymentStatus = "success"
                    }
                    CircularProgressIndicator(color = GenesysTheme.colors.brand)
                    GenesysSpacer(GenesysTheme.spacing.m)
                    GenesysText(text = "Processando via Stripe...")
                } else {
                    Icon(com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons.Check, null, tint = Color(0xFF34C759), modifier = Modifier.size(64.dp))
                    GenesysSpacer(GenesysTheme.spacing.m)
                    GenesysText(text = "Pagamento Confirmado!", fontWeight = GenesysFontWeight.ExtraBold)
                    GenesysText(text = "O lojista já recebeu o pedido.", style = GenesysTextStyle.Label)

                    GenesysSpacer(GenesysTheme.spacing.m)
                    GenesysTextButton(text = "Reiniciar Demo", onClick = { paymentStatus = "idle" })
                }
            }
        }
    }
}

@Composable
private fun CTASection(onStartNow: () -> Unit) {
    GenesysColumn(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = GenesysAlignment.Center
    ) {
        GenesysText(text = "Pronto para decolar?", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysSpacer(GenesysTheme.spacing.m)
        GenesysLoadingButton(
            text = "Criar Minha Loja Grátis",
            onClick = onStartNow,
            fillWidth = false,
            modifier = Modifier.height(56.dp).width(280.dp),
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    }
}
