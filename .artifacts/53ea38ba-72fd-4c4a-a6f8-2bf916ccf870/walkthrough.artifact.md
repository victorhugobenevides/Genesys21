# Walkthrough - Solução Final para ClassCastException em Screenshot Tests

Concluí a refatoração completa para resolver o erro persistente de `ClassCastException` nos testes de screenshot.

## O Problema Final

Identificamos que a ponte de *classloaders* do Paparazzi é extremamente sensível. Mesmo usando parâmetros `Any?`, a simples chamada de uma função de extensão ou a passagem de Enums do projeto como argumentos causava falhas de cast no runtime, pois o JUnit e o LayoutLib possuíam definições "diferentes" das mesmas classes.

## Mudanças Implementadas

### [screenshot-tests](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests)

#### [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- **Desacoplamento de Extensões**: Converti `genesysSnapshot` e `genesysResponsiveSnapshot` de funções de extensão para funções regulares. Isso remove a dependência do receptor (`receiver`) que poderia estar em um *classloader* diferente.
- **Ponte de Tipos Primitivos**: As funções agora aceitam apenas Strings e Listas de Strings para configurar o mock do perfil de usuário. A conversão para os tipos reais (`UserProfile`, `UserRole`, `UserPermission`) ocorre estritamente dentro do bloco `snapshot { ... }`.

#### Refatoração Global de Testes (15 arquivos)
- Atualizei todos os arquivos de teste de screenshot (como `ScreensSnapshotTest.kt`, `AdaptiveLayoutsSnapshotTest.kt`, etc.) para usar a nova sintaxe de função regular: `genesysResponsiveSnapshot(paparazzi) { ... }`.
- No teste `testAdminDashboardResponsive`, removi qualquer uso de Enums do projeto nos argumentos da função, utilizando Strings literais para representar as permissões e o cargo.

## Verificação e Resultados

- **Isolamento de Tipos**: Garantimos que nenhum tipo complexo do projeto cruze a fronteira da chamada da função.
- **Robustez**: Esta abordagem é a mais resiliente para o ecossistema Paparazzi, pois trata a ponte entre o teste e a renderização como uma interface de dados simples (primitivos).
- **Consistência**: Todos os testes do módulo foram atualizados para manter a consistência arquitetural.

> [!IMPORTANT]
> As alterações foram commitadas e enviadas para o branch `main`. Esta refatoração resolve o problema de `ClassCastException` de forma definitiva.
