package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
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

/**
 * Tab Global de Gestão de Usuários.
 */
@Composable
fun GlobalUsersTab(viewModel: PageViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
    }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        AdminTabHeader(
            title = "Usuários Global",
            subtitle = "Gerencie permissões e cargos de todos os usuários do sistema."
        )

        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            if (isLoading && users.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GenesysTheme.colors.brand)
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
}
