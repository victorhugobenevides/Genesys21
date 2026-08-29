package com.itbenevides.genesys21.presentation.screens.list.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysStatusPicker
import com.itbenevides.genesys21.ui.components.organisms.chat.OrderChatComponent
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.util.CurrencyUtils
import kotlin.math.roundToLong

@Composable
fun OrderCardUI(
    order: Order,
    isSelected: Boolean = false,
    onStatusUpdate: (OrderStatus) -> Unit,
    onContact: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    GenesysCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = if (isSelected) 4.dp else 1.dp,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GenesysTheme.colors.brand) else null,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(GenesysTheme.spacing.s)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    val initials =
                        remember(order.customerName) {
                            order.customerName?.split(" ")?.take(2)?.mapNotNull { it.firstOrNull() }?.joinToString("")?.uppercase() ?: "C"
                        }
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(GenesysTheme.colors.accent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GenesysTheme.colors.accent,
                        )
                    }
                    Spacer(Modifier.width(GenesysTheme.spacing.xs))
                    Column {
                        Text(
                            text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}",
                            style = GenesysTheme.typography.label,
                            fontWeight = FontWeight.Bold,
                            color = GenesysTheme.colors.brand,
                        )
                        Text(
                            text = order.customerName ?: "Consumidor",
                            style = GenesysTheme.typography.title,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                GenesysStatusPicker(currentStatus = order.status, onStatusSelected = onStatusUpdate)
            }
            Spacer(Modifier.height(GenesysTheme.spacing.xs))
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = GenesysTheme.spacing.xxxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.quantity}x",
                        style = GenesysTheme.typography.body,
                        fontWeight = FontWeight.Bold,
                        color = GenesysTheme.colors.brand,
                        modifier = Modifier.width(GenesysTheme.spacing.xl),
                    )
                    Text(
                        text = item.name,
                        style = GenesysTheme.typography.body,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtotal = (item.price * item.quantity * 100.0).roundToLong() / 100.0
                    Text(
                        text = "${GenesysStrings.PricePrefix}$subtotal",
                        style = GenesysTheme.typography.body,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(GenesysTheme.spacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = GenesysStrings.OrderTotal,
                        style = GenesysTheme.typography.label,
                        color = GenesysTheme.colors.onSurfaceVariant,
                    )
                    val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
                    Text(
                        text = "${GenesysStrings.PricePrefix}$totalFormatted",
                        style = GenesysTheme.typography.headline,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                if (!order.customerPhone.isNullOrBlank()) {
                    GenesysLoadingButton(
                        text = "Falar com o cliente",
                        onClick = onContact,
                        icon = GenesysIcons.Chat,
                        fillWidth = false,
                        shape = RoundedCornerShape(GenesysTheme.spacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
fun OrderDetailContent(
    order: Order,
    chatMessages: List<ChatMessage>,
    onStatusUpdate: (OrderStatus) -> Unit,
    onContact: () -> Unit,
    onSendMessage: (String) -> Unit,
) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            GenesysText("Detalhes do Pedido", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.ExtraBold)
            GenesysSpacer(GenesysTheme.spacing.m)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    GenesysText("Pedido #${order.id.uppercase()}", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold, color = GenesysTheme.colors.brand, isSelectable = true)
                    GenesysText(order.customerName ?: "Consumidor", style = GenesysTextStyle.Headline, isSelectable = true)
                }
                GenesysStatusPicker(currentStatus = order.status, onStatusSelected = onStatusUpdate)
            }

            GenesysSpacer(GenesysTheme.spacing.l)

            OrderChatComponent(
                messages = chatMessages,
                currentNick = "Lojista",
                isMerchantView = true,
                onSendMessage = onSendMessage
            )

            GenesysSpacer(GenesysTheme.spacing.l)
            GenesysDivider()
            GenesysSpacer(GenesysTheme.spacing.l)

            GenesysText("Itens", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.s)

            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    val subtotal = (item.price * item.quantity * 100.0).roundToLong() / 100.0
                    Text("${GenesysStrings.PricePrefix}$subtotal", fontWeight = FontWeight.Bold)
                }
            }

            GenesysSpacer(GenesysTheme.spacing.l)
            GenesysDivider()
            GenesysSpacer(GenesysTheme.spacing.l)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleLarge)
                val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
                Text("${GenesysStrings.PricePrefix}$totalFormatted", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = GenesysTheme.colors.brand)
            }

            if (!order.customerPhone.isNullOrBlank()) {
                GenesysSpacer(GenesysTheme.spacing.huge)
                GenesysLoadingButton(
                    text = "Falar com o cliente",
                    onClick = onContact,
                    icon = GenesysIcons.Chat,
                    fillWidth = true
                )
            }
        }
    }
}

@Composable
fun OrdersHeaderUI(
    state: com.itbenevides.genesys21.presentation.screens.list.PageListState,
    onEvent: (com.itbenevides.genesys21.presentation.screens.list.PageListEvent) -> Unit,
) {
    val rawRevenue = remember(state.orders) { state.orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total } }
    val totalRevenue = (rawRevenue * 100.0).roundToLong() / 100.0
    val totalPending = remember(state.orders) { state.orders.count { it.status == OrderStatus.PENDING } }
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        GenesysSpacer(GenesysTheme.spacing.l)
        if (isCompact) {
            GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
                GenesysStatsCard(
                    label = GenesysStrings.Revenue,
                    value = "${GenesysStrings.PricePrefix}$totalRevenue",
                    color = Color(0xFF34C759),
                )
                GenesysSpacer(GenesysTheme.spacing.s)
                GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500))
            }
        } else {
            GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = true) {
                GenesysWeightBox(1f) {
                    GenesysStatsCard(
                        label = GenesysStrings.Revenue,
                        value = "${GenesysStrings.PricePrefix}$totalRevenue",
                        color = Color(0xFF34C759),
                    )
                }
                GenesysSpacer(GenesysTheme.spacing.m)
                GenesysWeightBox(
                    1f,
                ) { GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500)) }
            }
        }
        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysTextField(value = state.searchQuery, onValueChange = {
                onEvent(com.itbenevides.genesys21.presentation.screens.list.PageListEvent.OnSearchQueryChanged(it))
            }, label = GenesysStrings.SearchOrdersLabel, icon = GenesysIcons.Search)
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysRow(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GenesysFilterChip(selected = state.selectedStatusFilter == null, onClick = {
                    onEvent(com.itbenevides.genesys21.presentation.screens.list.PageListEvent.OnStatusFilterSelected(null))
                }, label = GenesysStrings.All, badgeCount = state.orders.size)
                OrderStatus.entries.forEach { status ->
                    val label =
                        when (status) {
                            OrderStatus.PENDING -> GenesysStrings.StatusPending
                            OrderStatus.AWAITING_PAYMENT -> "Aguardando Pagamento"
                            OrderStatus.PROCESSING -> GenesysStrings.StatusProcessing
                            OrderStatus.COMPLETED -> GenesysStrings.StatusCompleted
                            OrderStatus.CANCELLED -> GenesysStrings.StatusCancelled
                        }
                    GenesysFilterChip(selected = state.selectedStatusFilter == status, onClick = {
                        onEvent(com.itbenevides.genesys21.presentation.screens.list.PageListEvent.OnStatusFilterSelected(status))
                    }, label = label, badgeCount = state.orders.count { it.status == status })
                }
            }
        }
        GenesysSpacer(GenesysTheme.spacing.m)
    }
}

@Composable
fun PageItemRow(
    page: Page,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onRename: () -> Unit,
    onCopyUrl: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        GenesysColumn(usePadding = false) {
            GenesysRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                GenesysBox(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(GenesysTheme.colors.brandContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(GenesysIcons.Web, null, tint = GenesysTheme.colors.brand, modifier = Modifier.size(20.dp))
                }
                GenesysSpacer(GenesysTheme.spacing.m)
                GenesysColumn(modifier = Modifier.weight(1f), usePadding = false) {
                    GenesysText(
                        text = page.title,
                        style = GenesysTextStyle.Title,
                        fontWeight = GenesysFontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GenesysText(text = "ID: ${page.id}", style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysRow(fillWidth = true, horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                GenesysIconButton(icon = GenesysIcons.Visibility, onClick = onView)
                GenesysIconButton(icon = GenesysIcons.Edit, onClick = onRename)
                GenesysIconButton(icon = GenesysIcons.Magic, onClick = onEdit)
                GenesysIconButton(icon = GenesysIcons.Copy, onClick = onCopyUrl)
                GenesysIconButton(icon = GenesysIcons.CloudUpload, onClick = onExport)
                GenesysIconButton(icon = GenesysIcons.Delete, onClick = onDelete, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ToggleOptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GenesysText(text = label, style = GenesysTextStyle.Body)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AdminTabHeader(
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null
) {
    GenesysColumn(usePadding = true, modifier = Modifier.fillMaxWidth()) {
        GenesysSpacer(GenesysTheme.spacing.l)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                GenesysText(
                    text = title,
                    style = GenesysTextStyle.Headline,
                    fontWeight = GenesysFontWeight.ExtraBold
                )
                GenesysText(
                    text = subtitle,
                    style = GenesysTextStyle.Body,
                    color = GenesysTheme.colors.onSurfaceVariant
                )
            }
            if (action != null) {
                Box(Modifier.padding(start = 16.dp)) {
                    action()
                }
            }
        }
        GenesysSpacer(GenesysTheme.spacing.l)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserAdminCard(
    user: UserProfile,
    onRoleChange: (UserRole) -> Unit,
    onPermissionChange: (UserPermission, Boolean) -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isCompact) {
                UserInfoSection(user)
                GenesysSpacer(GenesysTheme.spacing.m)
                UserActionsSection(user, onRoleChange, modifier = Modifier.fillMaxWidth())
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    UserInfoSection(user, modifier = Modifier.weight(1f))
                    UserActionsSection(user, onRoleChange)
                }
            }

            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysDivider()
            GenesysSpacer(GenesysTheme.spacing.m)

            GenesysText(text = "Permissões Granulares:", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.s)

            // Grid de Permissões
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UserPermission.entries.forEach { permission ->
                    PermissionCheckbox(
                        label = when(permission) {
                            UserPermission.MANAGE_VITRINES -> "Vitrines"
                            UserPermission.MANAGE_ORDERS -> "Pedidos"
                            UserPermission.MANAGE_AGENDA -> "Agenda"
                            UserPermission.MANAGE_SERVICES -> "Serviços"
                            UserPermission.MANAGE_STORE -> "Loja"
                            UserPermission.MANAGE_RECEIPTS -> "Notas"
                            UserPermission.ACCESS_ADMIN_PANEL -> "Painel Adm"
                        },
                        checked = user.permissions.contains(permission),
                        onCheckedChange = { onPermissionChange(permission, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = GenesysTheme.colors.brand,
                uncheckedColor = GenesysTheme.colors.outline
            )
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = GenesysTheme.colors.onSurface)
    }
}

@Composable
private fun UserInfoSection(user: UserProfile, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        GenesysText(text = user.name, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysText(text = user.email, style = GenesysTextStyle.Label, color = GenesysTheme.colors.onSurfaceVariant)
        GenesysSpacer(GenesysTheme.spacing.s)
        GenesysRow(verticalAlignment = Alignment.CenterVertically, usePadding = false) {
            GenesysText(text = "Cargo: ", style = GenesysTextStyle.Label)
            GenesysText(text = user.role.name, style = GenesysTextStyle.Label, color = GenesysTheme.colors.brand, fontWeight = GenesysFontWeight.Bold)
        }
    }
}

@Composable
private fun UserActionsSection(
    user: UserProfile,
    onRoleChange: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        if (user.role == UserRole.CUSTOMER) {
            GenesysLoadingButton(
                text = "Tornar Merchant",
                onClick = { onRoleChange(UserRole.MERCHANT) },
                fillWidth = modifier != Modifier
            )
        } else if (user.role == UserRole.MERCHANT) {
            GenesysTextButton(
                text = "Remover Acesso",
                onClick = { onRoleChange(UserRole.CUSTOMER) },
                color = GenesysTheme.colors.error,
                modifier = if (modifier != Modifier) Modifier.fillMaxWidth() else Modifier
            )
        }
    }
}
