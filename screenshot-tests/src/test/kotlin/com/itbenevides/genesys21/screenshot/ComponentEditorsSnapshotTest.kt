package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.screens.editor.ButtonComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.HeaderComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.ProductListComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.ProfileHeaderComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.SocialLinksComponentEditor
import com.itbenevides.genesys21.presentation.screens.editor.TextComponentEditor
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import org.junit.Rule
import org.junit.Test

class ComponentEditorsSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun testHeaderComponentEditor() {
        paparazzi.genesysSnapshot {
            HeaderComponentEditor(
                component = PageComponent.Header(title = "Meu Título"),
                onSave = {}
            )
        }
    }

    @Test
    fun testTextComponentEditor() {
        paparazzi.genesysSnapshot {
            TextComponentEditor(
                component = PageComponent.Text(content = "Este é um texto de exemplo para o editor."),
                onSave = {}
            )
        }
    }

    @Test
    fun testProfileHeaderComponentEditor() {
        paparazzi.genesysSnapshot {
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
    fun testSocialLinksComponentEditor() {
        paparazzi.genesysSnapshot {
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
    fun testProductListComponentEditor() {
        val sampleProducts = listOf(
            Product(id = "1", storeId = "s1", name = "Produto 1", price = 10.0, imageUrls = emptyList()),
            Product(id = "2", storeId = "s1", name = "Produto 2", price = 20.0, imageUrls = emptyList())
        )
        paparazzi.genesysSnapshot {
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
    fun testButtonComponentEditor() {
        paparazzi.genesysSnapshot {
            ButtonComponentEditor(
                component = PageComponent.Button(text = "Clique Aqui", url = "https://example.com"),
                onSave = {}
            )
        }
    }
}
