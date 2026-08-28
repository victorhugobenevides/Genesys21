# Plano de Implementação - B2B Insights & The Genesys Experience

Este plano detalha a implementação de duas frentes estratégicas: o painel de métricas globais para administradores e a página pública interativa para demonstração do produto.

## 🛠️ Build Fix (Crítico)
- Corrigido erro de *smart cast* em `SqliteOrderRepository.kt` capturando propriedades de módulos externos em variáveis locais.

---

## 1. B2B Metrics Dashboard (B2B Insights)
Permitir que o SuperAdmin monitore a saúde da rede de lojas.

### [shared](file:///Users/victorben/AndroidStudioProjects/genesys21/shared)
- **Modelos**: Criar `B2BAnalytics` e `MerchantPerformance` em `domain/model`.
- **Casos de Uso**: Criar `GetB2BAnalyticsUseCase`.

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)
- **Repositório**: Implementar queries agregadas (GMV global, total de lojistas ativos) no repositório.
- **Rotas**: Criar `GET /api/admin/b2b/summary` protegido por verificação de SuperAdmin.

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)
- **Navegação**: Adicionar aba "B2B" no `PageListScreen`.
- **UI**: Criar `B2BAnalyticsTabUI.kt` com KPIs e gráficos globais.

---

## 2. The Genesys Experience (Tour Interativo)
Página pública para converter visitantes através da experimentação direta.

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)
- **Navegação**: Rota `/experience` disponível sem login.
- **Interatividade**:
    - **Magic Theme Switcher**: Mudança de tema em tempo real.
    - **Device Sandbox**: Preview em molduras de Celular, Tablet e Desktop.
    - **Stripe Simulator**: Demonstração visual do checkout integrado.
- **Dados**: Utilizar mocks locais para garantir carregamento instantâneo.

---

## Plano de Verificação

### B2B Insights
- Validar se lojistas comuns (`MERCHANT`) recebem 403 Forbidden na nova rota.
- Verificar consistência dos cálculos de faturamento no banco.

### Genesys Experience
- Testar transições de temas e responsividade do simulador de dispositivos no Wasm.
- Validar o fluxo de conversão (CTA final para cadastro).
