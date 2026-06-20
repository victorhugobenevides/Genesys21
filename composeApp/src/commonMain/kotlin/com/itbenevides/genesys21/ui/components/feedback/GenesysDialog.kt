package com.itbenevides.genesys21.ui.components.feedback

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.theme.GenesysDimens

@Composable
fun GenesysDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { GenesysText(text = title, style = GenesysTextStyle.Title) },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = RoundedCornerShape(GenesysDimens.CornerRadiusHuge),
    )
}
