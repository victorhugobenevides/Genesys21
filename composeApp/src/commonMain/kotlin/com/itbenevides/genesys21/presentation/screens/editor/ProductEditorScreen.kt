package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.input.GenesysDropdownField
import com.itbenevides.genesys21.ui.components.input.GenesysPhotoPicker
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysAlignment
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysRow
import com.itbenevides.genesys21.ui.components.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.InputValidator
import com.itbenevides.genesys21.util.rememberImagePicker
import kotlin.random.Random

@Composable
fun ProductEditorScreen(
    viewModel: PageViewModel,
    product: Product?,
    existingCategories: List<String>,
    onSave: (Product) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString()?.replace(".", ",") ?: "") }
    var imageUrls by remember { mutableStateOf(product?.imageUrls ?: emptyList()) }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "0") }

    val isLoading by viewModel.isLoading.collectAsState()
    var isUploading by remember { mutableStateOf(false) }
    val backendUrl = remember { getBaseUrl() }

    // O rememberImagePicker no seu projeto KMP retorna uma função () -> Unit
    val imagePicker = rememberImagePicker { bytes ->
        bytes?.let {
            if (imageUrls.size < 5) {
                isUploading = true
                viewModel.uploadImage(it, "prod_${Random.nextInt(10000)}.jpg") { uploadedUrl ->
                    imageUrls = imageUrls + uploadedUrl
                    isUploading = false
                }
            }
        }
    }

    GenesysPage(
        topBar = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.widthIn(max = GenesysDimens.EditorMaxWidth)) {
                    GenesysTopAppBar(
                        title = if (product == null) GenesysStrings.NewProduct else GenesysStrings.EditProduct,
                        onBack = onBack,
                        actions = {
                            GenesysTextButton(
                                text = GenesysStrings.Save,
                                onClick = {
                                    val finalProduct = Product(
                                        id = product?.id ?: "P-${Random.nextInt(1000, 9999)}",
                                        name = name.trim(),
                                        price = InputValidator.parsePrice(price.replace(",", ".")),
                                        imageUrls = imageUrls,
                                        description = description.trim(),
                                        category = category.trim(),
                                        stock = InputValidator.parseStock(stock)
                                    )
                                    onSave(finalProduct)
                                },
                                isLoading = isLoading,
                                enabled = name.isNotBlank() && !isUploading
                            )
                        }
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            GenesysColumn(
                useScroll = true,
                maxWidth = GenesysDimens.EditorMaxWidth,
                horizontalAlignment = GenesysAlignment.Center
            ) {
                GenesysSectionHeader(
                    title = GenesysStrings.PhotosTitle,
                    subtitle = "${imageUrls.size}/5"
                )

                GenesysSpacer(GenesysSpacing.Small)

                val displayUrls = remember(imageUrls, backendUrl) {
                    imageUrls.map { url ->
                        if (url.startsWith("/") && !url.startsWith("http")) "$backendUrl$url" else url
                    }
                }

                GenesysPhotoPicker(
                    urls = displayUrls,
                    onAddClick = {
                        if (!isUploading && imageUrls.size < 5) {
                            // CORREÇÃO: Chama a função diretamente sem o .launch()
                            imagePicker()
                        }
                    },
                    onRemoveClick = { fullUrl ->
                        val toRemove = imageUrls.find {
                            (if (it.startsWith("/") && !it.startsWith("http")) "$backendUrl$it" else it) == fullUrl
                        } ?: fullUrl
                        imageUrls = imageUrls.filter { it != toRemove }
                    },
                    isUploading = isUploading
                )

                GenesysSpacer(GenesysSpacing.Large)

                GenesysCard(elevation = GenesysDimens.ElevationLow) {
                    GenesysText("Informações Gerais", style = GenesysTextStyle.Title)
                    GenesysSpacer(GenesysSpacing.Medium)

                    GenesysTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = GenesysStrings.ProductName,
                        icon = GenesysIcons.Inventory
                    )

                    GenesysSpacer(GenesysSpacing.Medium)

                    GenesysRow {
                        GenesysTextField(
                            value = price,
                            onValueChange = { price = InputValidator.validatePrice(it) },
                            label = GenesysStrings.ProductPrice,
                            weightValue = 1f,
                            icon = GenesysIcons.Payments
                        )
                        GenesysSpacer(GenesysSpacing.Small)
                        GenesysTextField(
                            value = stock,
                            onValueChange = { stock = InputValidator.validateStock(it) },
                            label = GenesysStrings.ProductStock,
                            weightValue = 1f,
                            icon = GenesysIcons.Numbers
                        )
                    }

                    GenesysSpacer(GenesysSpacing.Medium)

                    GenesysDropdownField(
                        value = category,
                        onValueChange = { category = it },
                        label = GenesysStrings.ProductCategory,
                        options = existingCategories,
                        icon = GenesysIcons.Category
                    )

                    GenesysSpacer(GenesysSpacing.Medium)

                    GenesysTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = GenesysStrings.ProductDescription,
                        icon = GenesysIcons.Description,
                        singleLine = false,
                        minLines = 4
                    )
                }
            }
        }
    }
}
