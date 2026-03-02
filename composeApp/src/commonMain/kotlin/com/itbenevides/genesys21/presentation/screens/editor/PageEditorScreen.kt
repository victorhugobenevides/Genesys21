package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun PageEditorScreen(
    viewModel: PageViewModel, 
    page: Page?, 
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(page?.title ?: "") }
    var whatsapp by remember { mutableStateOf(page?.whatsapp ?: "") }
    var customDomain by remember { mutableStateOf(page?.customDomain ?: "") }
    
    val isGlobalLoading by viewModel.isLoading.collectAsState()
    val canSave = title.isNotBlank() && !isGlobalLoading

    val onSave = {
        val newPage = (page ?: Page(id = "", title = title.trim())).copy(
            title = title.trim(),
            whatsapp = whatsapp.trim(),
            customDomain = customDomain.trim()
        )
        viewModel.savePage(newPage, isEditing = page != null) { onBack() }
    }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = if (page != null) GenesysStrings.EditPageTitle else GenesysStrings.NewPageTitle,
                onBack = onBack,
                modifier = Modifier.testTag("page_editor_app_bar")
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GenesysColumn(
                maxWidth = GenesysDimens.EditorMaxWidth,
                horizontalAlignment = GenesysAlignment.Center,
                useScroll = true,
                modifier = Modifier.padding(bottom = 80.dp) // Espaço para o botão fixo
            ) {
                // SEÇÃO 1: Identidade da Vitrine
                GenesysCard {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = "Identidade da Vitrine", 
                            style = GenesysTextStyle.Title,
                            fontWeight = com.itbenevides.genesys21.ui.components.text.GenesysFontWeight.Bold
                        )
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysTextField(
                            value = title, 
                            onValueChange = { title = it }, 
                            label = GenesysStrings.PageTitleLabel,
                            placeholder = GenesysStrings.PageTitlePlaceholder,
                            icon = GenesysIcons.Web,
                            modifier = Modifier.testTag("page_title_field")
                        )
                    }
                }
                
                GenesysSpacer(GenesysSpacing.Large)

                // SEÇÃO 2: Contato e Vendas
                GenesysCard {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = "Contato e Vendas", 
                            style = GenesysTextStyle.Title,
                            fontWeight = com.itbenevides.genesys21.ui.components.text.GenesysFontWeight.Bold
                        )
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysTextField(
                            value = whatsapp, 
                            onValueChange = { whatsapp = it }, 
                            label = "WhatsApp para Pedidos",
                            placeholder = "5511999999999",
                            icon = GenesysIcons.Chat,
                            modifier = Modifier.testTag("page_whatsapp_field")
                        )
                        
                        GenesysSpacer(GenesysSpacing.Small)
                        Text(
                            text = "O número deve incluir o DDI (Ex: 55 para Brasil).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                GenesysSpacer(GenesysSpacing.Large)

                // SEÇÃO 3: Configurações Avançadas
                GenesysCard {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = "Configurações Avançadas", 
                            style = GenesysTextStyle.Title,
                            fontWeight = com.itbenevides.genesys21.ui.components.text.GenesysFontWeight.Bold
                        )
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysTextField(
                            value = customDomain, 
                            onValueChange = { customDomain = it }, 
                            label = "Domínio Customizado",
                            placeholder = "minha-loja.com",
                            icon = GenesysIcons.Language,
                            modifier = Modifier.testTag("page_domain_field")
                        )
                    }
                }
                
                GenesysSpacer(GenesysSpacing.ExtraLarge)
            }

            // UX IMPROVEMENT: Botão de Salvar sempre visível no rodapé
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Box(Modifier.padding(16.dp)) {
                    GenesysLoadingButton(
                        onClick = onSave,
                        text = GenesysStrings.SavePageButton,
                        isLoading = isGlobalLoading,
                        enabled = canSave,
                        fillWidth = true,
                        modifier = Modifier.testTag("btn_save_page")
                    )
                }
            }
        }
    }
}
