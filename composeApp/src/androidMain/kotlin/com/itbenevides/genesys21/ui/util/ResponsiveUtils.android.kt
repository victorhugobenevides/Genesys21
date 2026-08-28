package com.itbenevides.genesys21.ui.util

actual fun isSystemTestPropertyEnabled(): Boolean {
    // Detecta tanto nossa propriedade customizada quanto propriedades padrão do Paparazzi/JUnit
    return System.getProperty("genesys.test_mode") == "true" ||
           System.getProperty("paparazzi.test.resources") != null ||
           System.getProperty("android.screenshot.test") == "true"
}
