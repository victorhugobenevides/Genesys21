package com.itbenevides.genesys21.ui.components.organisms.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysTheme

/**
 * Modal padronizado (Bottom Sheet) abstraído do Material 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesysBottomSheet(
    onDismiss: () -> Unit,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = GenesysTheme.colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = GenesysDimens.SpacingLarge)
                    .padding(bottom = GenesysTheme.spacing.xl) // Espaço para barra de navegação/segurança
                    .navigationBarsPadding(),
        ) {
            // Header centralizado ou organizado
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = GenesysTheme.spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = GenesysTheme.typography.title,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    actions()
                }
            }

            HorizontalDivider(color = GenesysTheme.colors.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(GenesysDimens.SpacingLarge))

            content()
        }
    }
}
