# Plano de Implementação - Dev-AI Agent System (Foco: Desenvolvimento)

Este plano visa criar uma infraestrutura de subagentes especializados para acelerar e automatizar o ciclo de desenvolvimento do Genesys21.

## 🎯 Objetivos
- Criar um **Coordenador de Agentes** no módulo `shared` para gerenciar tarefas técnicas.
- Implementar o primeiro subagente: **Component Architect**, focado em gerar código Compose válido e tipos serializáveis.
- Expor uma interface de "Dev Agent" no servidor Ktor para integração com LLMs externos.

## 🛠️ Mudanças Propostas

### [shared] Core de Agentes

#### [NEW] [AgentSystem.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/service/AgentSystem.kt)
- Define a interface `DevAgent` com métodos como `executeTask` e `getRequiredContext`.
- Implementa o `AgentCoordinator`, que decide qual subagente deve lidar com uma solicitação de desenvolvimento.

#### [NEW] [ComponentArchitectAgent.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/service/agents/ComponentArchitectAgent.kt)
- Subagente especializado na biblioteca `PageComponent`.
- **Habilidade**: Sabe ler o `Page.kt` e gerar o boilerplate necessário para novos componentes, incluindo `@Serializable` e os parâmetros padrão de IA.

---

### [server] Integração de Ferramentas (DevOps)

#### [MODIFY] [DevToolRoutes.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/routes/DevToolRoutes.kt)
- Adicionar rota `POST /api/dev/agents/task`:
    - Recebe uma tarefa (ex: "Crie um componente de Mapa").
    - O `AgentCoordinator` delega para o `ComponentArchitectAgent`.
    - Retorna o código sugerido ou executa a ação se permitido.

---

## 📅 Plano de Verificação
1.  **Geração de Componente**: Enviar um prompt via API Dev pedindo um novo componente e validar se o código gerado segue exatamente o padrão do projeto (SerialNames, etc).
2.  **Consistência**: Verificar se o subagente respeita as regras da Skill `genesys-dogma`.

> [!TIP]
> Com este sistema, o Genesys21 se torna um projeto "IA-Operável". O desenvolvedor (você) poderá dizer à IA: "Delegue para o Agente de Dados a criação de uma tabela de Cupons", e o subagente trará o código Exposed pronto baseado no `/schema`.

**Podemos iniciar a criação do `AgentSystem` focado em Desenvolvimento?**
