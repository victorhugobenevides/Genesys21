package com.itbenevides.genesys21.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.images.GenesysAvatar
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage

@Composable
fun ProfileScreen(
    viewModel: PageViewModel,
    router: Router
) {
    val userProfile by viewModel.userProfile.collectAsState()

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = "Meu Perfil",
                onBack = { router.goBack() }
            )
        }
    ) {
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = true
        ) {
            userProfile?.let { profile ->
                GenesysAvatar(
                    icon = GenesysIcons.Person,
                    modifier = Modifier.size(120.dp)
                )

                GenesysSpacer(GenesysSpacing.Large)

                GenesysText(
                    text = profile.name,
                    style = GenesysTextStyle.Headline,
                    fontWeight = GenesysFontWeight.ExtraBold
                )

                GenesysText(
                    text = profile.email,
                    style = GenesysTextStyle.Body
                )

                GenesysSpacer(GenesysSpacing.Medium)

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    GenesysText(
                        text = profile.role.name,
                        style = GenesysTextStyle.Label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                GenesysSpacer(GenesysSpacing.Huge)

                GenesysCard(modifier = Modifier.fillMaxWidth()) {
                    GenesysColumn(usePadding = false) {
                        ProfileMenuItem(
                            icon = GenesysIcons.History,
                            title = "Meus Pedidos e Agendamentos",
                            onClick = { router.navigateTo(Route.CustomerOrderHistory(null)) }
                        )

                        if (profile.role == UserRole.MERCHANT || profile.role == UserRole.SUPERADMIN) {
                            GenesysDivider()
                            ProfileMenuItem(
                                icon = GenesysIcons.Dashboard,
                                title = "Painel do Vendedor",
                                onClick = { router.navigateTo(Route.PageList) }
                            )
                        }
                    }
                }

                GenesysSpacer(GenesysSpacing.Large)

                GenesysLoadingButton(
                    text = "Sair da Conta",
                    onClick = {
                        viewModel.signOut()
                        router.navigateTo(Route.Login, replace = true)
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    fillWidth = true
                )
            } ?: run {
                GenesysText(text = "Carregando perfil...")
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysText(text = title, style = GenesysTextStyle.Body, modifier = Modifier.weight(1f))
            Icon(imageVector = GenesysIcons.ArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
