package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.screens.viewer.ComponentCatalogUI
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelState
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import org.junit.Rule
import org.junit.Test

class WhiteLabelInteractionSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testComponentCatalogVisibility() {
        val samplePage = Page(id = "test", storeId = "store-1", title = "Editor Test")
        val state = WhiteLabelState(
            page = samplePage,
            showCatalog = true
        )

        paparazzi.genesysSnapshot {
            // Testamos o componente do catálogo diretamente.
            // Se o código do catálogo sumir ou quebrar, este teste falhará no CI.
            ComponentCatalogUI(
                state = state,
                onEvent = {}
            )
        }
    }
}
