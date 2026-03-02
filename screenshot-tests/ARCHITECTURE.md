# Screenshot Tests - Arquitetura Organizada

## 📁 Estrutura de Pastas

```
screenshot-tests/src/test/java/com/itbenevides/genesys21/screenshot/
├── components/              # Testes de componentes UI individuais
│   ├── buttons/
│   │   └── ButtonScreenshotTest.kt
│   ├── inputs/
│   │   └── InputScreenshotTest.kt
│   ├── feedback/
│   │   └── FeedbackScreenshotTest.kt
│   └── layout/
│       └── LayoutScreenshotTest.kt
├── integration/             # Testes de integração (múltiplos componentes)
│   ├── ComponentGalleryScreenshotTest.kt
│   └── ComponentStatesScreenshotTest.kt
├── pages/                   # Testes de páginas completas
│   ├── FullPageScreenshotTest.kt
│   └── PublicPageScreenshotTest.kt
├── themes/                  # Testes de temas
│   └── ThemeVariationsScreenshotTest.kt
├── edgecases/               # Edge cases e estados extremos
│   └── EdgeCasesScreenshotTest.kt
├── responsive/              # Testes responsivos
│   └── ResponsiveScreenshotTest.kt
└── base/                    # Classes base e utilitários
    └── PaparazziTestBase.kt
```

## 🎯 Convenções de Nomenclatura

### Arquivos de Teste
- Sufixo `ScreenshotTest.kt` para todos os arquivos
- Nome descritivo do que está sendo testado
- Exemplo: `ButtonScreenshotTest.kt`, `ThemeVariationsScreenshotTest.kt`

### Métodos de Teste
- Prefixo `test` + descrição clara em inglês
- Formato: `test{Component}{Variation}{State}`
- Exemplos:
  - `testButtonAllStates`
  - `testTextFieldErrorState`
  - `testCardElevatedVariant`

### Nomes de Snapshots
- snake_case minúsculo
- Formato: `{component}_{variation}_{state}`
- Exemplos:
  - `button_all_states`
  - `textfield_error_variant`
  - `card_elevated_theme_royal`

## 📋 Padrões de Código

### Imports
1. Android/Compose (alfabético)
2. Domain/Model
3. UI Components (agrupados por categoria)
4. Theme
5. Test libraries
6. Static imports

### Estrutura de Teste
```kotlin
class ComponentScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi()

    // Helper methods
    private fun createTestData() = ...

    @Test
    fun testComponentAllStates() {
        paparazzi.snapshot(name = "component_all_states") {
            TestTheme {
                // Test implementation
            }
        }
    }
}
```

## 🚀 Comandos

```bash
# Executar todos os testes
./gradlew :screenshot-tests:testDebugUnitTest

# Executar testes de uma categoria
./gradlew :screenshot-tests:testDebugUnitTest --tests "*components*"

# Gerar relatório
open screenshot-tests/build/reports/paparazzi/debug/index.html
```
