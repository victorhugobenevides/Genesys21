# Plano de Implementação - Resolução Definitiva dos Testes de Segurança

Este plano visa corrigir de uma vez por todas as falhas nos testes de `SecurityHardeningTest.kt`, atacando a causa raiz: a falta de isolamento real entre testes concorrentes e a inconsistência na persistência de dados recalculados.

## 🔍 Diagnóstico das Falhas Persistentes

### 1. Falha de Preço (Recálculo no Servidor)
- **Sintoma**: `AssertionError` (esperava 1000.0, mas veio outro valor).
- **Causa provável**: No ambiente de CI, o SQLite em memória com `cache=shared` pode não estar sincronizando commits entre a thread de setup do teste e a thread do servidor Ktor. Além disso, o servidor pode estar aceitando o nome do produto do DTO enquanto o preço vem do banco, gerando objetos híbridos.
- **Solução**: Forçar o uso do nome e preço OFICIAIS do catálogo para o `insert` no histórico de itens, ignorando completamente o que vier do DTO se o ID do produto for válido.

### 2. Falha de Cargo (Escalada de Privilégios)
- **Sintoma**: `AssertionError` (o cargo mudou para `SUPERADMIN`).
- **Causa provável**: A lógica de detecção de existência (`exists`) pode estar falhando devido a colisões de conexão, caindo no bloco de `insert` onde (em versões anteriores) o cargo era aceito.
- **Solução**: Blindagem total no `SqliteUserRepository.kt` — o cargo (`role`) e as permissões só podem ser definidos no `insert` inicial (forçado como `CUSTOMER`) ou via regra de dogma (admin oficial). Updates NUNCA tocam na coluna `role`.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [SqliteOrderRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteOrderRepository.kt)
- Adicionar logs (`println`) do valor recalculado.
- Garantir que o nome do produto salvo no `OrderItemsTable` também venha do banco de dados, não do DTO.

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Adicionar logs do cargo antes e depois da operação de save.
- Reforçar que o `update` SQL não contenha a coluna `role`.

#### [MODIFY] [SecurityHardeningTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/test/kotlin/com/itbenevides/genesys21/SecurityHardeningTest.kt)
- Alterar o setup para criar um **arquivo físico único** em `build/` para cada teste.
- Adicionar verificações explícitas do estado do banco de dados antes e depois de cada chamada de API.

## 📅 Plano de Verificação
- Rodar a compilação e testes locais do servidor.
- Monitorar os logs do CircleCI para validar os `println` de diagnóstico em caso de nova falha.
