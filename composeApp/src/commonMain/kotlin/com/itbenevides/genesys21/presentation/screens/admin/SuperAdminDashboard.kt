package com.itbenevides.genesys21.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.model.UserProfile
import com.itbenevides.genesys21.domain.model.UserPermission
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

@Composable
fun SuperAdminDashboard(viewModel: PageViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.errorEvents.collect { error ->
            if (users.isEmpty()) {
                localError = error.message
            }
        }
    }

    LaunchedEffect(Unit) {
        localError = null
        viewModel.loadAllUsers()
    }

    GenesysColumn(usePadding = true, modifier = Modifier.fillMaxWidth()) {
        GenesysText(text = "Painel SuperAdmin", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(text = "Gerencie permissões de acesso ao sistema", style = GenesysTextStyle.Body, color = GenesysTheme.colors.onSurfaceVariant)

        GenesysSpacer(GenesysTheme.spacing.l)

        if (isLoading && users.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GenesysTheme.colors.brand)
            }
        } else if (localError != null && users.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(GenesysIcons.Feedback, null, tint = GenesysTheme.colors.error, modifier = Modifier.size(48.dp))
                    GenesysSpacer(GenesysTheme.spacing.m)
                    GenesysText(text = localError ?: "Erro ao carregar usuários", style = GenesysTextStyle.Error)
                    GenesysSpacer(GenesysTheme.spacing.l)
                    GenesysLoadingButton(text = "Tentar Novamente", onClick = {
                        localError = null
                        viewModel.loadAllUsers()
                    })
                }
            }
        } else if (users.isEmpty() && !isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                GenesysText(text = "Nenhum usuário encontrado.", style = GenesysTextStyle.Body)
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.s)) {
                users.forEach { user ->
                    UserAdminCard(
                        user = user,
                        onRoleChange = { newRole -> viewModel.updateUserRole(user.id, newRole) },
                        onPermissionChange = { permission, enabled ->
                            val currentPerms = user.permissions.toMutableSet()
                            if (enabled) currentPerms.add(permission) else currentPerms.remove(permission)
                            viewModel.updateUserPermissions(user.id, currentPerms)
                        }
                    )
                }
            }
        }
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
