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
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysOutlinedTextField
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.CurrencyUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ValuedActionComponent(
    component: PageComponent.ValuedAction,
    onActionClick: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedValue by remember { mutableStateOf<Double?>(if (component.suggestedValues.isNotEmpty()) component.suggestedValues.first() else null) }
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
                GenesysSpacer(GenesysTheme.spacing.s)
                GenesysText(
                    text = it,
                    style = GenesysTextStyle.Body,
                    color = GenesysTheme.colors.onSurfaceVariant
                )
            }

            GenesysSpacer(GenesysTheme.spacing.m)

            // Valores sugeridos
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.xs)
            ) {
                for (value in component.suggestedValues) {
                    GenesysFilterChip(
                        selected = selectedValue == value,
                        onClick = {
                            selectedValue = value
                            customValueText = ""
                        },
                        label = "${GenesysStrings.PricePrefix}${CurrencyUtils.formatDisplay(value)}",
                        modifier = Modifier.padding(horizontal = GenesysTheme.spacing.xxs)
                    )
                }

                if (component.allowCustomValue) {
                    GenesysFilterChip(
                        selected = isCustomSelected,
                        onClick = { selectedValue = null },
                        label = GenesysStrings.CustomValue,
                        modifier = Modifier.padding(horizontal = GenesysTheme.spacing.xxs)
                    )
                }
            }

            if (isCustomSelected) {
                GenesysSpacer(GenesysTheme.spacing.m)
                GenesysOutlinedTextField(
                    value = customValueText,
                    onValueChange = { customValueText = it },
                    label = "Valor",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            GenesysSpacer(GenesysTheme.spacing.l)

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
