package com.genesys.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.genesys.ui.theme.Dimensions
import kotlin.math.pow

object AccessibilityHelper {
    val MINIMUM_TOUCH_TARGET: Dp = Dimensions.touch_target_min
    
    fun checkColorContrast(foreground: Int, background: Int): Boolean {
        val ratio = calculateContrastRatio(foreground, background)
        return ratio >= 4.5
    }
    
    fun calculateContrastRatio(color1: Int, color2: Int): Double {
        val lum1 = calculateRelativeLuminance(color1)
        val lum2 = calculateRelativeLuminance(color2)
        
        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)
        
        return (lighter + 0.05) / (darker + 0.05)
    }
    
    private fun calculateRelativeLuminance(color: Int): Double {
        val r = ((color shr 16) and 0xFF) / 255.0
        val g = ((color shr 8) and 0xFF) / 255.0
        val b = (color and 0xFF) / 255.0
        
        fun adjust(value: Double): Double {
            return if (value <= 0.03928) {
                value / 12.92
            } else {
                ((value + 0.055) / 1.055).pow(2.4)
            }
        }
        
        return 0.2126 * adjust(r) + 0.7152 * adjust(g) + 0.0722 * adjust(b)
    }
}

fun Modifier.accessibilityLabel(label: String): Modifier {
    return this.semantics {
        contentDescription = label
    }
}