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
            // 1. Determine Admin ID from Environment
            val adminEmail = System.getenv("OWNER_EMAIL") ?: "victorkoto@gmail.com"
            val existingAdmin = UsersTable.selectAll().where { UsersTable.email eq adminEmail }.firstOrNull()
            val adminId = existingAdmin?.get(UsersTable.id) ?: "mKQ9MZqG6bYhy3JqvngGpv49ZZs1"

            if (existingAdmin == null) {
                UsersTable.insert {
                    it[id] = adminId
                    it[email] = adminEmail
                    it[name] = "Victor Hugo"
                    it[role] = UserRole.SUPERADMIN.name
                    it[permissions] = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { it.name }
                }
            } else {
                UsersTable.update({ UsersTable.id eq adminId }) {
                    it[role] = UserRole.SUPERADMIN.name
                    it[permissions] = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { it.name }
                }
            }

            // 2. Create/Update Default Store (Linked to Admin UID for instant dashboard access)
            val defaultStoreId = adminId
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

                // Força atualização se as chaves de ambiente forem REAIS (diferentes do default) e o banco estiver com o default
                val isEnvReal = pk != "pk_test_genesys_default" && sk != "sk_test_genesys_default"
                val isDbDefault = currentSk == "sk_test_genesys_default" || currentSk.isNullOrBlank()

                if (isEnvReal && isDbDefault) {
                    println("Seeder: Detectadas chaves Stripe reais no ambiente. Atualizando loja padrão...")
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

            val fofocaId = "serv_fofoca_gratis"
            BookingServicesTable.deleteWhere { id eq fofocaId }
            BookingServicesTable.insert {
                it[id] = fofocaId
                it[storeId] = defaultStoreId
                it[name] = "me conte uma fofoca"
                it[price] = 0.0
                it[durationMinutes] = 15
                it[description] = "Um momento para relaxar e compartilhar histórias."
                it[isEnabled] = true
                it[isOnline] = true
            }

            // 3. Create/Update CV Page
            val cvPageId = "victor-hugo-cv"
            PagesTable.deleteWhere { id eq cvPageId }
            PageComponentsTable.deleteWhere { pageId eq cvPageId }

            val components = listOf(
                PageComponent.ProfileHeader(
                    imageUrl = "https://ui-avatars.com/api/?name=Victor+Hugo&size=300&background=000&color=fff",
                    name = "Victor Hugo",
                    bio = "Engenheiro de Software com foco em ambientes mobile | 14+ Anos de Experiência",
                    imageSize = 160
                ),

                PageComponent.SocialLinks(
                    instagram = "https://www.instagram.com/euvictorben/",
                    whatsapp = "https://wa.me/5511998104606",
                    email = "victorkoto@gmail.com"
                ),

                PageComponent.Divider(),

                PageComponent.Filter(placeholder = "Buscar ferramenta ou tecnologia (ex: Kotlin, PCI)..."),

                PageComponent.Skills(
                    title = "Tech Stack & Expertise",
                    tags = listOf(
                        "Kotlin", "Java", "Swift", "Dart", "SQL",
                        "Jetpack Compose", "Flutter", "SwiftUI", "KMP", "Ktor",
                        "Clean Architecture", "MVI", "MVVM", "SOLID",
                        "PCI Compliance", "Dexguard", "RASP", "Firebase Auth",
                        "CI/CD", "CircleCI", "Docker", "Exposed ORM", "SQLite",
                        "Paparazzi", "Unit Testing", "Analytics",
                        "AI (IA)", "MCP", "Devin", "Copilot", "Android Studio",
                        "Visual Studio", "Windsurf", "Cascade", "SpecKit", "Antigravity"
                    )
                ),

                PageComponent.Divider(),

                PageComponent.Header(title = "Sobre Mim", fontSize = 26, fontWeight = "EXTRA_BOLD"),
                PageComponent.Text(
                    content = "Engenheiro de Software focado no ecossistema mobile com longa trajetória em projetos de alta escala. Atuação profunda em arquitetura sustentável (Clean/MVI), segurança cibernética e performance em ambientes de missão crítica.",
                    fontSize = 17
                ),

                PageComponent.Divider(),

                PageComponent.Header(title = "Experiência Profissional", fontSize = 26, fontWeight = "EXTRA_BOLD"),

                PageComponent.Header(title = "Sensedia (Getnet) | 2024 - Atual", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(content = "Software Engineer - Payments", fontSize = 14),

                PageComponent.Header(title = "Dafiti Group | 2018 - 2024", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(content = "Software Engineer - Mobile", fontSize = 14),

                PageComponent.Header(title = "It Lean | 2018 - 2019", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(content = "Software Engineer - Android", fontSize = 14),

                PageComponent.Header(title = "MáximaTech | 2015 - 2018", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(content = "Android Developer", fontSize = 14),

                PageComponent.Header(title = "Onnet System | 2012 - 2015", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(content = "Android Developer", fontSize = 14),

                PageComponent.Divider(),

                PageComponent.Header(title = "Mentoria & Consultoria", fontSize = 26, fontWeight = "EXTRA_BOLD"),
                PageComponent.SingleService(
                    service = com.itbenevides.genesys21.domain.model.BookingService(
                        id = mentoriaId,
                        storeId = defaultStoreId,
                        name = "Mentoria em Engenharia de Software Mobile",
                        price = 250.0,
                        durationMinutes = 60,
                        description = "Mentoria técnica e estratégica."
                    )
                ),

                PageComponent.Divider(),

                PageComponent.ValuedAction(
                    title = "Pague minha cerveja",
                    description = "Gostou do conteúdo ou do currículo? Contribua com uma breja!",
                    suggestedValues = listOf(10.0, 20.0, 50.0),
                    buttonText = "Pagar Cerveja"
                ),

                PageComponent.Divider(),

                PageComponent.Header(title = "Interação", fontSize = 26, fontWeight = "EXTRA_BOLD"),
                PageComponent.SingleService(
                    service = com.itbenevides.genesys21.domain.model.BookingService(
                        id = fofocaId,
                        storeId = defaultStoreId,
                        name = "me conte uma fofoca",
                        price = 0.0,
                        durationMinutes = 15,
                        description = "Vamos bater um papo descontraído."
                    )
                ),

                PageComponent.Spacer(height = 40),
                PageComponent.Button(text = "📥 Baixar CV Completo (PDF)", url = "print", isPrimary = true)
            )

            PagesTable.insert {
                it[id] = cvPageId
                it[storeId] = defaultStoreId
                it[title] = "Currículo - Victor Hugo"
                it[theme] = PageThemeConfig.MONO.name
                it[whatsapp] = "5511998104606"
                it[createdAt] = System.currentTimeMillis()
                it[updatedAt] = System.currentTimeMillis()
            }

            components.forEachIndexed { index, component ->
                val contentJson = json.encodeToString(component)
                val componentId = java.util.UUID.randomUUID().toString()
                PageComponentsTable.insert {
                    it[id] = componentId
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
                            it[ComponentProductsTable.componentId] = componentId
                            it[productId] = pid
                            it[order] = pIdx
                        }
                    }
                }
            }

            println("Seeder: Initial data and CV Page updated successfully.")
        }
    }
}
