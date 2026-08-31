# Plano de Implementação - Estabilização Final dos Testes do Servidor

Este plano resolve as falhas persistentes nos testes do servidor, corrigindo o erro de estado do plugin de RateLimit e garantindo a integridade dos dados nos testes de segurança.

## 🔍 Causa Raiz das Falhas

### 1. Falhas no `ApplicationTest` (RateLimit)
- **Erro**: `java.lang.IllegalStateException at RateLimitInterceptors.kt:38`.
- **Causa**: O plugin `RateLimit` não estava sendo instalado quando `isTesting == true`, mas as rotas continuavam tentando usá-lo. O Ktor exige que o plugin esteja instalado se qualquer rota o referenciar.
- **Solução**: Sempre instalar o plugin `RateLimit`, mas configurá-lo com limites extremamente altos em ambiente de teste para não interferir na execução.

### 2. Falhas no `SecurityHardeningTest` (AssertionError)
- **Erro**: `java.lang.AssertionError`.
- **Causa**: Provável dessincronização de dados ou resquícios de estados de testes anteriores. O uso de `/tmp/genesys_security_test.db` como caminho fixo pode causar colisões se o arquivo não for limpo corretamente ou se permissões de escrita falharem no CI.
- **Solução**:
    - Usar um caminho relativo dentro da pasta `build/` do projeto para garantir permissões.
    - Melhorar as mensagens das asserções para expor os valores reais recebidos em caso de falha.
    - Garantir que o `DatabaseFactory` seja resetado completamente entre cada teste.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Alterar a instalação do `RateLimit` para ser incondicional.
- Configurar `rateLimiter(limit = 1000, refillPeriod = 1.seconds)` quando `isTesting == true`.

#### [MODIFY] [SecurityHardeningTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/test/kotlin/com/itbenevides/genesys21/SecurityHardeningTest.kt)
- Mudar `testDbPath` para `build/test-db/security_hardening.db`.
- Adicionar mensagens descritivas em todos os `assertEquals`.
- Adicionar logs do corpo da resposta em caso de falha de status code.

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Reforçar que o campo `role` NUNCA é atualizado via `saveUserProfile` para usuários existentes, exceto o admin dogma. (Já implementado, mas revisarei por redundância).

## 📅 Plano de Verificação
- Rodar `./gradlew :server:test` localmente e garantir que os 13 testes passem.
- Monitorar a Pipeline do CircleCI.
