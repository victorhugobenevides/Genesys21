package com.itbenevides.genesys21.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else null
}
