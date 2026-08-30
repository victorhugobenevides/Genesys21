# Plano de Implementação - Refinamento e Realização do Console Administrativo

Este plano visa concluir a melhoria das funcionalidades da página ADM, transformando os "placeholders" do backend em funcionalidades reais e integradas, além de expandir a cobertura de auditoria para ações críticas.

## 🎯 Objetivos
- Tornar o **B2B Insights** funcional com métricas reais de toda a rede.
- Implementar a visualização real de **Logs de Auditoria** para o SuperAdmin.
- Integrar um feed de "Atividades Recentes" no Dashboard do lojista.
- Garantir que toda mudança sensível (status de pedido, cargo, permissões) seja registrada.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [SqliteOrderRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteOrderRepository.kt)
- **getB2BAnalytics**: Implementar queries para contar lojistas ativos, somar GMV Global e calcular o ranking de performance.
- **getAuditLogs**: Implementar a busca real na `AuditLogsTable`.
- **updateOrderStatus**: Adicionar log de auditoria ao mudar o status de um pedido.

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- **updateUserRole / updateUserPermissions**: Adicionar logs de auditoria detalhados para estas ações críticas.

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [MODIFY] [MainDashboardTab.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/list/tabs/MainDashboardTab.kt)
- Adicionar uma seção de "Atividades Recentes" que consome os logs de auditoria (filtrados pela loja do usuário).

## 📅 Cronograma de Execução
1.  **Fase 1: Backend Real** - Implementar as queries SQL agregadas.
2.  **Fase 2: Auditoria Total** - Espalhar os logs de auditoria nos repositórios.
3.  **Fase 3: Dashboard Conectado** - Exibir as atividades no front-end.

## ✅ Verificação
- Validar se o B2B Insights exibe os números corretos (comparando com o banco).
- Verificar se cada mudança de status de pedido gera uma nova linha na aba de Auditoria.
