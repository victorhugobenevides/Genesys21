package com.itbenevides.genesys21.screenshot.base

import app.cash.paparazzi.DeviceConfig

/**
 * Configurações de dispositivos padronizadas para testes.
 */
object DeviceConfigs {
    // Phones
    val SMALL_PHONE = DeviceConfig.PIXEL_4A
    val STANDARD_PHONE = DeviceConfig.PIXEL_5
    val LARGE_PHONE = DeviceConfig.PIXEL_6_PRO
    
    // Tablets
    val SMALL_TABLET = DeviceConfig.NEXUS_7
    val LARGE_TABLET = DeviceConfig.NEXUS_10
    
    /**
     * Lista de dispositivos para testes multi-resolução completos.
     */
    val ALL_DEVICES = listOf(
        SMALL_PHONE,
        STANDARD_PHONE,
        LARGE_PHONE,
        SMALL_TABLET,
        LARGE_TABLET
    )
    
    /**
     * Lista de dispositivos para testes rápidos (skip tablets).
     */
    val PHONE_DEVICES = listOf(
        SMALL_PHONE,
        STANDARD_PHONE,
        LARGE_PHONE
    )
}
