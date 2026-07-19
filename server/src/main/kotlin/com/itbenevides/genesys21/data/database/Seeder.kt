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

            // 3. Create/Update CV Page
            val cvPageId = "victor-hugo-cv"
            val pageExists = PagesTable.selectAll().where { PagesTable.id eq cvPageId }.count() > 0

            val components = listOf(
                PageComponent.ProfileHeader(
                    imageUrl = "https://github.com/victorhugobenevides.png",
                    name = "Victor Hugo",
                    bio = "Desenvolvedor Android Especialista | Mobile & Flutter Expert",
                    imageSize = 140,
                    isCircular = true
                ),
                PageComponent.SocialLinks(
                    email = "victorkoto@gmail.com",
                    whatsapp = "5511998104606"
                ),

                PageComponent.Header(title = "Redes & Portfólio", fontSize = 18, textAlign = "CENTER"),
                PageComponent.Row(
                    components = listOf(
                        PageComponent.Button(text = "LinkedIn", url = "https://linkedin.com/in/victorhugobenevides", isPrimary = true),
                        PageComponent.Button(text = "GitHub", url = "https://github.com/victorhugobenevides", isPrimary = false)
                    )
                ),
                PageComponent.Row(
                    components = listOf(
                        PageComponent.Button(text = "📐 Showcase", url = "/about", isPrimary = false),
                        PageComponent.Button(text = "📥 PDF", url = "print", isPrimary = false)
                    )
                ),

                PageComponent.Text(content = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", textAlign = "CENTER", usePrimaryColor = true),

                PageComponent.Header(title = "Sobre mim", fontSize = 24),
                PageComponent.Text(
                    content = "Olá! Sou o Victor, Especialista Mobile com mais de 12 anos de experiência transformando ideias em aplicativos nativos de alta performance. Tenho um histórico sólido liderando projetos complexos em grandes empresas, focando sempre em arquiteturas escaláveis, segurança cibernética e, mais recentemente, integrando Inteligência Artificial no ciclo de vida do desenvolvimento para acelerar a inovação.",
                    fontSize = 16
                ),

                PageComponent.Header(title = "Stack de Especialista", fontSize = 24),
                PageComponent.Text(
                    content = "• Mobile: Android Studio, Java, Kotlin, Swift, Flutter, Jetpack Compose, RxJava, Coroutines.\n" +
                            "• Arquitetura & Qualidade: Clean Architecture, MVVM, MVI, SOLID, JUnit, Mockito.\n" +
                            "• Segurança & DevOps: Dexguard, PCI Compliance, CircleCI, Fastlane, GitHub Actions.\n" +
                            "• Cloud & IA: Azure, GitHub Copilot, Devin, MCP (Model Context Protocol), SDD guiado por IA.",
                    fontSize = 14,
                    fontWeight = "BOLD"
                ),

                PageComponent.Header(title = "Experiência Profissional", fontSize = 24),

                PageComponent.Header(title = "Sensedia (Getnet)", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "Especialista Android & Pagamentos | Atualmente\n" +
                            "Focado no desenvolvimento de aplicativos de pagamento para terminais POS. Utilizo IA Generativa (Copilot, Devin) e arquitetura em nuvem (Azure) para otimizar processos de engenharia e garantir a robustez de transações financeiras.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "Dafiti Group", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "Especialista Mobile | 2018 - 2024\n" +
                            "Liderei a migração tecnológica de 100% dos aplicativos nativos (Android & iOS) para Flutter, garantindo paridade de funcionalidades e otimização de performance para milhões de usuários. Atuei intensamente com Kotlin e Swift (nativo), arquitetura MVVM/Clean, pipelines CI/CD e segurança avançada com Dexguard.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "It Lean", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "Desenvolvedor Sênior | 2018 - 2019\nFoco em persistência local com Room/SQLite e arquitetura escalável para grandes clientes.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "MáximaTech", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(content = "Desenvolvedor Pleno | 2015 - 2018\nDesenvolvimento de soluções de força de vendas e automação comercial.", fontSize = 15),

                PageComponent.Header(title = "Formação Acadêmica", fontSize = 24),
                PageComponent.Text(
                    content = "Ciência da Computação - PUC Goiás\nBacharelado (2009 - 2013)",
                    fontSize = 16
                )
            )

            if (!pageExists) {
                PagesTable.insert {
                    it[id] = cvPageId
                    it[PagesTable.storeId] = storeId
                    it[title] = "Currículo - Victor Hugo"
                    it[theme] = PageThemeConfig.MINIMAL.name
                    it[whatsapp] = "5511998104606"
                }
            } else {
                PagesTable.update({ PagesTable.id eq cvPageId }) {
                    it[PagesTable.storeId] = storeId
                    it[title] = "Currículo - Victor Hugo"
                    it[theme] = PageThemeConfig.MINIMAL.name
                }
            }

            // Re-insere os componentes
            PageComponentsTable.deleteWhere { pageId eq cvPageId }
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

            println("Seeder: Initial data and CV Page updated successfully.")
        }
    }
}
