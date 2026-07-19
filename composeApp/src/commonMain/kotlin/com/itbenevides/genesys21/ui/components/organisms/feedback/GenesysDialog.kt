package com.itbenevides.genesys21.ui.components.organisms.feedback

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

@Composable
fun GenesysDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    if (isCompact) {
        GenesysBottomSheet(
            onDismiss = onDismissRequest,
            title = title,
            actions = {
                dismissButton?.invoke()
                confirmButton()
            }
        ) {
            GenesysColumn(usePadding = true) {
                content()
                GenesysSpacer(GenesysSpacing.Large)
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { GenesysText(text = title, style = GenesysTextStyle.Title) },
            text = content,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            shape = RoundedCornerShape(GenesysDimens.CornerRadiusHuge),
        )
    }
}
