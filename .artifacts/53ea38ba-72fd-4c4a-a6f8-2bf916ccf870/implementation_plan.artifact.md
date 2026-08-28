# Plano de Implementação - Estabilização Final e Resiliência de Testes

Os testes de screenshot continuam falhando devido a problemas de infraestrutura de composição (CompositionLocals) e ciclo de vida do Koin. Mesmo fornecendo o `LocalTestMode`, o código parece cair no bloco `else` que invoca o `NavigationSuiteScaffold`.

## Análise

- **Falha de Propagação**: O `LocalTestMode` pode estar sendo "perdido" ou não respeitado devido à forma como o Paparazzi e o Koin interagem com a árvore de composição.
- **Koin 4.0**: O uso de `KoinContext` ou `KoinApplication` em loops de snapshot responsivo precisa ser extremamente cuidadoso para evitar o `IllegalStateException`.
- **NavigationSuiteScaffold**: Este componente é a fonte principal de `ClassCastException` no Paparazzi. Precisamos de uma forma garantida de ignorá-lo em testes.

## Mudanças Propostas

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [MODIFY] [ResponsiveUtils.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/util/ResponsiveUtils.kt)
- Adicionar uma função auxiliar `isTestMode()` que verifica tanto o `LocalTestMode.current` quanto uma Propriedade de Sistema (`System.getProperty("genesys.test_mode")`). Isso garante redundância.

#### [MODIFY] [GenesysPage.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/components/templates/pages/GenesysPage.kt)
- Usar a nova função `isTestMode()` para decidir entre o layout manual e o `NavigationSuiteScaffold`.

### [screenshot-tests](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests)

#### [MODIFY] [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- Configurar `System.setProperty("genesys.test_mode", "true")`.
- Simplificar a injeção do Koin: usar a abordagem clássica de Paparazzi com o `KoinApplication` mas garantindo que ele não tente iniciar o Koin globalmente (usando o parâmetro `application` do Koin).
- Remover o override manual de `LocalContext`. O Paparazzi já fornece um contexto adequado para renderização.

## Plano de Verificação

### Testes Automatizados
- Executar `:screenshot-tests:testDebugUnitTest`.
- O sucesso será atingido quando a pipeline de CI completar sem `IllegalStateException` e os snapshots forem gerados.
