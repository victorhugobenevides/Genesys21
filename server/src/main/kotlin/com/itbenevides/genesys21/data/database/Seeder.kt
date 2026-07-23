package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.UserRole
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Seeder {
    private val json = Json { ignoreUnknownKeys = true }

    fun seedInitialData() {
        transaction {
            // 1. Determine Admin ID (use existing user with email if present)
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
            val storeId = "genesys-official-store"
            val storeExists = StoresTable.selectAll().where { StoresTable.id eq storeId }.count() > 0
            if (!storeExists) {
                StoresTable.insert {
                    it[id] = storeId
                    it[ownerId] = adminId
                    it[name] = "Genesys Store"
                    it[description] = "The official Genesys21 store."
                }
            } else {
                StoresTable.update({ StoresTable.id eq storeId }) {
                    it[ownerId] = adminId
                }
            }

            // 3. Create/Update CV Page (Force Refresh to apply improvements)
            val cvPageId = "victor-hugo-cv"

            // Deletamos a versão antiga para garantir que a nova estrutura (links no final) seja aplicada
            PagesTable.deleteWhere { id eq cvPageId }
            PageComponentsTable.deleteWhere { pageId eq cvPageId }

            val components = listOf(
                PageComponent.Image(
                    url = "https://ui-avatars.com/api/?name=Victor+Hugo&size=200&background=6200ee&color=fff",
                    size = 140,
                    isCircular = true
                ),
                PageComponent.Header(title = "Victor Hugo", textAlign = "CENTER", fontSize = 28, fontWeight = "EXTRA_BOLD"),
                PageComponent.Text(content = "Especialista Android | 14+ Anos de Experiência Mobile", textAlign = "CENTER", fontSize = 16),

                PageComponent.Text(content = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", textAlign = "CENTER", usePrimaryColor = true),

                PageComponent.Header(title = "Sobre Mim", fontSize = 24),
                PageComponent.Text(
                    content = "Especialista em desenvolvimento Android Nativo com 14 anos de trajetória. Atuei em projetos de alta complexidade para grandes players do mercado, desenvolvendo apps que escalam para milhões de usuários. Focado em arquitetura sustentável (Clean/MVVM/MVI), segurança cibernética (PCI/Dexguard) e excelência técnica.",
                    fontSize = 16
                ),

                PageComponent.Header(title = "Core Stack", fontSize = 24),
                PageComponent.Text(
                    content = "• Mobile: Kotlin, Java, Jetpack Compose, Coroutines, Flow, RxJava, Dagger/Koin.\n" +
                            "• Arquitetura: Clean Architecture, MVI, MVVM, SOLID, Modularização.\n" +
                            "• Segurança & DevOps: Dexguard, Certificação PCI, CI/CD (CircleCI/Fastlane), Firebase.\n" +
                            "• Multiplataforma: Flutter e Kotlin Multiplatform (KMP).",
                    fontSize = 14,
                    fontWeight = "BOLD"
                ),

                PageComponent.Header(title = "Experiência Profissional", fontSize = 24),

                PageComponent.Header(title = "Sensedia (Getnet) | 2024 - Atual", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "Desenvolvimento de SDKs de pagamentos para terminais POS em ambiente de missão crítica. Focado em segurança financeira e conformidade com protocolos PCI.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "Dafiti Group | 2018 - 2024 (6 anos)", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "Liderança técnica de ponta a ponta: do levantamento de requisitos à publicação. Implementei MVVM e Clean Architecture garantindo alta escalabilidade. Configurei Dexguard para proteção avançada contra engenharia reversa (RASP) e atuei na adequação do app para certificação PCI. Estrategicamente, liderei a migração da base para Flutter.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "It Lean | 2018 - 2019", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "Atuação Sênior focada em arquitetura Android e persistência de dados local avançada com Room e SQLite.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "MáximaTech | 2015 - 2018", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "Desenvolvedor Android Pleno especializado em automação comercial e força de vendas, com foco em performance e sincronização offline.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "Onnet System | 2012 - 2015", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "Desenvolvedor Android Júnior. Início da trajetória com foco em Java nativo e integração com APIs REST/Firebase.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "Formação Acadêmica", fontSize = 24),
                PageComponent.Text(
                    content = "Bacharelado em Ciência da Computação - PUC Goiás (2009 - 2013)",
                    fontSize = 16
                ),

                PageComponent.Text(content = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", textAlign = "CENTER", usePrimaryColor = true),
                PageComponent.Header(title = "Redes & Contato", fontSize = 18, textAlign = "CENTER"),
                PageComponent.Button(text = "LinkedIn", url = "https://linkedin.com/in/victorhugobenevides", isPrimary = true),
                PageComponent.Button(text = "GitHub", url = "https://github.com/victorhugobenevides", isPrimary = false),
                PageComponent.Button(text = "WhatsApp", url = "https://wa.me/5511998104606", isPrimary = false),
                PageComponent.Button(text = "E-mail", url = "mailto:victorkoto@gmail.com", isPrimary = false),
                PageComponent.Button(text = "📥 Baixar PDF (Imprimir)", url = "print", isPrimary = false)
            )

            PagesTable.insert {
                it[id] = cvPageId
                it[PagesTable.storeId] = storeId
                it[title] = "Currículo - Victor Hugo"
                it[theme] = PageThemeConfig.MINIMAL.name
                it[whatsapp] = "5511998104606"
                it[createdAt] = System.currentTimeMillis()
                it[updatedAt] = System.currentTimeMillis()
            }

            // Insere os componentes
            components.forEachIndexed { index, component ->
                PageComponentsTable.insert {
                    it[pageId] = cvPageId
                    it[type] = component::class.simpleName ?: "Unknown"
                    it[customLabel] = component.customLabel
                    it[isFilterable] = component.isFilterable
                    it[order] = index
                    it[content] = json.encodeToString(component)
                }
            }

            println("Seeder: CV Page seeded successfully.")

            println("Seeder: Initial data and CV Page updated successfully.")
        }
    }
}
