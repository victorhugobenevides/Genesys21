# Genesys21 - Agent & AI Engineering System

Bem-vindo ao ecossistema Genesys21. Este repositório utiliza um sistema disciplinado de **Agent Skills** (baseado na metodologia de Matt Pocock, adaptado para Kotlin Multiplatform e Ktor) para garantir alta qualidade de código, segurança inabalável e prevenção do "vibe coding".

---

## 1. Princípios Inabaláveis (Dogma & Segurança)

Antes de executar qualquer modificação, todo agente de IA DEVE respeitar:
1. **[Genesys Dogma](file://.agents/skills/genesys-dogma/SKILL.md)**:
   - Regra de Bypass de Identidade: E-mail de Ouro (`victorkoto@gmail.com`) e UID de Ouro (`mKQ9MZqG6bYhy3JqvngGpv49ZZs1`) garantem acesso `SUPERADMIN`.
   - Arquitetura KMP (Android, WasmJs, iOS, JVM).
   - Componentização declarativa em Compose com `PageComponent`.
2. **[Genesys Security](file://.agents/skills/genesys-security/SKILL.md)**:
   - Impede escalação de privilégios em `UPDATE` de usuário.
   - Recálculo de preços OBRIGATÓRIO no servidor Ktor (`SqliteOrderRepository.kt`) para prevenir *Price Tampering*.
   - Validação estrita de IDOR com `principal.name`.

---

## 2. Catálogo de Agent Skills

As skills estão localizadas no diretório `.agents/skills/`:

### 🛠️ Engenharia e Disciplinas Básicas
- **[`tdd`](file://.agents/skills/tdd/SKILL.md)**: Desenvolvimento orientado a testes (Red-Green-Refactor) em `:shared`, `:server` e `:screenshot-tests` (Paparazzi).
- **[`diagnosing-bugs`](file://.agents/skills/diagnosing-bugs/SKILL.md)**: Investigação sistemática baseada em evidências e testes reprodutores em 6 passos.
- **[`codebase-design`](file://.agents/skills/codebase-design/SKILL.md)**: MVI limpo, módulos profundos, desacoplamento Koin e segurança de interop WasmJs (`bridge.js`).

### 🎯 Qualidade e Processos
- **[`grill-me`](file://.agents/skills/grill-me/SKILL.md)**: Sabatina interativa para eliminar ambiguidades de requisitos e validar regras multi-tenant.
- **[`code-review`](file://.agents/skills/code-review/SKILL.md)**: Revisão em duplo eixo: Dogma/Segurança (Eixo 1) e Requisitos/Testes (Eixo 2).
- **[`domain-modeling`](file://.agents/skills/domain-modeling/SKILL.md)**: Gestão da linguagem ubíqua e alinhamento com `CONTEXT.md` / `genesys-dogma`.
- **[`handoff`](file://.agents/skills/handoff/SKILL.md)**: Compressão de contexto e resumo estruturado para transição de sessões de IA.

---

## 3. Comandos de Verificação do Projeto

| Tarefa | Comando Gradle |
| :--- | :--- |
| **Testes Unitários KMP** | `./gradlew :shared:allTests` |
| **Testes de Servidor** | `./gradlew :server:test` |
| **Verificar Screenshots (Paparazzi)** | `./gradlew :screenshot-tests:verifyPaparazziDebug` |
| **Gravar Screenshots (Paparazzi)** | `./gradlew :screenshot-tests:recordPaparazziDebug` |
| **Análise Estática (Linter & Detekt)** | `./gradlew ktlintCheck detekt` |
