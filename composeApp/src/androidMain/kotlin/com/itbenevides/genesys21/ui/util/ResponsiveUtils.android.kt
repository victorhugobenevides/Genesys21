package com.itbenevides.genesys21.ui.util

actual fun isSystemTestPropertyEnabled(): Boolean {
    return System.getProperty("genesys.test_mode") == "true"
}
