package com.itbenevides.genesys21.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.shape.RoundedCornerShape
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
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
    var selectedTab by remember { mutableStateOf(0) }

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
        GenesysText(text = "Gerencie permissões e infraestrutura global", style = GenesysTextStyle.Body, color = GenesysTheme.colors.onSurfaceVariant)

        GenesysSpacer(GenesysTheme.spacing.l)

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = GenesysTheme.colors.surface,
            contentColor = GenesysTheme.colors.brand,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GenesysTheme.colors.brand
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Box(Modifier.padding(16.dp)) { GenesysText("Usuários", fontWeight = if (selectedTab == 0) GenesysFontWeight.Bold else GenesysFontWeight.Normal) }
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Box(Modifier.padding(16.dp)) { GenesysText("Domínios Globais", fontWeight = if (selectedTab == 1) GenesysFontWeight.Bold else GenesysFontWeight.Normal) }
            }
        }

        GenesysSpacer(GenesysTheme.spacing.m)

        if (selectedTab == 0) {
            UsersManagementTab(users, isLoading, localError, viewModel)
        } else {
            DomainsManagementTab(viewModel)
        }
    }
}

@Composable
private fun UsersManagementTab(
    users: List<UserProfile>,
    isLoading: Boolean,
    localError: String?,
    viewModel: PageViewModel
) {
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

@Composable
private fun DomainsManagementTab(viewModel: PageViewModel) {
    val mappings by viewModel.domainMappings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDomainMappings()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GenesysText(text = "Mapeamentos de Domínios", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            GenesysLoadingButton(
                text = "Adicionar Domínio",
                onClick = { showAddDialog = true },
                icon = GenesysIcons.Add
            )
        }

        GenesysSpacer(GenesysTheme.spacing.m)

        if (isLoading && mappings.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GenesysTheme.colors.brand)
            }
        } else if (mappings.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                GenesysText(text = "Nenhum domínio mapeado.", style = GenesysTextStyle.Body)
            }
        } else {
            mappings.forEach { mapping ->
                DomainMappingCard(mapping, onDelete = { viewModel.deleteDomainMapping(mapping.id) })
            }
        }
    }

    if (showAddDialog) {
        AddDomainDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { domain, pageId ->
                viewModel.saveDomainMapping(domain, pageId)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DomainMappingCard(mapping: DomainMapping, onDelete: () -> Unit) {
    GenesysCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GenesysText(text = mapping.domain, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                GenesysText(text = "Direciona para: ${mapping.targetPageId}", style = GenesysTextStyle.Label)
            }
            GenesysIconButton(
                icon = GenesysIcons.Delete,
                onClick = onDelete,
                tint = GenesysTheme.colors.error
            )
        }
    }
}

@Composable
private fun AddDomainDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var domain by remember { mutableStateOf("") }
    var pageId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { GenesysText("Mapear Novo Domínio", style = GenesysTextStyle.Title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GenesysTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = "Domínio (ex: loja.com)",
                    placeholder = "meu-site.com"
                )
                GenesysTextField(
                    value = pageId,
                    onValueChange = { pageId = it },
                    label = "ID da Vitrine (UUID)",
                    placeholder = "ID da página no Genesys21"
                )
            }
        },
        confirmButton = {
            GenesysLoadingButton(
                text = "Salvar Mapeamento",
                onClick = { onConfirm(domain, pageId) },
                enabled = domain.isNotBlank() && pageId.isNotBlank()
            )
        },
        dismissButton = {
            GenesysTextButton(text = "Cancelar", onClick = onDismiss)
        },
        containerColor = GenesysTheme.colors.surface,
        shape = RoundedCornerShape(GenesysTheme.config.cornerRadius)
    )
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
