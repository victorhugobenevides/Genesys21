package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.components.AdminTabHeader
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.util.CurrencyUtils
import kotlin.math.roundToLong

@Composable
fun B2BInsightsTab(viewModel: PageViewModel) {
    val b2bData by viewModel.b2bAnalytics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    LaunchedEffect(Unit) {
        viewModel.loadB2BAnalytics()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            AdminTabHeader(
                title = "B2B Insights",
                subtitle = "Visão macro da performance de toda a rede de lojistas."
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isCompact) GenesysTheme.spacing.m else GenesysTheme.spacing.l)
            ) {
                b2bData?.let { data ->
                    // KPI Section - Responsive
                    if (isCompact) {
                        GenesysStatsCard(label = "GMV Global", value = "R$ ${CurrencyUtils.formatDisplay(data.platformGMV)}", color = Color(0xFF34C759))
                        GenesysSpacer(GenesysTheme.spacing.m)
                        GenesysStatsCard(label = "Lojistas Ativos", value = data.totalMerchants.toString(), color = GenesysTheme.colors.brand)
                        GenesysSpacer(GenesysTheme.spacing.m)
                        GenesysStatsCard(label = "Ticket Médio Rede", value = "R$ ${CurrencyUtils.formatDisplay(data.globalAverageTicket)}", color = Color(0xFF5856D6))
                        GenesysSpacer(GenesysTheme.spacing.m)
                        GenesysStatsCard(label = "Conversão Média", value = "3.2%", color = Color(0xFFFF9500))
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.m)) {
                            Box(Modifier.weight(1f)) { GenesysStatsCard(label = "GMV Global", value = "R$ ${CurrencyUtils.formatDisplay(data.platformGMV)}", color = Color(0xFF34C759)) }
                            Box(Modifier.weight(1f)) { GenesysStatsCard(label = "Lojistas Ativos", value = data.totalMerchants.toString(), color = GenesysTheme.colors.brand) }
                        }
                        GenesysSpacer(GenesysTheme.spacing.m)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.m)) {
                            Box(Modifier.weight(1f)) { GenesysStatsCard(label = "Ticket Médio Rede", value = "R$ ${CurrencyUtils.formatDisplay(data.globalAverageTicket)}", color = Color(0xFF5856D6)) }
                            Box(Modifier.weight(1f)) { GenesysStatsCard(label = "Conversão Média", value = "3.2%", color = Color(0xFFFF9500)) }
                        }
                    }

                    GenesysSpacer(GenesysTheme.spacing.xl)

                    GenesysText(text = "Ranking de Performance (Top Lojistas)", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                    GenesysSpacer(GenesysTheme.spacing.m)

                    data.topMerchants.forEachIndexed { index, merchant ->
                        MerchantPerformanceRow(index + 1, merchant)
                        GenesysSpacer(GenesysTheme.spacing.s)
                    }
                }

                GenesysSpacer(GenesysTheme.spacing.huge)
            }
        }
    }
}

@Composable
private fun MerchantPerformanceRow(rank: Int, performance: com.itbenevides.genesys21.domain.model.MerchantPerformance) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(GenesysTheme.colors.brandContainer, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GenesysText(text = rank.toString(), style = GenesysTextStyle.Label, color = GenesysTheme.colors.brand, fontWeight = GenesysFontWeight.Bold)
            }
            GenesysSpacer(GenesysTheme.spacing.m)
            Column(Modifier.weight(1f)) {
                GenesysText(text = performance.merchantName, fontWeight = GenesysFontWeight.Bold)
                GenesysText(text = "${performance.orderCount} pedidos finalizados", style = GenesysTextStyle.Label, color = GenesysTheme.colors.onSurfaceVariant)
            }
            GenesysText(
                text = "R$ ${CurrencyUtils.formatDisplay(performance.totalRevenue)}",
                fontWeight = GenesysFontWeight.ExtraBold,
                color = GenesysTheme.colors.brand
            )
        }
    }
}
