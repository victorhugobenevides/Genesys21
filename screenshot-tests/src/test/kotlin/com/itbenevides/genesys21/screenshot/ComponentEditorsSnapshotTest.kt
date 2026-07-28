package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.editor.ButtonComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.HeaderComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.ProductListComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.ProfileHeaderComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.SocialLinksComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.TextComponentEditor
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import org.junit.Rule
import org.junit.Test

class ComponentEditorsSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testHeaderComponentEditorResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            HeaderComponentEditor(
                component = PageComponent.Header(title = "Meu Título"),
                onSave = {}
            )
        }
    }

    @Test
    fun testTextComponentEditorResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            TextComponentEditor(
                component = PageComponent.Text(content = "Este é um texto de exemplo para o editor."),
                onSave = {}
            )
        }
    }

    @Test
    fun testProfileHeaderComponentEditorResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ProfileHeaderComponentEditor(
                component = PageComponent.ProfileHeader(
                    imageUrl = "https://github.com/victorhugobenevides.png",
                    name = "Victor Hugo",
                    bio = "Desenvolvedor Especialista"
                ),
                onSave = {},
                onPickImage = {}
            )
        }
    }

    @Test
    fun testSocialLinksComponentEditorResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            SocialLinksComponentEditor(
                component = PageComponent.SocialLinks(
                    email = "teste@exemplo.com",
                    whatsapp = "11999999999"
                ),
                onSave = {}
            )
        }
    }

    @Test
    fun testProductListComponentEditorResponsive() {
        val sampleProducts = listOf(
            Product(id = "1", storeId = "s1", name = "Produto 1", price = 10.0, imageUrls = emptyList()),
            Product(id = "2", storeId = "s1", name = "Produto 2", price = 20.0, imageUrls = emptyList())
        )
        paparazzi.genesysResponsiveSnapshot {
            ProductListComponentEditor(
                component = PageComponent.ProductList(products = sampleProducts.take(1)),
                allAvailableProducts = sampleProducts,
                onEditProduct = {},
                onProductsUpdated = {},
                onSaveLabel = { _, _ -> }
            )
        }
    }

    @Test
    fun testButtonComponentEditorResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ButtonComponentEditor(
                component = PageComponent.Button(text = "Clique Aqui", url = "https://example.com"),
                onSave = {}
            )
        }
    }

    @Test
    fun testGridComponentEditorResponsive() {
        val grid = PageComponent.Grid(
            columns = 2,
            items = listOf(
                PageComponent.GridItem(components = listOf(PageComponent.Text("Item 1"))),
                PageComponent.GridItem(components = listOf(PageComponent.Image(url = "https://picsum.photos/100")))
            )
        )
        paparazzi.genesysResponsiveSnapshot {
            com.itbenevides.genesys21.presentation.screens.editor.GridComponentEditor(
                component = grid,
                allPageComponents = listOf(PageComponent.Header("H1"), PageComponent.Button("B1", "#")),
                onSave = {}
            )
        }
    }
}
