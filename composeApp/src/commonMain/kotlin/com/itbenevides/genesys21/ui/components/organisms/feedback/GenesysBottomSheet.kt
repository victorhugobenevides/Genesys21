package com.itbenevides.genesys21.ui.components.organisms.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.theme.GenesysDimens

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
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = GenesysDimens.SpacingLarge)
                    .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Row { actions() }
            }
            Spacer(modifier = Modifier.height(GenesysDimens.SpacingMedium))
            content()
        }
    }
}
