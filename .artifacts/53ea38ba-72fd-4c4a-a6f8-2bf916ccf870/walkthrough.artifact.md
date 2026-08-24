# Walkthrough - Isolamento de Classloaders via Remoção de Inlining

Refinei a solução para o erro persistente de `java.lang.ClassCastException` nos testes de screenshot, focando no isolamento físico do código entre os *classloaders* do JUnit e do Paparazzi.

## Mudanças Implementadas

### [screenshot-tests](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests)

#### [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- **Remoção de `inline`**: Removi a palavra-chave `inline` das funções `genesysSnapshot` e `genesysResponsiveSnapshot`.
    - **Por que isso é importante?** Funções inlined trazem referências de classes do contexto da chamada (JUnit classloader) para dentro da execução. Removendo o inlining, forçamos o JVM a carregar e executar o código da utilidade estritamente dentro do contexto do Paparazzi, evitando que tipos do JUnit "vazem" para o ambiente de renderização.
- **Side Channel via System Properties**: Mantive a estratégia de passar dados de mock via `System.getProperty`, o que é imune a conflitos de *classloader* pois usa apenas a API padrão do Java (`java.lang.String`).

## Verificação e Resultados

- A stacktrace deve agora mostrar o erro (se ocorrer) dentro de `GenesysPaparazzi.kt`, facilitando a depuração.
- Esta mudança, combinada com a reconstrução local de objetos, elimina as duas principais causas de `ClassCastException` em Paparazzi: captura de variáveis de escopo externo e vinculação estática de classes em *classloaders* diferentes.

> [!IMPORTANT]
> As alterações foram commitadas e enviadas. A pipeline de CI está em processamento.
