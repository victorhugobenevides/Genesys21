# Spec-Driven Development (SDD) Workflow

Este projeto segue a metodologia **Spec-Driven Development (SDD)** baseada no [github/spec-kit](https://github.com/github/spec-kit). O fluxo é dividido em fases incrementais onde requisitos se tornam artefatos executáveis.

## 1. Ciclo de Vida do Desenvolvimento

O desenvolvimento de novas funcionalidades ou correções de bugs segue 4 fases principais:

1.  **Constitute**: Define os princípios governamentais, padrões de teste e guardrails de qualidade do projeto.
2.  **Specify**: Define os requisitos funcionais ("o quê" e "porquê") e user stories, independente da stack técnica.
3.  **Plan**: Determina a arquitetura, stack tecnológica e detalhes técnicos de implementação.
4.  **Implement**: Quebra o plano em tarefas acionáveis e executa a geração de código.

---

## 2. Comandos do Speckit

Os comandos abaixo são utilizados através de agentes de IA (como Claude Code ou GitHub Copilot) para navegar entre as fases.

### Comandos Principais
*   **`/speckit.constitution`**: Cria ou atualiza os princípios governamentais e guias de desenvolvimento (armazenados em `.speckit/CONSTITUTION.md`).
*   **`/speckit.specify`**: Gera requisitos funcionais e user stories com base em uma descrição de alto nível (armazenados em `.speckit/SPECIFICATION.md`).
*   **`/speckit.plan`**: Gera um plano técnico de implementação, incluindo escolhas de arquitetura e modelos de dados (armazenados em `.speckit/PLAN.md`).
*   **`/speckit.tasks`**: Converte o plano de implementação em uma lista ordenada de tarefas (armazenadas em `tasks.md`).
*   **`/speckit.taskstoissues`**: Converte a lista de tarefas em issues do GitHub para rastreamento.
*   **`/speckit.implement`**: Executa a implementação das tarefas de acordo com o plano.

### Comandos de Suporte
*   **`/speckit.clarify`**: Identifica e resolve áreas subespecificadas nos requisitos antes do planejamento.
*   **`/speckit.analyze`**: Realiza análise de consistência entre artefatos (requisitos vs plano vs código).
*   **`/speckit.checklist`**: Gera checklists de qualidade personalizados para validar artefatos.

---

## 3. Specify CLI (Interface de Linha de Comando)

Para gerenciar o toolkit e inicializar novos fluxos:

*   `specify init`: Inicializa o projeto com templates e integrações.
*   `specify self upgrade`: Atualiza a CLI para a versão mais recente.
*   `specify extension add <name>`: Adiciona novas capacidades ou workflows específicos.
*   `specify preset add <name>`: Sobrescreve templates padrão para padrões organizacionais.

---

## 4. Estado do Workflow

O progresso do fluxo atual é rastreado no arquivo `.speckit/state.json`. Este arquivo mantém o histórico de cada fase, incluindo timestamps de início e fim, status e pontuação de qualidade.
