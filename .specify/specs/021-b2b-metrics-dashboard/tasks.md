# Tasks: B2B Metrics Dashboard Implementation

## 🏗️ Fase 1: Domínio e Shared (KMP)
- [ ] **T001** Criar modelo de dados `B2BAnalytics` e `MerchantPerformance` no módulo `shared`.
- [ ] **T002** Definir `GetB2BAnalyticsUseCase` no módulo `shared`.
- [ ] **T003** Adicionar ícone de Business/Trending ao `GenesysIcons`.

## 🛡️ Fase 2: Backend (Server)
- [ ] **T004** Implementar lógica de agregação global no `SqliteOrderRepository`.
- [ ] **T005** Criar rota `GET /api/admin/b2b/summary` protegida por autenticação e verificação de SuperAdmin.
- [ ] **T006** Adicionar testes unitários para a nova rota de analytics global.

## 🎨 Fase 3: Front-end (Compose App)
- [ ] **T007** Adicionar aba "B2B" ao `PageListScreen.kt` visível apenas para `SUPERADMIN`.
- [ ] **T008** Criar `B2BAnalyticsTabUI.kt` para renderizar as métricas globais.
- [ ] **T009** Integrar a nova aba com o `PageViewModel` para carregar os dados reais do servidor.

## 📄 Fase 4: Documentação e Finalização
- [ ] **T010** Atualizar o `README.md` com a nova funcionalidade administrativa.
- [ ] **T011** Validar o isolamento de permissões (garantir que um `MERCHANT` comum não veja esta aba).
