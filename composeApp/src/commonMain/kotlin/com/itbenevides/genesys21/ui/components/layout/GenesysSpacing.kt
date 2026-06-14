package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.ui.unit.Dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Escala de espaçamento semântico para o Design System.
 */
enum class GenesysSpacing(val value: Dp) {
    Small(GenesysDimens.SpacingSmall),
    Medium(GenesysDimens.SpacingMedium),
    Large(GenesysDimens.SpacingLarge),
    ExtraLarge(GenesysDimens.SpacingExtraLarge),
    Huge(GenesysDimens.SpacingHuge),
}
