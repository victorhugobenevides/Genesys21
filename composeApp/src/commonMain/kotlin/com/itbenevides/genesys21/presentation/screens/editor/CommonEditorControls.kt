package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.input.GenesysSlider
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.components.image.GenesysColorCircle

/**
 * Controles centralizados para formatação de texto.
 */
@Composable
fun TextFormatControls(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    textAlign: String,
    onTextAlignChange: (String) -> Unit,
    fontWeight: String,
    onFontWeightChange: (String) -> Unit,
    isUppercase: Boolean,
    onUppercaseChange: (Boolean) -> Unit,
    fontSizeRange: ClosedFloatingPointRange<Float> = 12f..80f
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    GenesysColumn(usePadding = false) {
        GenesysRow(modifier = Modifier.fillMaxWidth()) {
            // Alinhamento
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.Alignment, style = GenesysTextStyle.Label)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("LEFT", "CENTER", "RIGHT").forEach { align ->
                            val isSelected = textAlign == align
                            GenesysIconButton(
                                icon = when(align) {
                                    "LEFT" -> GenesysIcons.AlignLeft
                                    "RIGHT" -> GenesysIcons.AlignRight
                                    else -> GenesysIcons.AlignCenter
                                },
                                onClick = { onTextAlignChange(align) },
                                tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
            
            // Peso e Caps
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.FontStyle, style = GenesysTextStyle.Label)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysIconButton(
                            icon = GenesysIcons.Bold,
                            onClick = { onFontWeightChange(if (fontWeight == "BOLD") "NORMAL" else "BOLD") },
                            tint = if (fontWeight == "BOLD") primaryColor else MaterialTheme.colorScheme.outline
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onUppercaseChange(!isUppercase) }) {
                            Checkbox(checked = isUppercase, onCheckedChange = onUppercaseChange)
                            GenesysText("Caps", style = GenesysTextStyle.Label)
                        }
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysSlider(
            value = fontSize, 
            onValueChange = onFontSizeChange, 
            label = "${GenesysStrings.FontSize}: ${fontSize.toInt()}px", 
            valueRange = fontSizeRange
        )
    }
}

/**
 * Controles centralizados para edição de imagem/mídia.
 */
@Composable
fun ImageEditControls(
    imageUrl: String,
    isUploading: Boolean,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    GenesysColumn(usePadding = false, modifier = modifier) {
        GenesysText(text = "Mídia", style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isUploading) { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = GenesysIcons.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    GenesysSpacer(GenesysSpacing.Small)
                    GenesysText("Toque para enviar foto", style = GenesysTextStyle.Label)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GenesysText("Imagem Selecionada", style = GenesysTextStyle.Label)
                }
                
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = GenesysIcons.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            if (isUploading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

/**
 * Controle centralizado para mudança de cor de fundo com Paleta Inteligente.
 */
@Composable
fun BackgroundColorEditControls(
    backgroundColor: String?,
    onColorChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentScheme = MaterialTheme.colorScheme
    
    // Paleta inteligente baseada no tema atual + tons neutros
    val smartPresets = remember(currentScheme) {
        listOf(
            "#00000000", // Transparente
            currentScheme.primary.toHexStr(),
            currentScheme.secondary.toHexStr(),
            currentScheme.tertiary.toHexStr(),
            currentScheme.surfaceVariant.toHexStr(),
            currentScheme.background.toHexStr(),
            "#FFFFFF", "#F5F5F5", "#E0E0E0", "#212121", "#000000",
            "#F44336", "#FFEB3B", "#4CAF50", "#2196F3" // Cores vibrantes básicas
        ).distinct()
    }

    var hexValue by remember(backgroundColor) { mutableStateOf(backgroundColor ?: "") }

    GenesysColumn(usePadding = false, modifier = modifier) {
        GenesysText(text = "Cor de Fundo", style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        
        // Paleta de cores horizontal com scroll
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(smartPresets) { colorHex ->
                val isSelected = hexValue.equals(colorHex, ignoreCase = true)
                val color = if (colorHex == "#00000000") Color.Transparent else Color(colorHex.toColorInt())
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (color == Color.Transparent) Color.LightGray.copy(alpha = 0.2f) else color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) currentScheme.primary else currentScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .clickable {
                            hexValue = colorHex
                            onColorChange(if (colorHex == "#00000000") null else colorHex)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (colorHex == "#00000000") {
                        Icon(GenesysIcons.Close, null, modifier = Modifier.size(18.dp), tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Small)
        
        // Campo manual
        GenesysTextField(
            value = hexValue,
            onValueChange = { newValue -> 
                hexValue = newValue
                if (newValue.isBlank()) {
                    onColorChange(null)
                } else if (newValue.startsWith("#") && (newValue.length == 7 || newValue.length == 9)) {
                    onColorChange(newValue)
                }
            },
            placeholder = "#RRGGBB",
            label = "Código Hexadecimal",
            icon = GenesysIcons.Palette
        )
    }
}

// Extensão manual para converter Color em Hex String (Evitando String.format que quebra no Wasm)
private fun Color.toHexStr(): String {
    val a = (alpha * 255).toInt()
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return "#" + 
           a.toString(16).padStart(2, '0') +
           r.toString(16).padStart(2, '0') +
           g.toString(16).padStart(2, '0') +
           b.toString(16).padStart(2, '0')
}

private fun String.toColorInt(): Int {
    if (this == "#00000000") return 0
    var color = this.substring(1)
    if (color.length == 6) color = "FF$color"
    return color.toLong(16).toInt()
}
