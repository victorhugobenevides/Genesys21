# Plano de Implementação - Estabilização de Testes de Segurança

Este plano visa corrigir as falhas nos testes de `SecurityHardeningTest.kt` no ambiente de CI, garantindo que as validações de segurança sejam executadas corretamente e sem interferências de infraestrutura de banco de dados.

## Análise das Falhas

### 1. Falha no Recálculo de Preço
- **Erro**: `java.lang.Exception at SqliteOrderRepository.kt:42` (Pedido não encontrado).
- **Causa**: O pedido não está sendo salvo no banco de dados durante o POST, provavelmente devido a uma falha de integridade referencial (falta da Loja/Store no banco de dados de teste).
- **Solução**: Inserir explicitamente uma loja (`StoresTable`) no setup do teste antes de tentar criar o pedido.

### 2. Falha na Prevenção de Escalada de Cargo
- **Erro**: `java.lang.AssertionError` (O cargo foi alterado ou não permaneceu como `CUSTOMER`).
- **Causa**: Possível persistência de dados entre testes devido ao uso de `:memory:` com `cache=shared` ou erro na lógica de detecção de existência do usuário no repositório.
- **Solução**:
    - Refatorar `saveUserProfile` para ser ainda mais rígido: novos usuários (insert) SEMPRE serão criados como `CUSTOMER`, independente do que for enviado no JSON (exceto o admin Dogma).
    - Garantir isolamento total do banco de dados entre os testes.

## Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Alterar o bloco `else` (insert) do `saveUserProfile` para forçar `it[role] = UserRole.CUSTOMER.name` (exceto para o dogma admin). Isso fecha completamente a brecha de mass assignment na criação de conta.

#### [MODIFY] [SecurityHardeningTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/test/kotlin/com/itbenevides/genesys21/SecurityHardeningTest.kt)
- Adicionar a criação de uma Store no setup do teste de pedidos.
- Adicionar verificações de `HttpStatusCode` nas respostas das APIs para facilitar o diagnóstico de falhas.
- Usar IDs únicos por teste para evitar conflitos de cache do SQLite.

## Plano de Verificação

### Testes Automatizados
- Rodar `./gradlew :server:test --tests "com.itbenevides.genesys21.SecurityHardeningTest"` localmente (simulando ambiente limpo).
- Verificar logs do CircleCI após o push.
