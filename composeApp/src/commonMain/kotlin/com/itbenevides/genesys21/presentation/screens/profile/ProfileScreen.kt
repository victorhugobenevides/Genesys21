package com.itbenevides.genesys21.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysConfirmDialog
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: PageViewModel,
    router: Router
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val addresses by viewModel.userAddresses.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
            usePadding = true,
            useScroll = true
        ) {
            userProfile?.let { profile ->
                val displayAvatarUrl = profile.avatarUrl ?: "https://ui-avatars.com/api/?name=${profile.name.replace(" ", "+")}&size=300&background=000&color=fff"

                com.itbenevides.genesys21.ui.components.atoms.images.GenesysImage(
                    url = displayAvatarUrl,
                    size = 120.dp,
                    isCircular = true
                )

                GenesysSpacer(GenesysTheme.spacing.l)

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

                GenesysSpacer(GenesysTheme.spacing.m)

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

                GenesysSpacer(GenesysTheme.spacing.huge)

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
                                    router.navigateTo(Route.Cart(null))
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

                GenesysSpacer(GenesysTheme.spacing.l)

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
                            onClick = { showEditDialog = true }
                        )
                    }
                }

                GenesysSpacer(GenesysTheme.spacing.l)

                // Meus Endereços
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GenesysText(
                        text = "Meus Endereços",
                        style = GenesysTextStyle.Title,
                        fontWeight = GenesysFontWeight.Bold
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.Add,
                        onClick = { showAddAddressDialog = true }
                    )
                }

                GenesysSpacer(GenesysTheme.spacing.m)

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

                GenesysSpacer(GenesysTheme.spacing.l)

                // Privacidade e Dados
                GenesysText(
                    text = "Privacidade & Dados",
                    style = GenesysTextStyle.Title,
                    fontWeight = GenesysFontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                GenesysSpacer(GenesysTheme.spacing.m)

                GenesysCard(modifier = Modifier.fillMaxWidth()) {
                    GenesysColumn(usePadding = false) {
                        ProfileMenuItem(
                            icon = GenesysIcons.Delete,
                            title = "Excluir minha conta permanentemente",
                            titleColor = GenesysTheme.colors.error,
                            onClick = { showDeleteConfirmDialog = true }
                        )
                    }
                }

                GenesysSpacer(GenesysTheme.spacing.huge)

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

    if (showEditDialog && userProfile != null) {
        EditProfileDialog(
            profile = userProfile!!,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.saveUserProfile(updated)
                showEditDialog = false
            }
        )
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddAddressDialog = false },
            onSave = { address ->
                viewModel.saveAddress(address)
                showAddAddressDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        GenesysConfirmDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = "Excluir Conta?",
            text = "Esta ação é irreversível. Todos os seus dados, vitrines e pedidos serão anonimizados ou removidos conforme a LGPD.",
            confirmButton = {
                GenesysLoadingButton(
                    text = "Excluir Permanentemente",
                    containerColor = GenesysTheme.colors.error,
                    onClick = {
                        viewModel.deleteAccount {
                            showDeleteConfirmDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                GenesysTextButton(text = "Cancelar", onClick = { showDeleteConfirmDialog = false })
            }
        )
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone ?: "") }
    var avatarUrl by remember { mutableStateOf(profile.avatarUrl ?: "") }

    GenesysDialog(
        onDismissRequest = onDismiss,
        title = "Editar Perfil",
        confirmButton = {
            GenesysLoadingButton(
                text = "Salvar",
                onClick = { onSave(profile.copy(name = name, phone = phone, avatarUrl = avatarUrl.ifBlank { null })) }
            )
        },
        dismissButton = {
            GenesysTextButton(text = "Cancelar", onClick = onDismiss)
        }
    ) {
        GenesysColumn(usePadding = false) {
            GenesysTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome Completo",
                icon = GenesysIcons.Person
            )
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Telefone/WhatsApp",
                icon = GenesysIcons.Chat
            )
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysTextField(
                value = avatarUrl,
                onValueChange = { avatarUrl = it },
                label = "URL do Avatar",
                icon = GenesysIcons.Web
            )
        }
    }
}

@Composable
private fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit
) {
    var street by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }

    GenesysDialog(
        onDismissRequest = onDismiss,
        title = "Novo Endereço",
        confirmButton = {
            GenesysLoadingButton(
                text = "Adicionar",
                enabled = street.isNotBlank() && number.isNotBlank() && city.isNotBlank() && zipCode.isNotBlank(),
                onClick = {
                    onSave(
                        Address(
                            id = "", // Server generates
                            userId = "", // ViewModel fills
                            street = street,
                            number = number,
                            neighborhood = neighborhood,
                            city = city,
                            state = state,
                            zipCode = zipCode
                        )
                    )
                }
            )
        },
        dismissButton = {
            GenesysTextButton(text = "Cancelar", onClick = onDismiss)
        }
    ) {
        GenesysColumn(usePadding = false) {
            GenesysTextField(value = zipCode, onValueChange = { zipCode = it }, label = "CEP", icon = GenesysIcons.Search)
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysTextField(value = street, onValueChange = { street = it }, label = "Rua/Logradouro")
            GenesysSpacer(GenesysTheme.spacing.m)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { GenesysTextField(value = number, onValueChange = { number = it }, label = "Nº") }
                Box(Modifier.weight(2f)) { GenesysTextField(value = neighborhood, onValueChange = { neighborhood = it }, label = "Bairro") }
            }
            GenesysSpacer(GenesysTheme.spacing.m)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(2f)) { GenesysTextField(value = city, onValueChange = { city = it }, label = "Cidade") }
                Box(Modifier.weight(1f)) { GenesysTextField(value = state, onValueChange = { state = it }, label = "UF") }
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
            GenesysSpacer(GenesysTheme.spacing.s)
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
    titleColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
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
            Icon(imageVector = icon, contentDescription = null, tint = if (titleColor != androidx.compose.ui.graphics.Color.Unspecified) titleColor else GenesysTheme.colors.brand)
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysText(text = title, style = GenesysTextStyle.Body, color = titleColor, modifier = Modifier.weight(1f))
            Icon(imageVector = GenesysIcons.ArrowRight, contentDescription = null, tint = GenesysTheme.colors.outline)
        }
    }
}
