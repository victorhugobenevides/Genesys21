package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.GenesysTextStyle

@Composable
fun DividerComponentEditor(
    component: PageComponent.Divider,
    onSave: (PageComponent.Divider) -> Unit,
) {
    var usePadding by remember(component) { mutableStateOf(component.usePadding) }

    LaunchedEffect(usePadding) {
        onSave(component.copy(usePadding = usePadding))
    }

    GenesysColumn(usePadding = false) {
        GenesysRow(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            GenesysText("Usar Margens Laterais?", style = GenesysTextStyle.Body, modifier = androidx.compose.ui.Modifier.weight(1f))
            Switch(checked = usePadding, onCheckedChange = { usePadding = it })
        }
    }
}
