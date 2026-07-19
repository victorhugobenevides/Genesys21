package com.itbenevides.genesys21.ui.components.organisms.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.viewer.ProductCard
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import kotlinx.coroutines.launch

/**
 * GenesysProductList: An Organism that manages a collection of ProductCard molecules.
 * Handles both Grid and Horizontal Scroll layouts with responsiveness.
 */
@Composable
fun GenesysProductList(
    products: List<Product>,
    modifier: Modifier = Modifier,
    isHorizontal: Boolean = false,
    isEditMode: Boolean = false,
    onProductClick: ((Product) -> Unit)? = null,
    onAddToCart: ((Product) -> Unit)? = null,
    onHover: ((Product) -> Unit)? = null,
) {
    if (products.isEmpty()) return

    val windowSizeClass = LocalWindowSizeClass.current
    val scope = rememberCoroutineScope()

    val maxColumns = when (windowSizeClass) {
        GenesysWindowSizeClass.EXPANDED -> 4
        GenesysWindowSizeClass.MEDIUM -> 3
        GenesysWindowSizeClass.COMPACT -> 2
    }

    val horizontalItemWidth = when (windowSizeClass) {
        GenesysWindowSizeClass.EXPANDED -> 220.dp
        GenesysWindowSizeClass.MEDIUM -> 180.dp
        GenesysWindowSizeClass.COMPACT -> 150.dp
    }

    val spacing = if (windowSizeClass == GenesysWindowSizeClass.COMPACT) 8.dp else 16.dp
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    Box(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {

        if (isHorizontal) {
            val listState = rememberLazyListState()
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    contentPadding = PaddingValues(horizontal = if (isCompact) 0.dp else 48.dp, vertical = 8.dp),
                ) {
                    itemsIndexed(products) { index, product ->
                        ProductCard(
                            product = product,
                            modifier = Modifier.width(horizontalItemWidth),
                            onClick = onProductClick,
                            onAddToCart = { onAddToCart?.invoke(product) },
                            onHover = onHover,
                            isEditMode = isEditMode,
                            index = index,
                        )
                    }
                }

                if (!isCompact && products.size > 1) {
                    Surface(
                        modifier = Modifier.align(Alignment.CenterStart).size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        tonalElevation = 4.dp,
                    ) {
                        GenesysIconButton(
                            icon = GenesysIcons.ArrowLeft,
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(
                                        (listState.firstVisibleItemIndex - 1).coerceAtLeast(0),
                                    )
                                }
                            },
                        )
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.CenterEnd).size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        tonalElevation = 4.dp,
                    ) {
                        GenesysIconButton(
                            icon = GenesysIcons.ArrowRight,
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(
                                        (listState.firstVisibleItemIndex + 1).coerceAtMost(products.size - 1),
                                    )
                                }
                            },
                        )
                    }
                }
            }
        } else {
            Column {
                products.chunked(maxColumns).forEachIndexed { rowIndex, rowProducts ->
                    GenesysRow(horizontalArrangement = Arrangement.spacedBy(spacing), usePadding = false) {
                        rowProducts.forEachIndexed { colIndex, product ->
                            val overallIndex = rowIndex * maxColumns + colIndex
                            GenesysWeightBox(1f) {
                                ProductCard(
                                    product = product,
                                    onClick = onProductClick,
                                    onAddToCart = { onAddToCart?.invoke(product) },
                                    onHover = onHover,
                                    isEditMode = isEditMode,
                                    index = overallIndex,
                                )
                            }
                        }
                        if (rowProducts.size < maxColumns) {
                            val rowScope = this
                            repeat(maxColumns - rowProducts.size) {
                                rowScope.GenesysWeightSpacer(1f)
                            }
                        }
                    }
                    GenesysSpacer(if (isCompact) GenesysSpacing.Small else GenesysSpacing.Medium)
                }
            }
        }
    }
}
