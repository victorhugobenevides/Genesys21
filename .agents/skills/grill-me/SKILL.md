---
name: grill-me
description: Sabatina interativa para refinamento rigoroso de requisitos, análise de riscos de segurança e regras de negócio multi-tenant no Genesys21.
---

# Grill Me: Sabatina Interativa de Requisitos no Genesys21

A skill **`grill-me`** é um processo de interrogatório reflexivo conduzido pela IA antes da escrita de código. O objetivo é desafiar premissas, expor casos de borda e alinhar a solução com o **Genesys21 Dogma** e o **Genesys21 Security**.

---

## 1. Quando Invocar esta Skill
- Antes de iniciar uma nova feature complexa (ex: novos componentes de página `PageComponent`, integração com novos meios de pagamento, novas rotas multi-tenant).
- Quando a solicitação do usuário for vaga ou contiver requisitos contraditórios.
- Quando houver alterações na estrutura do banco de dados (Exposed) ou no modelo de permissões.

---

## 2. Eixos de Interrogação (A Sabatina)

A IA deve sabatinar o desenvolvedor (ou a si mesma em planejamento) cobrindo os seguintes eixos:

### Eixo A: Segurança e Acesso Multi-tenant (Security Checklist)
1. **Regra de Bypass de Identidade**: Esta mudança afeta o comportamento do "E-mail de Ouro" (`victorkoto@gmail.com`) ou UID de Ouro (`mKQ9MZqG6bYhy3JqvngGpv49ZZs1`)?
2. **Isolamento de Dados (IDOR)**: Como garantimos que o Lojista A não possa visualizar ou modificar registros do Lojista B através do ID do recurso?
3. **Price Tampering**: Se a funcionalidade envolver compras ou pagamentos, os valores e totais são calculados estritamente no servidor (`:server`)?
4. **Permissões Granulares**: Qual o nível mínimo de papel necessário (`SUPERADMIN`, `MERCHANT`, `ADMIN`, `CUSTOMER`)?

### Eixo B: Arquitetura Multiplataforma (KMP & Wasm)
1. **Interop WasmJs**: A nova dependência ou API Kotlin funciona nativamente em WasmJs? Requer ajustes no `bridge.js` ou novos polyfills?
2. **Serialização**: As estruturas de dados trafegadas na rede utilizam `@Serializable` da biblioteca `kotlinx.serialization`?
3. **Compose UI**: A interface precisa ser testada visualmente via Paparazzi em `:screenshot-tests`?

### Eixo C: Resiliência e Persistência
1. **Transações do Banco**: A operação no Exposed ORM requer bloco de transação (`transaction { ... }`) com suporte a WAL mode?
2. **Operação Rebuild / Seeder**: A nova entidade é recriada corretamente quando a variável `DB_REBUILD=true` for ativada no servidor?

---

## 3. Formato da Resolução

A sabatina deve resultar em um documento curto de especificações contendo:
- **Decisões de Design (ADR)**.
- **Contrato de API / DTOs**.
- **Plano de Testes (TDD)**.
