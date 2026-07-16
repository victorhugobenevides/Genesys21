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
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysStatusBadge

@Composable
fun SuperAdminDashboard(viewModel: PageViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
    }

    GenesysColumn(usePadding = true, modifier = Modifier.fillMaxSize()) {
        GenesysText(text = "Painel SuperAdmin", style = GenesysTextStyle.Headline)
        GenesysText(text = "Gerencie permissões de acesso ao sistema", style = GenesysTextStyle.Body)

        GenesysSpacer(GenesysSpacing.Large)

        if (isLoading && users.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users) { user ->
                    UserAdminCard(user) { newRole ->
                        viewModel.updateUserRole(user.id, newRole)
                    }
                }
            }
        }
    }
}

@Composable
fun UserAdminCard(user: UserProfile, onRoleChange: (UserRole) -> Unit) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GenesysText(text = user.name, style = GenesysTextStyle.Title)
                GenesysText(text = user.email, style = GenesysTextStyle.Label)
                GenesysSpacer(GenesysSpacing.Small)
                GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                     GenesysText(text = "Cargo: ", style = GenesysTextStyle.Label)
                     GenesysText(text = user.role.name, style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.primary)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.role == UserRole.CUSTOMER) {
                    Button(onClick = { onRoleChange(UserRole.MERCHANT) }) {
                        Text("Tornar Merchant")
                    }
                } else if (user.role == UserRole.MERCHANT) {
                    OutlinedButton(onClick = { onRoleChange(UserRole.CUSTOMER) }) {
                        Text("Remover Acesso")
                    }
                }
            }
        }
    }
}
