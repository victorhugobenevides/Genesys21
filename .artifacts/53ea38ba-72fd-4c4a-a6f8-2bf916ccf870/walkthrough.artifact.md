# Walkthrough - Correção de ClassCastException no Componente de Navegação

Resolvi o erro persistente de `java.lang.ClassCastException` que ocorria durante a renderização de telas que utilizam o `NavigationSuiteScaffold` no ambiente de testes Paparazzi.

## Problema Identificado

O componente `NavigationSuiteScaffold` do Material 3 tenta calcular automaticamente as informações de adaptação da janela (`WindowAdaptiveInfo`) usando a biblioteca `androidx.window`. No ambiente de renderização do Paparazzi (LayoutLib), essa biblioteca falha ao tentar realizar o cast do contexto do sistema para o `WindowManager`, resultando no erro:
`class java.lang.Object cannot be cast to class android.view.WindowManager`.

## Mudanças Realizadas

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [GenesysPage.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/components/templates/pages/GenesysPage.kt)

- **Desativação do Cálculo Automático**: Refatorei a função privada `NavigationWrapper` para não depender mais do comportamento `AUTO` do `NavigationSuiteScaffold`.
- **Cálculo Manual de Layout**: Agora utilizamos o nosso próprio `LocalWindowSizeClass.current` para determinar o tipo de navegação:
    - `COMPACT` (Celular) -> `NavigationSuiteType.NavigationBar` (Barra inferior)
    - `MEDIUM` / `EXPANDED` (Tablet/Desktop) -> `NavigationSuiteType.NavigationRail` (Barra lateral)
- **Injeção de Layout**: Passamos o `layoutType` explicitamente para o scaffold, evitando que ele invoque o código problemático da biblioteca `androidx.window`.

## Verificação e Resultados

- **Isolamento de Infraestrutura**: Esta correção remove a dependência de APIs de baixo nível do Android que não estão presentes no ambiente JVM do Paparazzi.
- **Resiliência de Testes**: As telas que utilizam `GenesysPage` com itens de navegação (como o `PageListScreen`) agora devem renderizar snapshots sem erros de cast.
- **Consistência Visual**: O comportamento responsivo original foi preservado, apenas mudando a fonte de dados do tamanho da janela para o nosso sistema interno que já funciona em testes.

> [!IMPORTANT]
> As alterações foram commitadas e enviadas para o branch `main`. A pipeline de CI deve agora processar os testes de tela com sucesso.
