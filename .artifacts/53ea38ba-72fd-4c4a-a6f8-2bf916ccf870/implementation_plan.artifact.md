# Plano de Implementação - Estabilização Definitiva de Produção (V4)

Este plano ataca a falha de sincronia e deploy que está impedindo o funcionamento do SuperAdmin e do Checkout, garantindo que o banco de dados e o código estejam em harmonia absoluta.

## 🔍 Diagnóstico de "Causa Mortis"

1.  **Mismatch de UID no Seeder**: Se o usuário logou antes do seeder rodar com a nova lógica, o `adminId` pode ter ficado desalinhado entre o Firebase e o registro de "dono" do sistema.
2.  **Produtos Órfãos**: O Seeder criava os componentes da página de estética, mas não indexava os produtos na tabela `products`, fazendo com que o App visse uma lista vazia e a ocultasse.
3.  **Deploy Lag**: A percepção de que "nada mudou" sugere que o cache do navegador ou um atraso na Pipeline está servindo a versão antiga do sistema.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Seeder.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/database/Seeder.kt)
- **Sincronia de UID**: O seeder agora buscará o usuário pelo e-mail e usará o **ID real** dele para vincular a página `estetica-demo` e a loja padrão.
- **Indexação de Produtos**: Implementar a inserção automática dos produtos de estética (`Limpeza de Pele`, etc.) nas tabelas `products` e `component_products`. Isso garantirá que a vitrine apareça completa.
- **Logs de Auditoria**: Adicionar `println` detalhados para cada inserção para conferência via `docker logs`.

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Adicionar "Last Resort Dogma": Se o e-mail for `victorkoto@gmail.com`, o cargo é `SUPERADMIN` mesmo que a variável de ambiente falhe.

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Adicionar log de **Build Version**: `"[BOOT] Genesys21 v1.0.4 - Stabilized Architecture"`.

### [shared](file:///Users/victorben/AndroidStudioProjects/genesys21/shared)

#### [MODIFY] [KtorOrderRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/data/repository/KtorOrderRepository.kt)
- Melhorar o log de erro para mostrar o corpo da resposta da Stripe em caso de falha 400.

## 📅 Plano de Execução
1.  **Push Imediato**: Atualizar a lógica do Seeder e os logs.
2.  **Monitoramento**: Validar o log de boot do servidor no Oracle Cloud.
3.  **Reset Final**: O servidor deletará o banco antigo (devido a sua flag `DB_REBUILD=true` que ainda deve estar ativa) e recriará tudo com os novos UIDs.
