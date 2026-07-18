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
            // 1. Create SuperAdmin
            val adminId = "victorkoto-uid"
            val exists = UsersTable.selectAll().where { UsersTable.id eq adminId }.count() > 0
            if (!exists) {
                UsersTable.insert {
                    it[id] = adminId
                    it[email] = "victorkoto@gmail.com"
                    it[name] = "Victor Hugo"
                    it[role] = UserRole.SUPERADMIN.name
                }
            }

            // 2. Create Default Store
            val storeId = "genesys-official-store"
            val storeExists = StoresTable.selectAll().where { StoresTable.id eq storeId }.count() > 0
            if (!storeExists) {
                StoresTable.insert {
                    it[id] = storeId
                    it[ownerId] = adminId
                    it[name] = "Genesys Store"
                    it[description] = "The official Genesys21 store."
                }
            }

            // 3. Create CV Page
            val cvPageId = "victor-hugo-cv"
            val pageExists = PagesTable.selectAll().where { PagesTable.id eq cvPageId }.count() > 0
            if (!pageExists) {
                PagesTable.insert {
                    it[id] = cvPageId
                    it[PagesTable.storeId] = storeId
                    it[title] = "Currículo - Victor Hugo"
                    it[theme] = PageThemeConfig.MINIMAL.name
                    it[whatsapp] = "5511998104606"
                }

                val components = listOf(
                    PageComponent.ProfileHeader(
                        imageUrl = "https://github.com/victorhugobenevides.png",
                        name = "Victor Hugo",
                        bio = "Desenvolvedor Android Sênior | Especialista Mobile",
                        imageSize = 140,
                        isCircular = true
                    ),
                    PageComponent.SocialLinks(
                        email = "victorkoto@gmail.com",
                        whatsapp = "5511998104606"
                    ),
                    PageComponent.Header(title = "Redes Profissionais", fontSize = 18, textAlign = "CENTER"),
                    PageComponent.Button(text = "GitHub", url = "https://github.com/victorhugobenevides", isPrimary = false),
                    PageComponent.Button(text = "LinkedIn", url = "https://linkedin.com/in/victorhugobenevides", isPrimary = true),

                    PageComponent.Header(title = "Sobre mim", fontSize = 24),
                    PageComponent.Text(
                        content = "Olá, eu me chamo Victor e possuo 12 anos de experiência como Desenvolvedor Android Nativo com Kotlin/Java. Tive a oportunidade de trabalhar em vários projetos de grandes empresas e desenvolver aplicativos que hoje possuem milhares de usuários na Google Play Store.",
                        fontSize = 16
                    ),

                    PageComponent.Header(title = "Stack Técnica", fontSize = 24),
                    PageComponent.Text(
                        content = "Android Studio, Java, Kotlin, XML, Jetpack Compose, RxJava, Coroutines, Retrofit, Firebase, SQLite, Room, JUnit, Mockito, MVVM, MVI, CI/CD, Clean Architecture, SOLID, Dagger2, Koin, SCRUM, CircleCI, Fastlane.",
                        fontSize = 14,
                        fontWeight = "BOLD"
                    ),

                    PageComponent.Header(title = "Experiência Profissional", fontSize = 24),

                    PageComponent.Header(title = "Dafiti Group", fontSize = 18, usePrimaryColor = true),
                    PageComponent.Text(
                        content = "Especialista Android | 2018 - 2024\nLiderei requisitos, arquitetura MVVM/Clean, pipelines CI/CD com CircleCI/Fastlane e segurança com Dexguard/PCI.",
                        fontSize = 15
                    ),

                    PageComponent.Header(title = "It Lean", fontSize = 18, usePrimaryColor = true),
                    PageComponent.Text(
                        content = "Desenvolvedor Sênior | 2018 - 2019\nFoco em persistência local com Room/SQLite e arquitetura escalável.",
                        fontSize = 15
                    ),

                    PageComponent.Header(title = "MáximaTech", fontSize = 18, usePrimaryColor = true),
                    PageComponent.Text(content = "Desenvolvedor Pleno | 2015 - 2018", fontSize = 15),

                    PageComponent.Header(title = "Formação Acadêmica", fontSize = 24),
                    PageComponent.Text(
                        content = "Ciência da Computação - PUC Goiás\nBacharelado (2009 - 2013)",
                        fontSize = 16
                    )
                )

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
            }

            println("Seeder: Initial data and CV Page seeded successfully.")
        }
    }
}
