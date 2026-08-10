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
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysStatusBadge
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
        GenesysText(text = "Painel SuperAdmin", style = GenesysTextStyle.Headline)
        GenesysText(text = "Gerencie permissões de acesso ao sistema", style = GenesysTextStyle.Body)

        GenesysSpacer(GenesysSpacing.Large)

        if (isLoading && users.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (localError != null && users.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(GenesysIcons.Feedback, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysText(text = localError ?: "Erro ao carregar usuários", style = GenesysTextStyle.Error)
                    GenesysSpacer(GenesysSpacing.Large)
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
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                GenesysSpacer(GenesysSpacing.Medium)
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

            GenesysSpacer(GenesysSpacing.Medium)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            GenesysSpacer(GenesysSpacing.Medium)

            GenesysText(text = "Permissões Granulares:", style = GenesysTextStyle.Label, fontWeight = com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight.Bold)
            GenesysSpacer(GenesysSpacing.Small)

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
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun UserInfoSection(user: UserProfile, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        GenesysText(text = user.name, style = GenesysTextStyle.Title)
        GenesysText(text = user.email, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        GenesysRow(verticalAlignment = Alignment.CenterVertically) {
            GenesysText(text = "Cargo: ", style = GenesysTextStyle.Label)
            GenesysText(text = user.role.name, style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.primary)
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
            Button(onClick = { onRoleChange(UserRole.MERCHANT) }, modifier = if (modifier != Modifier) Modifier.fillMaxWidth() else Modifier) {
                Text("Tornar Merchant")
            }
        } else if (user.role == UserRole.MERCHANT) {
            OutlinedButton(onClick = { onRoleChange(UserRole.CUSTOMER) }, modifier = if (modifier != Modifier) Modifier.fillMaxWidth() else Modifier) {
                Text("Remover Acesso")
            }
        }
    }
}
