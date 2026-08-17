# Walkthrough: Correção dos Testes de Screenshot

Resolvi as falhas nos testes de regressão visual atacando três frentes principais: padronização de nomenclatura de arquivos, determinismo de datas e sincronização de dados de templates.

## Alterações Realizadas

### 1. Infraestrutura do Paparazzi
- **Arquivo**: [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- **Mudança**: Alterei os nomes das configurações de `Phone`, `Tablet` e `Desktop` para `phone`, `tablet` e `desktop`.
- **Motivo**: Os arquivos "goldens" no repositório utilizam sufixos minúsculos. Em sistemas Linux (como o CircleCI), a diferenciação entre maiúsculas e minúsculas impedia que os testes encontrassem as imagens de referência.

### 2. Determinismo de Datas no Carrinho
- **Arquivo**: [CartScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/viewer/CartScreen.kt)
- **Mudança**: Adicionei suporte para injeção de `TimeZone` nos componentes `CartContent` e `ModernCartItemRow`.
- **Arquivo**: [AdaptiveLayoutsSnapshotTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/AdaptiveLayoutsSnapshotTest.kt)
- **Mudança**: Configurei o teste para usar `TimeZone.UTC`.
- **Motivo**: Evitar variações no texto de datas (ex: 31/12 vs 01/01) dependendo do fuso horário da máquina que executa o teste.

### 3. Sincronização de Templates
- **Arquivo**: [TemplateShowcaseScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/editor/TemplateShowcaseScreen.kt)
- **Mudança**: Atualizei a lista de templates para usar IDs válidos que existem no `PageTemplateRegistry`.
- **Motivo**: O catálogo estava tentando renderizar templates com IDs antigos/inexistentes, resultando em telas vazias nos snapshots.

## Resultados da Validação

- [x] Verificação de sintaxe via `analyze_file` em todos os arquivos modificados.
- [x] Nomenclatura de snapshots agora alinhada com os artefatos do repositório.
- [x] Renderização de datas agora independente do ambiente.
