---
name: genesys-dogma
description: Guia de arquitetura técnica e regras fundamentais do projeto Genesys21 para desenvolvedores e agentes de IA.
---

# Genesys21 Dogma: Manual do Desenvolvedor AI

Este documento define os princípios inabaláveis e a arquitetura técnica do ecossistema Genesys21 (Android, Wasm, Server).

## 1. Identidade e Segurança (O Dogma)

O sistema possui uma regra de bypass de identidade para garantir que o proprietário sempre tenha acesso total (SUPERADMIN), independentemente do estado do banco de dados ou da falha em provedores externos.

### 🔑 Regras de Bypass
- **E-mail de Ouro**: `victorkoto@gmail.com`
- **UID de Ouro**: `mKQ9MZqG6bYhy3JqvngGpv49ZZs1` (Firebase UID)
- **Implementação**: Localizada em `SqliteUserRepository.kt` e validada em `KtorUserRepository.kt` (Client-side God Mode).

### 🛡️ Níveis de Acesso
- `SUPERADMIN`: Acesso total a todos os lojistas, logs, analytics B2B e gerenciamento de domínios.
- `MERCHANT`: Dono de uma loja. Pode editar suas páginas, produtos e gerenciar seus pedidos.
- `ADMIN`: Funcionário de um lojista com permissões granulares.
- `CUSTOMER`: Cliente final. Pode visualizar páginas públicas, fazer pedidos e agendar serviços.

---

## 2. Arquitetura Multiplataforma

O projeto utiliza **Kotlin Multiplatform (KMP)** com foco em WasmJs para a web.

### 🌉 Wasm Interop (A Ponte)
- O arquivo `bridge.js` é o coração da comunicação entre o Kotlin Wasm e as APIs do navegador (Firebase, Stripe, Google One Tap).
- **Polyfill Crítico**: Devido a restrições de segurança em contextos não-HTTPS (acesso via IP), o `randomUUID` é injetado manualmente no `index.html`.

### 🏗️ Padrões de Código
- **MVI (Model-View-Intent)**: Utilizado em todas as telas via ViewModels compartilhados.
- **Clean Architecture**: Separação rigorosa entre `domain`, `data` e `presentation`.
- **DI (Koin)**: Injeção de dependência unificada entre server e client.

---

## 3. Servidor e Persistência

- **Framework**: Ktor Server.
- **ORM**: JetBrains Exposed.
- **Banco**: SQLite (Produção e Dev) com suporte a modo WAL para concorrência.
- **Operação Nuclear**: O servidor pode ser configurado via variável de ambiente `DB_REBUILD=true` para deletar o banco e recriar todo o esquema e dados iniciais (Seeder).

---

## 4. Componentização de Páginas

As páginas do Genesys21 são baseadas no `sealed class PageComponent`.
- Cada componente deve ser `@Serializable`.
- Possuem campos obrigatórios para IA: `customLabel`, `isFilterable`, `destinationUrl`, `destinationPageId`.
- **Renderização**: Feita dinamicamente no Compose através do `PageComponentRenderer.kt`.

---

## 🤖 Instruções para Agentes de IA
1.  **Sempre verifique o Dogma**: Antes de alterar repositórios de usuários, garanta que as verificações de bypass de e-mail/UID não foram removidas.
2.  **Deployment**: Ao sugerir mudanças em componentes compartilhados, lembre-se de que os baselines de imagem do Paparazzi precisarão ser atualizados (`./gradlew :screenshot-tests:recordPaparazziDebug`).
3.  **Wasm Safety**: Evite usar APIs experimentais do Kotlin que não possuam mapeamento equivalente em JS/Wasm sem antes verificar o `bridge.js`.
