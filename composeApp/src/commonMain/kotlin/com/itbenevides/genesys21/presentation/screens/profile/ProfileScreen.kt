package com.itbenevides.genesys21.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysAlignment
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysDivider
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: PageViewModel,
    router: Router
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val addresses by viewModel.userAddresses.collectAsState()

    // Redirecionamento se deslogado
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            router.navigateTo(Route.Login, replace = true)
        }
    }

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
                val displayAvatarUrl = profile.avatarUrl ?: "https://ui-avatars.com/api/?name=${profile.name.replace(" ", "+")}&size=300&background=000&color=fff"

                com.itbenevides.genesys21.ui.components.atoms.images.GenesysImage(
                    url = displayAvatarUrl,
                    size = 120.dp,
                    isCircular = true
                )

                GenesysSpacer(GenesysSpacing.Large)

                GenesysText(
                    text = profile.name,
                    style = GenesysTextStyle.Headline,
                    fontWeight = GenesysFontWeight.ExtraBold
                )

                GenesysText(
                    text = profile.email,
                    style = GenesysTextStyle.Body,
                    color = GenesysTheme.colors.onSurfaceVariant
                )

                GenesysSpacer(GenesysSpacing.Medium)

                Surface(
                    shape = CircleShape,
                    color = GenesysTheme.colors.brandContainer.copy(alpha = 0.5f)
                ) {
                    GenesysText(
                        text = profile.role.name,
                        style = GenesysTextStyle.Label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = GenesysTheme.colors.brand,
                        fontWeight = GenesysFontWeight.Bold
                    )
                }

                GenesysSpacer(GenesysSpacing.Huge)

                // Quick Actions Grid
                GenesysRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenesysWeightBox(1f) {
                        ActionCard(
                            icon = GenesysIcons.ShoppingCart,
                            title = "Carrinho",
                            onClick = {
                                val firstPage = viewModel.pages.value.firstOrNull()
                                if (firstPage != null) {
                                    router.navigateTo(Route.Cart(firstPage))
                                } else {
                                    // Fallback if no page loaded yet, though unlikely in profile
                                }
                            }
                        )
                    }
                    GenesysWeightBox(1f) {
                        ActionCard(
                            icon = GenesysIcons.History,
                            title = "Pedidos",
                            onClick = { router.navigateTo(Route.CustomerOrderHistory(null)) }
                        )
                    }
                }

                GenesysSpacer(GenesysSpacing.Large)

                GenesysCard(modifier = Modifier.fillMaxWidth()) {
                    GenesysColumn(usePadding = false) {
                        if (profile.role == UserRole.MERCHANT || profile.role == UserRole.SUPERADMIN) {
                            ProfileMenuItem(
                                icon = GenesysIcons.Dashboard,
                                title = "Painel Administrativo",
                                onClick = { router.navigateTo(Route.PageList) }
                            )
                            GenesysDivider()
                        }

                        ProfileMenuItem(
                            icon = GenesysIcons.Person,
                            title = "Editar Meus Dados",
                            onClick = { /* TODO */ }
                        )
                    }
                }

                GenesysSpacer(GenesysSpacing.Large)

                // Meus Endereços
                GenesysText(
                    text = "Meus Endereços",
                    style = GenesysTextStyle.Title,
                    fontWeight = GenesysFontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                GenesysSpacer(GenesysSpacing.Medium)

                if (addresses.isEmpty()) {
                    GenesysText(
                        text = "Nenhum endereço cadastrado.",
                        style = GenesysTextStyle.Label,
                        color = GenesysTheme.colors.onSurfaceVariant
                    )
                } else {
                    addresses.forEach { address ->
                        GenesysCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    GenesysText(text = "${address.street}, ${address.number}", fontWeight = GenesysFontWeight.Bold)
                                    GenesysText(text = "${address.neighborhood} - ${address.city}/${address.state}", style = GenesysTextStyle.Label)
                                    GenesysText(text = address.zipCode, style = GenesysTextStyle.Label)
                                }
                                GenesysIconButton(
                                    icon = GenesysIcons.Delete,
                                    tint = GenesysTheme.colors.error.copy(alpha = 0.6f),
                                    onClick = { viewModel.deleteAddress(address.id) }
                                )
                            }
                        }
                    }
                }

                GenesysSpacer(GenesysSpacing.Huge)

                GenesysLoadingButton(
                    text = "Sair da Conta",
                    onClick = {
                        viewModel.signOut()
                        router.navigateTo(Route.Login, replace = true)
                    },
                    containerColor = GenesysTheme.colors.error.copy(alpha = 0.1f),
                    shape = CircleShape,
                    fillWidth = true
                )
            } ?: run {
                GenesysText(text = "Carregando perfil...")
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    GenesysCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GenesysTheme.colors.brand,
                modifier = Modifier.size(32.dp)
            )
            GenesysSpacer(GenesysSpacing.Small)
            GenesysText(
                text = title,
                style = GenesysTextStyle.Label,
                fontWeight = GenesysFontWeight.Bold
            )
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
            Icon(imageVector = icon, contentDescription = null, tint = GenesysTheme.colors.brand)
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysText(text = title, style = GenesysTextStyle.Body, modifier = Modifier.weight(1f))
            Icon(imageVector = GenesysIcons.ArrowRight, contentDescription = null, tint = GenesysTheme.colors.outline)
        }
    }
}
