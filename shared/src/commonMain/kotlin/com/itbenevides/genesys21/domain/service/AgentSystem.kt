package com.itbenevides.genesys21.domain.service

import kotlinx.serialization.Serializable

@Serializable
data class AgentTaskRequest(
    val task: String,
    val agentType: String? = null,
    val context: Map<String, String> = emptyMap()
)

@Serializable
data class AgentTaskResponse(
    val status: String,
    val agentName: String,
    val message: String,
    val generatedCode: String? = null,
    val suggestions: List<String> = emptyList()
)

/**
 * Interface base para todos os subagentes especializados do Genesys21.
 */
interface DevAgent {
    val name: String
    val specialty: String

    suspend fun executeTask(request: AgentTaskRequest): AgentTaskResponse
    fun canHandle(task: String): Boolean
}

/**
 * Gerenciador central que coordena a execução de tarefas entre subagentes.
 */
class AgentCoordinator(private val agents: List<DevAgent>) {

    suspend fun processTask(request: AgentTaskRequest): AgentTaskResponse {
        val agent = if (request.agentType != null) {
            agents.find { it.specialty.equals(request.agentType, ignoreCase = true) }
        } else {
            agents.find { it.canHandle(request.task) }
        }

        return agent?.executeTask(request) ?: AgentTaskResponse(
            status = "error",
            agentName = "Coordinator",
            message = "Nenhum subagente especializado encontrado para esta tarefa."
        )
    }
}
