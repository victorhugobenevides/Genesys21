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
import androidx.compose.material3.MaterialTheme

@Composable
fun TextComponentEditor(
    component: PageComponent.Text,
    onSave: (PageComponent.Text) -> Unit
) {
    var content by remember { mutableStateOf(component.content) }
    var alignment by remember { mutableStateOf(component.textAlign) }
    var fontSize by remember { mutableStateOf(component.fontSize.toFloat()) }
    var weight by remember { mutableStateOf(component.fontWeight) }
    
    val primaryColor = MaterialTheme.colorScheme.primary

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        
        GenesysText(
            text = content,
            style = GenesysTextStyle.Body,
            fontWeight = when(weight) {
                "BOLD" -> GenesysFontWeight.Bold
                "EXTRA_BOLD" -> GenesysFontWeight.ExtraBold
                else -> GenesysFontWeight.Normal
            },
            textAlign = when(alignment) {
                "CENTER" -> GenesysTextAlign.Center
                "RIGHT" -> GenesysTextAlign.End
                else -> GenesysTextAlign.Start
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        GenesysSpacer(GenesysSpacing.Large)
        GenesysTextField(
            value = content, 
            onValueChange = { content = it }, 
            label = GenesysStrings.ContentTextLabel, 
            singleLine = false, 
            minLines = 3,
            icon = GenesysIcons.Description
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysRow(modifier = Modifier.fillMaxWidth()) {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.Alignment, style = GenesysTextStyle.Label)
                    Row {
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
                }
            }
            
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.FontStyle, style = GenesysTextStyle.Label)
                    Row {
                        GenesysIconButton(
                            icon = GenesysIcons.Bold,
                            onClick = { weight = if (weight == "BOLD") "NORMAL" else "BOLD" },
                            tint = if (weight == "BOLD") primaryColor else Color.Unspecified
                        )
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysSlider(
            value = fontSize, 
            onValueChange = { fontSize = it }, 
            label = GenesysStrings.FontSize, 
            valueRange = 12f..24f
        )

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = GenesysStrings.SaveText, 
            fillWidth = true,
            onClick = {
                onSave(component.copy(
                    content = content, 
                    textAlign = alignment, 
                    fontSize = fontSize.toInt(),
                    fontWeight = weight
                ))
            }
        )
    }
}
