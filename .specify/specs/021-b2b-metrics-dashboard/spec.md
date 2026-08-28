# Spec 021: B2B Metrics Dashboard (B2B Insights)

## 1. Visão Geral
Como uma plataforma White-Label, o **Genesys21** precisa fornecer uma visão macro para os proprietários da plataforma ou parceiros estratégicos que gerenciam múltiplos lojistas. O painel de "B2B Insights" permitirá visualizar a performance global da rede de lojas, facilitando decisões de negócio e monitoramento de crescimento.

## 2. Objetivos
- Fornecer métricas agregadas de todas as lojas ativas.
- Identificar os lojistas com maior volume de vendas (GMV).
- Monitorar a saúde financeira da plataforma como um todo.
- Facilitar a gestão de parcerias B2B.

## 3. Requisitos Funcionais

### 3.1 Métricas Globais (Aggregated Metrics)
O painel deve exibir:
- **Total de Lojistas Ativos**: Quantidade de usuários com role `MERCHANT` ou `ADMIN` que possuem pelo menos uma página publicada.
- **GMV da Plataforma (Gross Merchandise Volume)**: Soma total de todos os pedidos com status `COMPLETED` em todas as lojas.
- **Ticket Médio da Rede**: Valor médio dos pedidos realizados em toda a plataforma.
- **Conversão Global (Futuro)**: Razão entre visualizações de páginas e pedidos criados.

### 3.2 Listagem de Performance de Lojistas
- Ranking dos Top 5 ou Top 10 lojistas por faturamento.
- Visualização rápida do número de pedidos por lojista.

### 3.3 Filtros Temporal
- Capacidade de visualizar métricas de: Hoje, Últimos 7 dias, Últimos 30 dias e Todo o período.

## 4. Design & UX

### 4.1 Localização no App
- Uma nova aba no menu administrativo (`PageListScreen`) chamada **"B2B Insights"**.
- Ícone sugerido: `GenesysIcons.BusinessCenter` ou `TrendingUp`.

### 4.2 Componentes Visuais
- **KPI Cards**: Uso do `GenesysStatsCard` para as métricas principais.
- **Gráficos**: Gráfico de linha para faturamento global diário.
- **Tabelas**: Lista simplificada de lojistas de alta performance.

## 5. Arquitetura Técnica

### 5.1 Modelagem de Dados
Nova classe no módulo `shared`:
```kotlin
@Serializable
data class B2BAnalytics(
    val totalMerchants: Int,
    val platformGMV: Double,
    val globalAverageTicket: Double,
    val topMerchants: List<MerchantPerformance>,
    val globalDailyRevenue: List<DailyRevenue>
)

@Serializable
data class MerchantPerformance(
    val merchantName: String,
    val totalRevenue: Double,
    val orderCount: Int
)
```

### 5.2 Backend (Ktor)
- Novo endpoint: `GET /api/admin/b2b/summary`.
- Segurança: Restrito a usuários com cargo `SUPERADMIN`.

### 5.3 Repositório (Server)
- Método no `SqliteOrderRepository` ou novo `SqliteB2BRepository` para realizar as queries agregadas (JOIN entre `orders`, `stores` e `users`).

## 6. Segurança & Permissões
- Somente o `SUPERADMIN` (atualmente `victorkoto@gmail.com`) terá acesso inicial a esta aba.
- No futuro, adicionar permissão `VIEW_B2B_INSIGHTS`.
