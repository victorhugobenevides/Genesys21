# Plano de Implementação - Resiliência Máxima nos Testes de Screenshot

Os testes de screenshot estão enfrentando falhas em massa devido a dois problemas:
1. `IllegalStateException`: Provavelmente causado pela gestão do contexto Koin em múltiplas chamadas de snapshot.
2. `ClassCastException`: Persistência de problemas de classloader no Paparazzi, afetando o `NavigationSuiteScaffold` e a passagem de argumentos.

## Análise

- A remoção do `inline` causou problemas na execução de lambdas `@Composable`.
- O uso de argumentos padrão em funções chamadas através de classloaders diferentes (JUnit vs Paparazzi) gera métodos sintéticos `$default` que frequentemente falham com `ClassCastException`.
- `NavigationSuiteScaffold` tenta acessar o `WindowManager` mesmo quando fornecemos o `layoutType`, devido a inicializações internas ou parâmetros padrão remanescentes.

## Mudanças Propostas

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [MODIFY] [GenesysPage.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/components/templates/pages/GenesysPage.kt)
- Adicionar uma verificação de `LocalTestMode.current`. Se estivermos em modo de teste, usaremos um Scaffold simples em vez do `NavigationSuiteScaffold` se a navegação não for estritamente necessária para o teste visual, OU forneceremos o `layoutType` de forma ainda mais isolada.
- Na verdade, vou tentar fornecer um `WindowAdaptiveInfo` fixo via `CompositionLocalProvider` se possível, para silenciar a biblioteca `androidx.window`.

### [screenshot-tests](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests)

#### [MODIFY] [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- **Voltar para `inline`**: Essencial para o funcionamento correto das lambdas do Compose.
- **Nomes Únicos para Overloads**: Remover sobrecargas com o mesmo nome para evitar confusão de assinatura no bytecode.
    - `genesysResponsiveSnapshot` (Simples)
    - `genesysResponsiveSnapshotWithPrefix` (Com prefixo)
    - `genesysResponsiveSnapshotFull` (Com mock de usuário)
- **Sem Argumentos Opcionais**: Todas as funções terão argumentos explícitos para evitar a geração de métodos `$default` problemáticos.
- **Gestão de Koin**: Garantir que o `KoinApplication` seja o único ponto de entrada do Koin dentro do snapshot.

#### [MODIFY] [ScreensSnapshotTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/ScreensSnapshotTest.kt) (e outros arquivos de teste)
- Atualizar as chamadas para os novos nomes únicos das funções.

## Plano de Verificação

### Testes Automatizados
- O foco é eliminar o `IllegalStateException` em todos os testes e o `ClassCastException` no `testAdminDashboardResponsive`.
- Como não consigo rodar localmente com sucesso total, aplicarei as mudanças e solicitarei a execução/log ao usuário.
