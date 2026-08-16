package com.itbenevides.genesys21.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.viewer.CartContent
import com.itbenevides.genesys21.presentation.screens.viewer.CartScreenState
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsContent
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsState
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import org.junit.Rule
import org.junit.Test

class ResponsivenessSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    private val sampleProduct =
        Product(
            id = "1",
            storeId = "store-1",
            name = "SmartWatch Genesys Pro",
            price = 899.90,
            imageUrls = listOf("https://images.unsplash.com/photo-1544117518-30dd5f2f309e?q=80&w=800"),
            description = "High-performance smartwatch with health tracking and elegant design.",
            stock = 15,
        )

    @Test
    fun testProductDetailsResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ProductDetailsContent(
                state = ProductDetailsState(product = sampleProduct),
                backendUrl = "",
                onEvent = {},
            )
        }
    }

    @Test
    fun testCartResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            CartContent(
                state = CartScreenState(total = 899.90),
                store = null,
                backendUrl = "",
                stripeAppearance = "",
                onEvent = {},
            )
        }
    }

    @Test
    fun testEmbeddedCheckoutResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            val appTheme = com.itbenevides.genesys21.domain.model.PageThemeConfig.ELEGANCE
            val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
            val appearance = com.itbenevides.genesys21.util.StripeThemeMapper.mapToAppearance(appTheme, colorScheme)

            CartContent(
                state = CartScreenState(
                    total = 899.90,
                    stripeClientSecret = "pi_test_secret"
                ),
                store = null,
                backendUrl = "",
                stripeAppearance = appearance,
                onEvent = {},
            )
        }
    }

    @Test
    fun testTypographyScaling() {
        paparazzi.genesysResponsiveSnapshot {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText(
                    text = "Headline Responsive",
                    style = com.itbenevides.genesys21.ui.theme.GenesysTextStyle.Headline,
                    fontWeight = com.itbenevides.genesys21.ui.theme.GenesysFontWeight.ExtraBold
                )
                com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText(
                    text = "Body standard remains consistent.",
                    style = com.itbenevides.genesys21.ui.theme.GenesysTextStyle.Body
                )
            }
        }
    }
}
