package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysSlider
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme

@Composable
fun HeaderComponentEditor(
    component: PageComponent.Header,
    onSave: (PageComponent.Header) -> Unit
) {
    var title by remember { mutableStateOf(component.title) }
    var alignment by remember { mutableStateOf(component.textAlign) }
    var fontSize by remember { mutableStateOf(component.fontSize.toFloat()) }
    var isUppercase by remember { mutableStateOf(component.isUppercase) }
    var usePrimaryColor by remember { mutableStateOf(component.usePrimaryColor) }
    
    val primaryColor = MaterialTheme.colorScheme.primary

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        
        GenesysColumn(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = when(alignment) {
                "CENTER" -> GenesysAlignment.Center
                "RIGHT" -> GenesysAlignment.End
                else -> GenesysAlignment.Start
            }
        ) {
            GenesysText(
                text = if (isUppercase) title.uppercase() else title,
                style = GenesysTextStyle.Headline,
                fontWeight = GenesysFontWeight.ExtraBold,
                color = if (usePrimaryColor) primaryColor else Color.Unspecified
            )
        }
        
        GenesysSpacer(GenesysSpacing.Large)
        GenesysTextField(
            value = title, 
            onValueChange = { title = it }, 
            label = GenesysStrings.TitleTextLabel, 
            icon = GenesysIcons.Description
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysText(text = GenesysStrings.Alignment, style = GenesysTextStyle.Label)
        GenesysRow(modifier = Modifier.fillMaxWidth()) {
            listOf("LEFT", "CENTER", "RIGHT").forEach { align ->
                val isSelected = alignment == align
                GenesysIconButton(
                    icon = when(align) {
                        "LEFT" -> GenesysIcons.AlignLeft
                        "RIGHT" -> GenesysIcons.AlignRight
                        else -> GenesysIcons.AlignCenter
                    },
                    onClick = { alignment = align },
                    tint = if (isSelected) primaryColor else Color.Unspecified
                )
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysSlider(
            value = fontSize, 
            onValueChange = { fontSize = it }, 
            label = GenesysStrings.FontSize, 
            valueRange = 18f..48f
        )

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysRow(modifier = Modifier.fillMaxWidth()) {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.UppercaseOption, style = GenesysTextStyle.Body)
                    Switch(checked = isUppercase, onCheckedChange = { isUppercase = it })
                }
            }
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.BrandColorOption, style = GenesysTextStyle.Body)
                    Switch(checked = usePrimaryColor, onCheckedChange = { usePrimaryColor = it })
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = GenesysStrings.UpdateTitle, 
            fillWidth = true,
            onClick = {
                onSave(component.copy(
                    title = title, 
                    textAlign = alignment, 
                    fontSize = fontSize.toInt(),
                    isUppercase = isUppercase, 
                    usePrimaryColor = usePrimaryColor
                ))
            }
        )
    }
}
