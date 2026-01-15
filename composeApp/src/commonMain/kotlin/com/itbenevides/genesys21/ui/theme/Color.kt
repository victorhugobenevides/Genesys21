package com.itbenevides.genesys21.ui.theme

import androidx.compose.ui.graphics.Color

// iOS-inspired "Comfort" Palette (Deep Slate & Charcoal)
val iOSSlate = Color(0xFF1C1C1E) // Cor de label primária no iOS (confortável)
val iOSIndigo = Color(0xFF5856D6)
val iOSGray = Color(0xFF8E8E93)
val iOSBackground = Color(0xFFF2F2F7)
val iOSSurface = Color(0xFFFFFFFF)
val iOSSeparator = Color(0xFFC6C6C8).copy(alpha = 0.4f)

val Primary = iOSSlate // Mudando para um tom grafite mais confortável que o azul/indigo vibrante
val OnPrimary = Color.White
val Background = iOSBackground
val Surface = iOSSurface
val OnSurface = iOSSlate
val OnSurfaceVariant = iOSGray
val Border = iOSSeparator
val Error = Color(0xFFFF3B30)
val Success = Color(0xFF34C759)
