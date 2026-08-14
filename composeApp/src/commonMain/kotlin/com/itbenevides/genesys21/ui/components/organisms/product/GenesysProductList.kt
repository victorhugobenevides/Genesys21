package com.itbenevides.genesys21.ui.components.organisms.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.theme.GenesysSpacing
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
        GenesysWindowSizeClass.EXPANDED -> GenesysTheme.spacing.huge * 3.5f // ~220dp
        GenesysWindowSizeClass.MEDIUM -> GenesysTheme.spacing.huge * 2.8f // ~180dp
        GenesysWindowSizeClass.COMPACT -> GenesysTheme.spacing.huge * 2.3f // ~150dp
    }

    val spacing = if (windowSizeClass == GenesysWindowSizeClass.COMPACT) GenesysTheme.spacing.xs else GenesysTheme.spacing.m
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    Box(modifier = modifier.fillMaxWidth().padding(vertical = GenesysTheme.spacing.s)) {

        if (isHorizontal) {
            val listState = rememberLazyListState()
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    contentPadding = PaddingValues(horizontal = if (isCompact) GenesysTheme.spacing.none else GenesysTheme.spacing.xxl, vertical = GenesysTheme.spacing.xs),
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
                        color = GenesysTheme.colors.surface.copy(alpha = 0.8f),
                        tonalElevation = GenesysTheme.spacing.xxs,
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
                        color = GenesysTheme.colors.surface.copy(alpha = 0.8f),
                        tonalElevation = GenesysTheme.spacing.xxs,
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
                    Spacer(Modifier.height(if (isCompact) GenesysTheme.spacing.xs else GenesysTheme.spacing.m))
                }
            }
        }
    }
}
