package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.components.AdminTabHeader
import com.itbenevides.genesys21.presentation.screens.list.components.UserAdminCard
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

/**
 * Tab Global de Gestão de Usuários.
 */
@Composable
fun GlobalUsersTab(viewModel: PageViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            AdminTabHeader(
                title = "Usuários Global",
                subtitle = "Gerencie permissões e cargos de todos os usuários do sistema."
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isCompact) GenesysTheme.spacing.m else GenesysTheme.spacing.l)
            ) {
                if (isLoading && users.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GenesysTheme.colors.brand)
                    }
                } else if (users.isEmpty() && !isLoading) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        GenesysText(text = "Nenhum usuário encontrado.", style = GenesysTextStyle.Body)
                    }
                } else {
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
                        GenesysSpacer(GenesysTheme.spacing.m)
                    }
                }

                GenesysSpacer(GenesysTheme.spacing.huge)
            }
        }
    }
}
