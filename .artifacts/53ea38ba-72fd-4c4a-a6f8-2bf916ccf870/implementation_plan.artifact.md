# Plano de Implementação - Correção de ClassCastException no NavigationSuiteScaffold

O erro `java.lang.ClassCastException` persistente nos testes de screenshot (especificamente no `testAdminDashboardResponsive`) não é mais relacionado ao mock de usuário, mas sim à detecção automática de tamanho de janela do Material 3 Adaptive.

## Análise

O componente `NavigationSuiteScaffold` utiliza por padrão a função `currentWindowAdaptiveInfo()`, que por sua vez utiliza a biblioteca `androidx.window`. No ambiente do Paparazzi (LayoutLib), essa biblioteca falha ao tentar obter o `WindowManager`, resultando em um erro de cast:
`class java.lang.Object cannot be cast to class android.view.WindowManager`.

Para corrigir isso, devemos fornecer explicitamente o `layoutType` para o `NavigationSuiteScaffold`, evitando que ele tente calcular as métricas da janela automaticamente.

## Mudanças Propostas

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [MODIFY] [GenesysPage.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/components/templates/pages/GenesysPage.kt)
- Atualizar a função `NavigationWrapper` para calcular o `NavigationSuiteType` manualmente com base no nosso `LocalWindowSizeClass`.
- Passar esse `layoutType` explicitamente para o `NavigationSuiteScaffold`.
- Mapeamento sugerido:
    - `COMPACT` -> `NavigationSuiteType.NavigationBar`
    - `MEDIUM` -> `NavigationSuiteType.NavigationRail`
    - `EXPANDED` -> `NavigationSuiteType.NavigationRail`

## Plano de Verificação

### Testes Automatizados
- Executar `:screenshot-tests:testDebugUnitTest --tests "com.itbenevides.genesys21.screenshot.ScreensSnapshotTest.testAdminDashboardResponsive"`.
- Como o ambiente local apresenta erros de infraestrutura do AGP, a validação final será via pipeline de CI ou através do log de execução caso o usuário consiga rodar.

### Verificação Manual
- Garantir que a navegação continue alternando corretamente entre barra inferior e trilho lateral no app real.
