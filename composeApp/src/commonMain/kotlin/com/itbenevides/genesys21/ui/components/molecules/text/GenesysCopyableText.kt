package com.itbenevides.genesys21.ui.components.molecules.text

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.GenesysTextStyle
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysCopyableText(
    text: String,
    label: String? = null,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = modifier) {
        if (label != null) {
            GenesysText(text = label, style = GenesysTextStyle.Label)
            Spacer(modifier = Modifier.height(4.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenesysText(
                text = text,
                style = style,
                isSelectable = true,
                modifier = Modifier.weight(1f, fill = false)
            )

            GenesysIconButton(
                icon = GenesysIcons.Copy,
                onClick = {
                    clipboardManager.setText(AnnotatedString(text))
                },
                tint = GenesysTheme.colors.brand,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
