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

            // 3. Create/Update CV Page (Versioned Seed)
            val cvPageId = "victor-hugo-cv"
            val pageExists = PagesTable.selectAll().where { PagesTable.id eq cvPageId }.count() > 0

            // Se a página já existe, não sobrescrevemos para preservar alterações manuais via editor
            if (pageExists) {
                println("Seeder: CV Page already exists. Skipping seed to preserve manual edits.")
                return@transaction
            }

            val components = listOf(
                PageComponent.Image(
                    url = "https://ui-avatars.com/api/?name=Victor+Hugo&size=200&background=6200ee&color=fff",
                    size = 140,
                    isCircular = true
                ),
                PageComponent.Header(
                    title = "Victor Hugo",
                    textAlign = "CENTER",
                    fontSize = 28,
                    fontWeight = "EXTRA_BOLD"
                ),
                PageComponent.Text(
                    content = "Especialista Android | 11+ Anos de Experiência Mobile",
                    textAlign = "CENTER",
                    fontSize = 16
                ),

                PageComponent.Text(content = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", textAlign = "CENTER", usePrimaryColor = true),

                PageComponent.Header(title = "Resumo Profissional", fontSize = 24),
                PageComponent.Text(
                    content = "Especialista Android com trajetória sólida em empresas de grande porte e produtos de alta escala. Especialista em transformar arquiteturas complexas em sistemas sustentáveis e liderar transições tecnológicas de alto impacto. Focado em qualidade de código, segurança financeira (PCI) e otimização de engenharia via IA.",
                    fontSize = 16
                ),

                PageComponent.Header(title = "Stack Técnica Core", fontSize = 24),
                PageComponent.Text(
                    content = "• Android Nativo: Kotlin, Coroutines, Flow, Jetpack Compose, KMP (Multiplatform).\n" +
                            "• Arquitetura: Clean Architecture, MVI, MVVM, SOLID, Modularização Dinâmica.\n" +
                            "• Qualidade & DevSecOps: JUnit, MockK, CI/CD (CircleCI/GitHub Actions), Dexguard, Proguard.\n" +
                            "• IA & Ferramentas: MCP, IA Generativa aplicada à codificação, Azure, Firebase.",
                    fontSize = 14,
                    fontWeight = "BOLD"
                ),

                PageComponent.Header(title = "Experiência de Impacto", fontSize = 24),

                PageComponent.Header(title = "Sensedia (Getnet) | 2024 - Atualmente", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "• Desenvolvimento de SDKs de pagamentos para terminais POS em ambiente de missão crítica.\n" +
                            "• Implementação de protocolos de segurança financeira e conformidade PCI.\n" +
                            "• Otimização de processos de desenvolvimento utilizando IA generativa para redução de lead time.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "Dafiti Group | 2018 - 2024 (6 anos)", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "• Liderança técnica na migração estratégica de 100% dos apps nativos (Android/iOS) para Flutter.\n" +
                            "• Sustentação e evolução da base Android Nativa (Kotlin) atendendo milhões de usuários ativos.\n" +
                            "• Mentor de equipes e definição de padrões de arquitetura para escalabilidade do e-commerce.",
                    fontSize = 15
                ),

                PageComponent.Header(title = "MáximaTech | 2015 - 2018 (3 anos)", fontSize = 18, usePrimaryColor = true),
                PageComponent.Text(
                    content = "• Especialista em soluções de força de vendas com foco em performance offline e sincronização SQLite.\n" +
                            "• Desenvolvimento de aplicações robustas para automação comercial em larga escala.",
                    fontSize = 15
                ),

                PageComponent.Text(content = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", textAlign = "CENTER", usePrimaryColor = true),
                PageComponent.Header(title = "Redes & Portfólio", fontSize = 18, textAlign = "CENTER"),
                PageComponent.Button(text = "LinkedIn", url = "https://linkedin.com/in/victorhugobenevides", isPrimary = true),
                PageComponent.Button(text = "GitHub", url = "https://github.com/victorhugobenevides", isPrimary = false),
                PageComponent.Button(text = "WhatsApp", url = "https://wa.me/5511998104606", isPrimary = false),
                PageComponent.Button(text = "E-mail", url = "mailto:victorkoto@gmail.com", isPrimary = false),
                PageComponent.Button(text = "📐 Showcase", url = "/about", isPrimary = false),
                PageComponent.Button(text = "📥 PDF", url = "print", isPrimary = false)
            )

            PagesTable.insert {
                it[id] = cvPageId
                it[PagesTable.storeId] = storeId
                it[title] = "Currículo - Victor Hugo"
                it[theme] = PageThemeConfig.MINIMAL.name
                it[whatsapp] = "5511998104606"
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
