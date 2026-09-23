---
name: codebase-design
description: Diretrizes de arquitetura de software e design de módulos profundos no Genesys21 (MVI, Clean Architecture, Koin e WasmJs safety).
---

# Design de Código e Módulos Profundos no Genesys21

Esta skill estabelece os padrões arquiteturais para manter a base de código do Genesys21 modular, segura, testável e sem acúmulo de acoplamento (*code entropy*).

---

## 1. Módulos Profundos e Fronteiras Limpas

Um módulo ou classe "profunda" possui uma **interface pública pequena e simples**, mas esconde uma **implementação rica e robusta**.

```
  ┌────────────────────────────────────────────────────────┐
  │ Interface Pública Pequena (ex: StoreRepository)        │
  └───────────────────────────┬────────────────────────────┘
                              │
  ┌───────────────────────────▼────────────────────────────┐
  │ Implementação Profunda (SqliteStoreRepository)         │
  │ - Validação IDOR com principal.name                    │
  │ - Suporte a WAL mode no SQLite                         │
  │ - Transações Exposed ORM                               │
  │ - Suporte ao Rebuild / Seeder                          │
  └────────────────────────────────────────────────────────┘
```

---

## 2. Camadas de Arquitetura (Clean Architecture)

O Genesys21 separa as responsabilidades estritamente em 3 camadas principais:

### 1. Camada de Domínio (`domain`)
- **Sem dependências de framework**: Contém entidades de negócio (`Order`, `Store`, `User`), repositórios abstratos (`StoreRepository`) e Use Cases.
- **Independência Multiplataforma**: Deve compilar puramente para todas as metas KMP (JVM, Android, iOS, WasmJs, JS).

### 2. Camada de Dados (`data`)
- **Implementações Concretas**: `SqliteUserRepository.kt`, `SqliteOrderRepository.kt`.
- **Regra de Ouro da Segurança**: Repositórios de dados executam a validação de segurança e o recálculo server-side dos valores de pedidos antes da gravação no banco de dados.

### 3. Camada de Apresentação (`presentation`) - Padrão MVI
- Utiliza **Model-View-Intent (MVI)** com ViewModels compartilhados via Koin.
- O estado da UI é exposto exclusivamente como um `StateFlow<UiState>` imutável.
- As intenções do usuário são enviadas através de funções de contrato de intenção (`onIntent(intent: UiIntent)`).

---

## 3. Segurança em WasmJs Interop

Ao criar funcionalidades no módulo `:shared` ou `:composeApp` compatíveis com WasmJs:
1. **Ponte JS (`bridge.js`)**: Nunca invoque funções de navegador não mapeadas diretamente em Kotlin sem garantir o binding correspondente no `bridge.js`.
2. **Contexto Seguro / Polyfills**: Lembre-se de que ambientes sem HTTPS (acesso local por IP) exigem os polyfills configurados em `index.html` (como o polyfill para `crypto.randomUUID()`).
3. **Serialização IR**: Sempre marque DTOs compartilhados com `@Serializable`.

---

## 4. Injeção de Dependências com Koin

- Módulos Koin são organizados por contexto (`sharedModule`, `serverModule`, `appModule`).
- Use `single` para repositórios e serviços de banco de dados.
- Use `viewModel` ou `factory` para componentes com ciclo de vida ligado a telas.
