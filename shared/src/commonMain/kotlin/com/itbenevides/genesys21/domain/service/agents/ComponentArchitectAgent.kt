package com.itbenevides.genesys21.domain.service.agents

import com.itbenevides.genesys21.domain.service.DevAgent
import com.itbenevides.genesys21.domain.service.AgentTaskRequest
import com.itbenevides.genesys21.domain.service.AgentTaskResponse

/**
 * Subagente especializado em arquitetura de UI e componentes Compose.
 */
class ComponentArchitectAgent : DevAgent {
    override val name: String = "Component Architect"
    override val specialty: String = "UI"

    override fun canHandle(task: String): Boolean {
        val keywords = listOf("componente", "ui", "view", "compose", "layout", "renderer")
        return keywords.any { task.lowercase().contains(it) }
    }

    override suspend fun executeTask(request: AgentTaskRequest): AgentTaskResponse {
        val task = request.task.lowercase()

        return when {
            task.contains("componente") -> generateComponentBoilerplate(request.task)
            else -> AgentTaskResponse(
                status = "success",
                agentName = name,
                message = "Entendi o pedido de UI, mas preciso de mais detalhes para gerar o código."
            )
        }
    }

    private fun generateComponentBoilerplate(description: String): AgentTaskResponse {
        val className = description.split(" ").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: "NewComponent"

        val code = """
            @Serializable
            @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.${className}")
            data class ${className}(
                val title: String,
                val subtitle: String? = null,
                @Transient
                override val customLabel: String? = null,
                @Transient
                override val isFilterable: Boolean = false,
                override val destinationUrl: String? = null,
                override val destinationPageId: String? = null,
            ) : PageComponent()

            // Lembre-se de adicionar no PageComponentRenderer.kt:
            // is PageComponent.${className} -> {
            //     ${className}Widget(component)
            // }
        """.trimIndent()

        return AgentTaskResponse(
            status = "success",
            agentName = name,
            message = "Boilerplate para o componente '${className}' gerado com sucesso.",
            generatedCode = code,
            suggestions = listOf(
                "Adicionar suporte a cores customizadas",
                "Implementar clique para navegação",
                "Registrar no Seeder.kt para testes"
            )
        )
    }
}
