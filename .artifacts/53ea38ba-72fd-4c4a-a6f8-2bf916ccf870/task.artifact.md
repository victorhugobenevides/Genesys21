# Tarefa: Refatoração Elite Admin Portal

## Fase 1: Infraestrutura de UI
- [ ] Criar `GenesysAdaptiveGrid` em `AdminUIComponents.kt`
- [ ] Padronizar `AdminTabHeader` para suportar layouts responsivos
- [ ] Refatorar `GenesysColumn` para remover conflitos de `BoxWithConstraints`

## Fase 2: Refatoração de Abas (Migração para LazyScroll)
- [ ] Refatorar `MainDashboardTab.kt` (Novo layout de KPIs)
- [ ] Refatorar `OrdersTab.kt` (Remover aninhamento de Master-Detail)
- [ ] Refatorar `AgendaTab.kt` (Corrigir área de toque e scroll)
- [ ] Refatorar `ServicesTab.kt` (Grid responsivo de serviços)
- [ ] Refatorar `StoreSettingsTab.kt` (Espaçamento e fluxo de formulário)
- [ ] Refatorar `PaymentsTab.kt` (Modernização visual do Stripe Connect)
- [ ] Refatorar abas globais (`B2BInsights`, `Users`, `Domains`, `Audit`)

## Fase 3: Integração e Polimento
- [ ] Atualizar `PageListScreen.kt` para remover containers redundantes
- [ ] Ajustar `GenesysPage.kt` para garantir compatibilidade total com scroll Wasm
- [ ] Validar acessibilidade e áreas de clique (min 48dp)
- [ ] Push para `main` e monitorar deploy
