package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import org.junit.Rule
import org.junit.Test

class GridSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testComplexNestedGrid() {
        val nestedGrid = PageComponent.Grid(
            columns = 2,
            items = listOf(
                PageComponent.GridItem(components = listOf(PageComponent.Text("Nested 1"))),
                PageComponent.GridItem(components = listOf(PageComponent.Text("Nested 2")))
            )
        )

        val mainGrid = PageComponent.Grid(
            columns = 2,
            title = "Grade Principal com Aninhamento",
            items = listOf(
                PageComponent.GridItem(components = listOf(PageComponent.Header("Coluna A"))),
                PageComponent.GridItem(components = listOf(nestedGrid)),
                PageComponent.GridItem(components = listOf(PageComponent.Button("Botão na Grade", "#"))),
                PageComponent.GridItem(components = listOf(PageComponent.Image(url = "https://picsum.photos/200")))
            )
        )

        genesysResponsiveSnapshot(paparazzi) {
            PageComponentRenderer(
                component = mainGrid,
                storeId = "store-1",
                onProductClick = {},
                onServiceClick = {}
            )
        }
    }
}
