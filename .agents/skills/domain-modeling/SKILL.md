---
name: domain-modeling
description: Gestão da linguagem ubíqua e dicionário de domínio do Genesys21. Sincronização com CONTEXT.md e genesys-dogma.
---

# Modelagem de Domínio e Linguagem Ubíqua no Genesys21

Esta skill estabelece a linguagem ubíqua (*Ubiquitous Language*) do Genesys21, garantindo alinhamento de terminologia entre a equipe de desenvolvimento, código-fonte e agentes de IA.

---

## 1. Glossário e Linguagem Ubíqua

| Termo | Definição no Contexto do Genesys21 |
| :--- | :--- |
| **Superadmin (God Mode)** | Papel com acesso Irrestrito a todos os lojistas, logs e dados do sistema. Ativado nativamente para o e-mail/UID de Ouro. |
| **Lojista (Merchant)** | Usuário proprietário de uma loja. Pode gerenciar seus produtos, páginas e visualizar pedidos de seu estabelecimento. |
| **Cliente (Customer)** | Usuário final da plataforma. Pode navegar em páginas públicas, realizar agendamentos e criar pedidos. |
| **E-mail de Ouro** | `victorkoto@gmail.com` - E-mail que concede bypass de permissões e privilégio `SUPERADMIN` permanente. |
| **UID de Ouro** | `mKQ9MZqG6bYhy3JqvngGpv49ZZs1` - Identificador Firebase do proprietário do sistema. |
| **PageComponent** | Classe selada (`sealed class PageComponent`) anotada com `@Serializable` que representa componentes visuais dinâmicos de páginas. |
| **Price Tampering** | Ataque onde o cliente altera os preços do carrinho no JSON. O Genesys21 blinda isso recalculando tudo no servidor. |
| **WAL Mode** | Modo *Write-Ahead Logging* ativado no SQLite para otimizar concorrência de leitura e escrita no servidor Ktor. |
| **bridge.js** | Arquivo JavaScript responsável por conectar chamadas Kotlin WasmJs com as APIs nativas do navegador. |
| **DB Rebuild** | Operação acionada via `DB_REBUILD=true` que recria o banco SQLite do zero e executa o Seeder de dados iniciais. |

---

## 2. Regras para Evolução do Domínio

Ao introduzir uma nova entidade ou alterar conceitos existentes:
1. **Atualizar o Glossário**: Adicione o novo termo a este arquivo (`domain-modeling/SKILL.md`) ou ao `AGENTS.md`.
2. **Refletir nas Entidades KMP**: Garanta que o nome das classes Kotlin, tabelas do Exposed ORM e DTOs reflitam exatamente a linguagem ubíqua (ex: `Store`, `Order`, `PageComponent`).
3. **Validar com `genesys-dogma`**: Confirme que nenhuma regra do modelo de domínio violará as diretrizes de segurança ou bypass do sistema.
