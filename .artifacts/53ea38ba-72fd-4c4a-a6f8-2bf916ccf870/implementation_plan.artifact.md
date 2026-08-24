# Plano de Implementação - Remoção de Inlining para Estabilidade de Classloader

A persistência do `ClassCastException` na pipeline de CI, mesmo com o uso de tipos primitivos, sugere que o `inlining` das funções de utilidade está trazendo referências de classes do *classloader* do JUnit para dentro do contexto do Paparazzi/LayoutLib.

## Análise

Quando uma função é `inline`, o código é copiado para o local da chamada. No nosso caso, o código de `genesysResponsiveSnapshot` (que faz referência a `UserProfile`, `UserRole`, etc.) está sendo inserido dentro de `ScreensSnapshotTest`. Como `ScreensSnapshotTest` é carregado pelo *classloader* do JUnit, essas referências de classe podem estar vinculadas ao *classloader* "errado".

Ao remover o `inline`, forçamos a execução a ocorrer dentro da classe de utilidade, que será carregada pelo Paparazzi no contexto correto de renderização.

## Mudanças Propostas

### [screenshot-tests](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests)

#### [MODIFY] [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- Remover as palavras-chave `inline` e `crossinline` das funções `genesysSnapshot` e `genesysResponsiveSnapshot`.
- Manter o uso de `System.getProperty` para passar dados de mock sem cruzar a fronteira de argumentos com tipos do projeto.
- Garantir que a reconstrução dos objetos de domínio ocorra estritamente dentro da lambda do Koin.

#### [MODIFY] [ScreensSnapshotTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/ScreensSnapshotTest.kt)
- Nenhuma mudança estrutural necessária, mas a remoção do `inline` deve mudar o comportamento do *runtime*.

## Plano de Verificação

### Testes Automatizados
- O objetivo é que a stacktrace pare de apontar para a classe de teste (`ScreensSnapshotTest.kt:218`) e passe a apontar para a classe de utilidade, ou melhor ainda, que o erro desapareça.
- Como o ambiente local não executa os testes devido a erros de infraestrutura do AGP, a validação final será via pipeline de CI.
