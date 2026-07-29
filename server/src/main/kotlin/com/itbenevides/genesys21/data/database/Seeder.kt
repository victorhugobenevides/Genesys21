package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Seeder {
    private val json = Json { ignoreUnknownKeys = true }

    fun seedInitialData() {
        transaction {
            // 1. Determine Admin ID
            val adminEmail = "victorkoto@gmail.com"
            val existingAdmin = UsersTable.selectAll().where { UsersTable.email eq adminEmail }.firstOrNull()
            val adminId = existingAdmin?.get(UsersTable.id) ?: "mKQ9MZqG6bYhy3JqvngGpv49ZZs1"

            if (existingAdmin == null) {
                UsersTable.insert {
                    it[id] = adminId
                    it[email] = adminEmail
                    it[name] = "Victor Hugo"
                    it[role] = UserRole.SUPERADMIN.name
                }
            } else {
                UsersTable.update({ UsersTable.id eq adminId }) {
                    it[role] = UserRole.SUPERADMIN.name
                }
            }

            // 2. Create/Update Default Store
            val defaultStoreId = "genesys-official-store"
            val storeExists = StoresTable.selectAll().where { StoresTable.id eq defaultStoreId }.count() > 0

            val pk = System.getenv("STRIPE_PUBLIC_KEY") ?: "pk_test_genesys_default"
            val sk = System.getenv("STRIPE_SECRET_KEY") ?: "sk_test_genesys_default"

            if (!storeExists) {
                StoresTable.insert {
                    it[id] = defaultStoreId
                    it[ownerId] = adminId
                    it[name] = "Victor Hugo - Tech & Mentorship"
                    it[description] = "Produtos e serviços para desenvolvedores Android."
                    it[originZipCode] = "01310-100"
                    it[originStreet] = "Avenida Paulista"
                    it[originNumber] = "1000"
                    it[originNeighborhood] = "Bela Vista"
                    it[originCity] = "São Paulo"
                    it[originState] = "SP"
                    it[allowPayOnLocation] = true
                    it[allowPayInApp] = true
                    it[allowPickup] = true
                    it[allowDelivery] = true
                    it[stripePublicKey] = pk
                    it[stripeSecretKey] = sk
                    it[paymentGateway] = "STRIPE"
                }
            } else {
                val currentStore = StoresTable.selectAll().where { StoresTable.id eq defaultStoreId }.first()
                val currentSk = currentStore[StoresTable.stripeSecretKey]

                if (currentSk == null || currentSk.length < 50) {
                    StoresTable.update({ StoresTable.id eq defaultStoreId }) {
                        it[stripePublicKey] = pk
                        it[stripeSecretKey] = sk
                        it[paymentGateway] = "STRIPE"
                    }
                }
            }

            // 2.1 Seed Products
            val productData = listOf(
                Triple("prod_ebook_career", "Guia Carreira Android 2025", 47.0),
                Triple("prod_template_kmp", "Template Clean Architecture KMP", 97.0),
                Triple("prod_course_compose", "Jetpack Compose Masterclass", 297.0),
                Triple("prod_checklist_pci", "Checklist de Segurança PCI", 27.0),
                Triple("prod_community_vip", "Comunidade VIP Genesys", 197.0)
            )

            productData.forEach { (pid, pName, pPrice) ->
                ProductsTable.deleteWhere { id eq pid }
                ProductsTable.insert {
                    it[id] = pid
                    it[storeId] = defaultStoreId
                    it[name] = pName
                    it[price] = pPrice
                    it[description] = "Acelere seu desenvolvimento com este conteúdo exclusivo."
                    it[stock] = 999
                }

                val imageId = "img_$pid"
                ProductImagesTable.deleteWhere { id eq imageId }
                ProductImagesTable.insert {
                    it[id] = imageId
                    it[productId] = pid
                    it[imageUrl] = "https://picsum.photos/seed/$pid/400/400"
                    it[order] = 0
                }
            }

            // 2.2 Seed Service
            val mentoriaId = "serv_mentoria_1to1"
            BookingServicesTable.deleteWhere { id eq mentoriaId }
            BookingServicesTable.insert {
                it[id] = mentoriaId
                it[storeId] = defaultStoreId
                it[name] = "Mentoria em Engenharia de Software Mobile"
                it[price] = 250.0
                it[durationMinutes] = 60
                it[description] = "Mentoria técnica e estratégica focada em desenvolvimento e arquitetura mobile."
                it[isEnabled] = true
                it[isOnline] = true
                it[meetingLink] = "https://meet.google.com/abc-defg-hij"
            }

            val consultoriaId = "serv_consultoria_presencial"
            BookingServicesTable.deleteWhere { id eq consultoriaId }
            BookingServicesTable.insert {
                it[id] = consultoriaId
                it[storeId] = defaultStoreId
                it[name] = "Consultoria Técnica Presencial"
                it[price] = 500.0
                it[durationMinutes] = 120
                it[description] = "Consultoria hands-on no seu escritório."
                it[isEnabled] = true
                it[isHomeService] = true
            }

            // 3. Create/Update CV Page
            val cvPageId = "victor-hugo-cv"
            PagesTable.deleteWhere { id eq cvPageId }
            PageComponentsTable.deleteWhere { pageId eq cvPageId }

            val components = listOf(
                PageComponent.ProfileHeader(
                    imageUrl = "https://ui-avatars.com/api/?name=Victor+Hugo&size=300&background=000&color=fff",
                    name = "Victor Hugo",
                    bio = "Engenheiro de Software Mobile | Especialista em Arquitetura & Segurança",
                    imageSize = 140
                ),

                PageComponent.SocialLinks(
                    instagram = "https://www.instagram.com/euvictorben/",
                    whatsapp = "https://wa.me/5511998104606",
                    email = "victorkoto@gmail.com"
                ),

                PageComponent.Hero(
                    title = "Transformando Ideias em Experiências Mobile de Alta Escala",
                    subtitle = "Especialista em ecossistemas Android e iOS com foco em Clean Architecture, MVI e Segurança Bancária.",
                    imageUrl = "https://picsum.photos/seed/mobile-dev/1200/600",
                    height = 300
                ),

                PageComponent.Benefits(
                    title = "Pilares de Atuação",
                    items = listOf(
                        PageComponent.BenefitItem("Arquitetura Sustentável", "Foco em Clean Architecture, MVI e Modularização para projetos escaláveis.", "Magic"),
                        PageComponent.BenefitItem("Segurança & PCI", "Implementação de RASP, Dexguard e conformidade com padrões de pagamentos.", "Check"),
                        PageComponent.BenefitItem("Performance Crítica", "Otimização de tempo de inicialização, renderização e consumo de memória.", "Inventory")
                    )
                ),

                PageComponent.Divider(),

                PageComponent.Header(title = "Experiência Profissional", fontSize = 28, fontWeight = "EXTRA_BOLD"),

                PageComponent.Grid(
                    columns = 2,
                    items = listOf(
                        PageComponent.GridItem(listOf(
                            PageComponent.Header(title = "Sensedia (Getnet)", fontSize = 20, usePrimaryColor = true),
                            PageComponent.Text("Software Engineer - Payments (2024 - Atualmente)", fontSize = 14, fontWeight = "BOLD"),
                            PageComponent.Text("Atuação no core de pagamentos mobile, garantindo estabilidade e segurança em transações de alto volume.")
                        )),
                        PageComponent.GridItem(listOf(
                            PageComponent.Header(title = "Dafiti Group", fontSize = 20, usePrimaryColor = true),
                            PageComponent.Text("Software Engineer - Mobile (2018 - 2024)", fontSize = 14, fontWeight = "BOLD"),
                            PageComponent.Text("Liderança técnica em features de checkout e catálogo para os apps Android e iOS do maior e-commerce de moda da AL.")
                        )),
                        PageComponent.GridItem(listOf(
                            PageComponent.Header(title = "It Lean", fontSize = 20, usePrimaryColor = true),
                            PageComponent.Text("Software Engineer (2018 - 2019)", fontSize = 14, fontWeight = "BOLD"),
                            PageComponent.Text("Desenvolvimento de soluções enterprise focadas em agilidade e performance.")
                        )),
                        PageComponent.GridItem(listOf(
                            PageComponent.Header(title = "MáximaTech", fontSize = 20, usePrimaryColor = true),
                            PageComponent.Text("Android Developer (2015 - 2018)", fontSize = 14, fontWeight = "BOLD"),
                            PageComponent.Text("Criação de aplicativos robustos para força de vendas e logística.")
                        ))
                    )
                ),

                PageComponent.Divider(),

                PageComponent.Header(title = "Tech Stack & Ferramentas", fontSize = 28, fontWeight = "EXTRA_BOLD"),
                PageComponent.Skills(
                    tags = listOf(
                        "Kotlin", "Swift", "Dart", "Jetpack Compose", "SwiftUI", "Flutter",
                        "KMP", "Ktor", "Clean Architecture", "MVI", "MVVM", "SOLID",
                        "PCI Compliance", "Dexguard", "RASP", "CircleCI", "Docker", "SQLite",
                        "Exposed", "Paparazzi", "Unit Testing", "AI/IA", "Windsurf", "Android Studio"
                    )
                ),

                PageComponent.Divider(),

                PageComponent.Header(title = "Mentoria & Consultoria", fontSize = 28, fontWeight = "EXTRA_BOLD"),
                PageComponent.ServiceList(
                    title = "Agende uma sessão técnica",
                    services = listOf(
                        com.itbenevides.genesys21.domain.model.BookingService(
                            id = mentoriaId,
                            storeId = defaultStoreId,
                            name = "Mentoria 1-to-1",
                            price = 250.0,
                            durationMinutes = 60,
                            description = "Engenharia de Software e Carreira Mobile."
                        ),
                        com.itbenevides.genesys21.domain.model.BookingService(
                            id = consultoriaId,
                            storeId = defaultStoreId,
                            name = "Consultoria Enterprise",
                            price = 500.0,
                            durationMinutes = 120,
                            description = "Arquitetura e Segurança de Sistemas."
                        )
                    )
                ),

                PageComponent.Spacer(height = 40),
                PageComponent.Button(text = "📥 Baixar Currículo Completo (PDF)", url = "print", isPrimary = true)
            )

            PagesTable.insert {
                it[id] = cvPageId
                it[storeId] = defaultStoreId
                it[title] = "Currículo - Victor Hugo"
                it[theme] = PageThemeConfig.MINIMAL.name
                it[whatsapp] = "5511998104606"
                it[createdAt] = System.currentTimeMillis()
                it[updatedAt] = System.currentTimeMillis()
            }

            components.forEachIndexed { index, component ->
                val contentJson = json.encodeToString(component)
                val compId = PageComponentsTable.insertAndGetId {
                    it[pageId] = cvPageId
                    it[type] = component::class.simpleName ?: "Unknown"
                    it[customLabel] = component.customLabel
                    it[isFilterable] = component.isFilterable
                    it[order] = index
                    it[content] = contentJson
                }

                if (component is PageComponent.ProductList) {
                    productData.forEachIndexed { pIdx, (pid, _, _) ->
                        ComponentProductsTable.insert {
                            it[componentId] = compId
                            it[productId] = pid
                            it[order] = pIdx
                        }
                    }
                }
            }

            println("Seeder: Professional CV Page updated with Grid and modern components.")
        }
    }
}
