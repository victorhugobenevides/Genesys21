package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Appointment
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.AnalyticsManager
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun CustomerOrderHistoryScreen(
    onBack: () -> Unit,
    onOrderClick: (Order) -> Unit,
) {
    val viewModel: PageViewModel = koinViewModel()
    val orders by viewModel.customerOrders.collectAsState()
    val appointments by viewModel.customerAppointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCustomerOrders()
        AnalyticsManager.trackPageView(GenesysStrings.OrderHistoryTitle)
    }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.OrderHistoryTitle,
                onBack = onBack,
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            if (orders.isEmpty() && appointments.isEmpty() && !isLoading) {
                GenesysEmptyState(
                    icon = GenesysIcons.ShoppingBag,
                    title = GenesysStrings.NoHistoryTitle,
                    description = GenesysStrings.NoHistoryDescription,
                    action = {
                        GenesysLoadingButton(
                            text = GenesysStrings.Back,
                            onClick = onBack,
                        )
                    },
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().widthIn(max = GenesysDimens.ContentMaxWidth).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (appointments.isNotEmpty()) {
                        item {
                            GenesysText(
                                text = "Seus Agendamentos",
                                style = GenesysTextStyle.Title,
                                fontWeight = GenesysFontWeight.Bold
                            )
                        }
                        items(appointments) { appointment ->
                            HistoryAppointmentCard(appointment)
                        }
                    }

                    if (orders.isNotEmpty()) {
                        item {
                            GenesysSpacer(GenesysSpacing.Large)
                            GenesysText(
                                text = "Seus Pedidos",
                                style = GenesysTextStyle.Title,
                                fontWeight = GenesysFontWeight.Bold
                            )
                        }
                        items(orders) { order ->
                            HistoryOrderCard(
                                order = order,
                                onClick = { onOrderClick(order) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryAppointmentCard(appointment: Appointment) {
    val startTime = appointment.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val dateStr = "${startTime.dayOfMonth.toString().padStart(2, '0')}/${startTime.monthNumber.toString().padStart(2, '0')}/${startTime.year}"
    val timeStr = "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}"

    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        GenesysColumn(usePadding = true) {
            GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                GenesysWeightBox(1f) {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = "Agendamento: $dateStr às $timeStr",
                            fontWeight = GenesysFontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        GenesysText(
                            text = "Status: ${appointment.status.name}",
                            style = GenesysTextStyle.Label,
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = when (appointment.status.name) {
                        "CONFIRMED" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        "CANCELLED" -> Color(0xFFF44336).copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        appointment.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Show public notes
            val publicNotes = appointment.notes.filter { !it.isPrivate }
            if (publicNotes.isNotEmpty()) {
                GenesysSpacer(GenesysSpacing.Small)
                GenesysDivider()
                GenesysSpacer(GenesysSpacing.Small)
                publicNotes.forEach { note ->
                    GenesysText(
                        text = "${note.authorName}: ${note.content}",
                        style = GenesysTextStyle.Label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryOrderCard(
    order: Order,
    onClick: () -> Unit,
) {
    val dateText =
        remember(order.createdAt) {
            val instant = Instant.fromEpochMilliseconds(order.createdAt)
            val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val month = dt.monthNumber
            "${dt.dayOfMonth.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/${dt.year}"
        }

    GenesysCard(
        elevation = GenesysDimens.ElevationMedium,
        onClick = onClick,
    ) {
        GenesysColumn(usePadding = true) {
            GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                GenesysWeightBox(1f) {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}",
                            fontWeight = GenesysFontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        GenesysText(
                            text = dateText,
                            style = GenesysTextStyle.Label,
                        )
                    }
                }
                GenesysStatusBadge(order.status)
            }

            GenesysSpacer(GenesysSpacing.Medium)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Medium)

            GenesysRow(verticalAlignment = Alignment.Bottom) {
                GenesysWeightBox(1f) {
                    GenesysText(
                        text = "${order.items.sumOf { it.quantity }} itens",
                        style = GenesysTextStyle.Body,
                    )
                }
                val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
                GenesysText(
                    text = "${GenesysStrings.PricePrefix}$totalFormatted",
                    style = GenesysTextStyle.Title,
                    fontWeight = GenesysFontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
