package com.itbenevides.genesys21.ui.components.molecules.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.CurrencyUtils

@Composable
fun ValuedActionComponent(
    component: PageComponent.ValuedAction,
    onActionClick: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedValue by remember { mutableStateOf<Double?>(component.suggestedValues.firstOrNull()) }
    var customValueText by remember { mutableStateOf("") }
    val isCustomSelected = component.allowCustomValue && selectedValue == null

    GenesysCard(
        modifier = modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GenesysText(
                text = component.title,
                style = GenesysTextStyle.Title,
                fontWeight = GenesysFontWeight.Bold
            )

            component.description?.let {
                GenesysSpacer(GenesysSpacing.Small)
                GenesysText(
                    text = it,
                    style = GenesysTextStyle.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            GenesysSpacer(GenesysSpacing.Medium)

            // Valores sugeridos
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                component.suggestedValues.forEach { value ->
                    FilterChip(
                        selected = selectedValue == value,
                        onClick = {
                            selectedValue = value
                            customValueText = ""
                        },
                        label = { Text("${GenesysStrings.PricePrefix}${CurrencyUtils.formatDisplay(value)}") },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                if (component.allowCustomValue) {
                    FilterChip(
                        selected = isCustomSelected,
                        onClick = { selectedValue = null },
                        label = { Text(GenesysStrings.CustomValue) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            if (isCustomSelected) {
                GenesysSpacer(GenesysSpacing.Medium)
                OutlinedTextField(
                    value = customValueText,
                    onValueChange = { customValueText = it },
                    label = { Text("Valor") },
                    prefix = { Text(GenesysStrings.PricePrefix) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            GenesysSpacer(GenesysSpacing.Large)

            val finalValue = if (isCustomSelected) {
                customValueText.toDoubleOrNull() ?: 0.0
            } else {
                selectedValue ?: 0.0
            }

            GenesysLoadingButton(
                text = "${component.buttonText} (${GenesysStrings.PricePrefix}${CurrencyUtils.formatDisplay(finalValue)})",
                onClick = {
                    if (finalValue > 0) {
                        onActionClick(component.title, finalValue)
                    }
                },
                enabled = finalValue > 0,
                fillWidth = true
            )
        }
    }
}
