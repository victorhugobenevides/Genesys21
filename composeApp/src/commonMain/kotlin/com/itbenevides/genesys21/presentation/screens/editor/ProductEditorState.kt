package com.itbenevides.genesys21.presentation.screens.editor

import com.itbenevides.genesys21.domain.model.Product

/**
 * UI State para a tela de Editor de Produtos.
 */
data class ProductEditorState(
    val name: String = "",
    val price: String = "",
    val imageUrls: List<String> = emptyList(),
    val description: String = "",
    val categoryId: Int? = null,
    val categoryName: String = "",
    val stock: String = "0",
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isEditing: Boolean = false
) {
    companion object {
        fun initial(product: Product?) = ProductEditorState(
            name = product?.name ?: "",
            price = product?.price?.toString()?.replace(".", ",") ?: "",
            imageUrls = product?.imageUrls ?: emptyList(),
            description = product?.description ?: "",
            categoryId = product?.categoryId,
            categoryName = product?.categoryName ?: "",
            stock = product?.stock?.toString() ?: "0",
            isEditing = product != null
        )
    }

    val canSave: Boolean get() = name.isNotBlank() && !isUploading && !isLoading
}

/**
 * UI Intents (Eventos) para a tela de Editor de Produtos.
 */
sealed class ProductEditorEvent {
    data class OnNameChanged(val name: String) : ProductEditorEvent()
    data class OnPriceChanged(val price: String) : ProductEditorEvent()
    data class OnDescriptionChanged(val description: String) : ProductEditorEvent()
    data class OnCategoryChanged(val categoryId: Int?, val categoryName: String) : ProductEditorEvent()
    data class OnStockChanged(val stock: String) : ProductEditorEvent()
    object OnAddPhotoClicked : ProductEditorEvent()
    data class OnRemovePhotoClicked(val url: String) : ProductEditorEvent()
    object OnSaveClicked : ProductEditorEvent()
}
