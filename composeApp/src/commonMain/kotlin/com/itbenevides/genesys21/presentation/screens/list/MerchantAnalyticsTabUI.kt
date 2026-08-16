package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.util.CurrencyUtils

@Composable
fun MerchantAnalyticsTabUI(
    viewModel: PageViewModel
) {
    val analytics by viewModel.analytics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysText(text = "Painel de Controle", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(text = "Visão geral da saúde do seu negócio.", style = GenesysTextStyle.Body, color = GenesysTheme.colors.onSurfaceVariant)

        GenesysSpacer(GenesysTheme.spacing.l)

        if (isLoading && analytics == null) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GenesysTheme.colors.brand)
            }
        } else {
            analytics?.let { data ->
                // Quick Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenesysStatsCard(
                        label = "Pedidos",
                        value = data.totalOrders.toString(),
                        color = GenesysTheme.colors.brand,
                        modifier = Modifier.weight(1f)
                    )
                    GenesysStatsCard(
                        label = "Ticket Médio",
                        value = "R$ ${CurrencyUtils.formatDisplay(data.averageTicket)}",
                        color = GenesysTheme.colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                }

                GenesysSpacer(GenesysTheme.spacing.m)

                // Revenue Chart
                DailyRevenueChart(data.dailyRevenue)

                GenesysSpacer(GenesysTheme.spacing.l)

                // Best Sellers
                TopProductsCard(data.topProducts)

                GenesysSpacer(GenesysTheme.spacing.l)

                // Booking Summary
                BookingStatusCard(data.bookingSummary)
            }
        }
    }
}

@Composable
private fun DailyRevenueChart(dailyRevenue: List<DailyRevenue>) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            GenesysText(text = "Receita nos Últimos 7 Dias", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            if (dailyRevenue.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    GenesysText(text = "Ainda não há dados suficientes.", color = GenesysTheme.colors.outline)
                }
            } else {
                val maxRevenue = dailyRevenue.maxOf { it.amount }.coerceAtLeast(10.0)
                val brandColor = GenesysTheme.colors.brand
                val windowSizeClass = LocalWindowSizeClass.current
                val chartHeight = if (windowSizeClass == GenesysWindowSizeClass.COMPACT) 150.dp else 220.dp

                Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (dailyRevenue.size.coerceAtLeast(2) - 1).coerceAtLeast(1)

                    val points = dailyRevenue.mapIndexed { index, data ->
                        val x = index * spacing
                        val y = height - (data.amount / maxRevenue * height).toFloat()
                        Offset(x, y)
                    }

                    // Preenchimento gradiente abaixo da linha
                    val fillPath = Path().apply {
                        points.firstOrNull()?.let { moveTo(it.x, height) }
                        points.forEach { lineTo(it.x, it.y) }
                        points.lastOrNull()?.let { lineTo(it.x, height) }
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(brandColor.copy(alpha = 0.3f), Color.Transparent),
                            startY = 0f,
                            endY = height.toFloat()
                        )
                    )

                    val path = Path().apply {
                        points.firstOrNull()?.let { moveTo(it.x, it.y) }
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                    }

                    drawPath(
                        path = path,
                        color = brandColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw circles
                    points.forEach { offset ->
                        drawCircle(color = brandColor, radius = 4.dp.toPx(), center = offset)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    dailyRevenue.forEach {
                        val day = it.date.takeLast(2)
                        Text(day, style = MaterialTheme.typography.labelSmall, color = GenesysTheme.colors.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopProductsCard(products: List<TopProduct>) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            GenesysText(text = "Produtos Mais Vendidos", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            if (products.isEmpty()) {
                GenesysText(text = "Nenhuma venda registrada.", color = GenesysTheme.colors.outline)
            } else {
                products.forEachIndexed { index, product ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp).background(GenesysTheme.colors.brandContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((index + 1).toString(), style = MaterialTheme.typography.labelSmall, color = GenesysTheme.colors.brand)
                        }
                        GenesysSpacer(GenesysTheme.spacing.m)
                        Column(Modifier.weight(1f)) {
                            GenesysText(text = product.name, fontWeight = GenesysFontWeight.Bold)
                            GenesysText(text = "${product.quantity} unidades vendidas", style = GenesysTextStyle.Label, color = GenesysTheme.colors.onSurfaceVariant)
                        }
                        GenesysText(
                            text = "R$ ${CurrencyUtils.formatDisplay(product.revenue)}",
                            fontWeight = GenesysFontWeight.ExtraBold,
                            color = GenesysTheme.colors.brand
                        )
                    }
                    if (index < products.size - 1) GenesysDivider()
                }
            }
        }
    }
}

@Composable
private fun BookingStatusCard(summary: BookingSummary) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            GenesysText(text = "Agenda e Compromissos", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.m)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatusItem("Pendentes", summary.pending, Color(0xFFFF9500))
                StatusItem("Confirmados", summary.confirmed, Color(0xFF34C759))
                StatusItem("Próximos", summary.upcoming, GenesysTheme.colors.accent)
            }
        }
    }
}

@Composable
private fun StatusItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = GenesysTheme.colors.outline)
    }
}
