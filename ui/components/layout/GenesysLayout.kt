package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Alinhamentos semânticos para a camada de Presentation.
 */
enum class GenesysAlignment {
    Start, Center, End
}

/**
 * Representa uma página completa do sistema (Scaffold).
 */
@Composable
fun GenesysPage(
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        content = { padding ->
            Box(Modifier.padding(padding)) {
                content()
            }
        }
    )
}

/**
 * Container vertical padronizado com suporte a scroll, largura máxima e peso.
 */
@Composable
fun GenesysColumn(
    modifier: Modifier = Modifier,
    usePadding: Boolean = true,
    useScroll: Boolean = false,
    horizontalAlignment: GenesysAlignment = GenesysAlignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxWidth: Dp? = null,
    weightValue: Float = 0f,
    content: @Composable ColumnScope.() -> Unit
) {
    val alignment = when (horizontalAlignment) {
        GenesysAlignment.Start -> Alignment.Start
        GenesysAlignment.Center -> Alignment.CenterHorizontally
        GenesysAlignment.End -> Alignment.End
    }

    val baseModifier = modifier
        .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier.fillMaxWidth())
        .then(if (usePadding) Modifier.padding(GenesysDimens.SpacingLarge) else Modifier)
        .then(if (useScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)

    // Nota: O peso é aplicado se o componente estiver dentro de um Row ou Column compatível.
    // Como GenesysColumn é usado para layout, delegamos a responsabilidade de escopo para o sistema.
    Column(
        modifier = if (weightValue > 0f) Modifier.weight(weightValue).then(baseModifier) else baseModifier,
        horizontalAlignment = alignment,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/**
 * Container horizontal padronizado com suporte a peso e scroll.
 */
@Composable
fun GenesysRow(
    modifier: Modifier = Modifier,
    fillWidth: Boolean = true,
    usePadding: Boolean = false,
    useHorizontalScroll: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    weightValue: Float = 0f,
    content: @Composable RowScope.() -> Unit
) {
    val baseModifier = if (fillWidth) modifier.fillMaxWidth() else modifier.wrapContentWidth()
    val paddingModifier = if (usePadding) baseModifier.padding(horizontal = GenesysDimens.SpacingLarge) else baseModifier
    val scrollModifier = if (useHorizontalScroll) paddingModifier.horizontalScroll(rememberScrollState()) else paddingModifier
    
    Row(
        modifier = if (weightValue > 0f) Modifier.weight(weightValue).then(scrollModifier) else scrollModifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

@Composable
fun <T> GenesysLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    maxWidth: Dp? = null,
    usePadding: Boolean = true,
    spacing: GenesysSpacing = GenesysSpacing.Medium,
    content: @Composable (T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = modifier
                .fillMaxHeight()
                .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier.fillMaxWidth())
                .then(if (usePadding) Modifier.padding(horizontal = GenesysDimens.SpacingLarge) else Modifier),
            contentPadding = PaddingValues(vertical = GenesysDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(spacing.value)
        ) {
            items(items) { item ->
                content(item)
            }
        }
    }
}

@Composable
fun <T> GenesysLazyColumnIndexed(
    items: List<T>,
    modifier: Modifier = Modifier,
    maxWidth: Dp? = null,
    spacing: GenesysSpacing = GenesysSpacing.Medium,
    content: @Composable (Int, T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = modifier
                .fillMaxHeight()
                .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier.fillMaxWidth())
                .then(if (maxWidth != null) Modifier.padding(horizontal = GenesysDimens.SpacingLarge) else Modifier),
            contentPadding = PaddingValues(vertical = GenesysDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(spacing.value)
        ) {
            itemsIndexed(items) { index, item ->
                content(index, item)
            }
        }
    }
}

@Composable
fun <T> GenesysLazyRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    spacing: GenesysSpacing = GenesysSpacing.Medium,
    content: @Composable (T) -> Unit
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.value),
        contentPadding = PaddingValues(vertical = GenesysDimens.SpacingSmall)
    ) {
        items(items) { item ->
            content(item)
        }
    }
}

@Composable
fun GenesysSpacer(size: GenesysSpacing = GenesysSpacing.Medium) {
    Spacer(modifier = Modifier.size(size.value))
}

@Composable
fun ColumnScope.GenesysWeightSpacer(weightValue: Float) {
    Spacer(modifier = Modifier.weight(weightValue))
}

@Composable
fun RowScope.GenesysWeightSpacer(weightValue: Float) {
    Spacer(modifier = Modifier.weight(weightValue))
}

/**
 * Utilitários de peso sem expor o Modifier nativo para a camada de Presentation.
 */
@Composable
fun RowScope.GenesysWeightBox(weightValue: Float, content: @Composable () -> Unit) {
    Box(Modifier.weight(weightValue)) { content() }
}

@Composable
fun ColumnScope.GenesysWeightBox(weightValue: Float, content: @Composable () -> Unit) {
    Box(Modifier.weight(weightValue)) { content() }
}

enum class GenesysSpacing(val value: Dp) {
    Small(GenesysDimens.SpacingSmall),
    Medium(GenesysDimens.SpacingMedium),
    Large(GenesysDimens.SpacingLarge),
    ExtraLarge(GenesysDimens.SpacingExtraLarge),
    Huge(GenesysDimens.SpacingHuge)
}
