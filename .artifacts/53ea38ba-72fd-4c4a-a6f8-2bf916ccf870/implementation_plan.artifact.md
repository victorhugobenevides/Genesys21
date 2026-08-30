# Plano de Implementação - Unificação e Estabilização do Banco de Dados de Teste

Este plano visa resolver o conflito de conexão entre o código de teste e o servidor Ktor, garantindo que ambos compartilhem exatamente o mesmo banco de dados em memória e que o estado não seja resetado durante a execução.

## 🔍 Causa Raiz Identificada
O código de teste e a `Application.module` do Ktor estavam gerando IDs de banco de dados aleatórios diferentes (`System.nanoTime()`). Mesmo com `cache=shared`, o SQLite só compartilha o cache se o nome do arquivo (mesmo que virtual) for idêntico. Como os nomes divergiam, o servidor enxergava um banco vazio enquanto o teste inseria dados em outro.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Definir uma URI constante para o banco de dados de teste: `jdbc:sqlite:file:genesys_test_db?mode=memory&cache=shared`.
- Remover a geração de `testDbId` aleatório.
- Garantir que a `module()` respeite uma inicialização prévia feita pelo thread de teste.

#### [MODIFY] [DatabaseFactory.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/database/DatabaseFactory.kt)
- Adicionar logs explícitos de inicialização.

#### [MODIFY] [SecurityHardeningTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/test/kotlin/com/itbenevides/genesys21/SecurityHardeningTest.kt)
- Usar a mesma URI constante definida no servidor.
- Garantir que o `rebuild = true` ocorra apenas uma vez no `@BeforeTest`.

## 📅 Plano de Verificação
- Rodar a compilação local: `./gradlew :server:compileKotlin`.
- Rodar os testes de segurança: `./gradlew :server:test --tests "com.itbenevides.genesys21.SecurityHardeningTest"`.
- Confirmar que as mensagens de log mostram a mesma URI de banco para ambos.
