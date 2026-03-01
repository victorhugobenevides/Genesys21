# Screenshot Tests com Paparazzi

Este módulo contém testes de screenshot usando o [Paparazzi](https://github.com/cashapp/paparazzi) da Square.

## O que é o Paparazzi?

Paparazzi é uma biblioteca de teste de screenshot que permite:
- Gerar imagens PNG dos componentes Compose
- Verificar regressões visuais
- Testar em múltiplos dispositivos sem emulador
- Testar diferentes temas e configurações

## Comandos

### Gerar screenshots de referência
```bash
./gradlew :screenshot-tests:recordPaparazziDebug
```

### Verificar screenshots contra referência
```bash
./gradlew :screenshot-tests:verifyPaparazziDebug
```

### Executar testes unitários (gera snapshots)
```bash
./gradlew :screenshot-tests:testDebugUnitTest
```

## Estrutura

```
screenshot-tests/
├── build.gradle.kts                  # Configuração do módulo
├── src/test/java/.../screenshot/     # Testes de screenshot
│   ├── SimpleScreenshotTest.kt       (3 testes)
│   ├── ComponentScreenshotTest.kt    (5 testes) 
│   ├── ThemeScreenshotTest.kt        (3 testes)
│   ├── WhiteLabelComponentsScreenshotTest.kt  (19 testes) ⭐⭐
│   └── AllUIComponentsScreenshotTest.kt       (30 testes) ⭐⭐⭐ NOVO
└── build/reports/paparazzi/debug/    # Relatórios e imagens geradas
```

## 📊 Estatísticas de Cobertura

| Categoria | Testes | Componentes Cobertos |
|-----------|--------|---------------------|
| Componentes UI do Design System | 30 | 100% (26 componentes) |
| Componentes WhiteLabel | 19 | 100% (12 tipos de componentes) |
| Temas e Estilos | 8 | Todos os 23 temas |
| **Total** | **60 testes** | **100% dos componentes** |

**Screenshots Gerados:** 64 imagens PNG

## Testes Disponíveis

### AllUIComponentsScreenshotTest ⭐⭐⭐ (100% dos Componentes)
Cobertura completa de **todos os componentes UI** do design system:

#### AppBar
- `testGenesysTopAppBar` - Barra superior com título e ações

#### Badges (3 componentes)
- `testGenesysBadge` - Badge padrão com label e cor
- `testGenesysStatusBadge` - Badge de status de pedido (PENDING, PROCESSING, COMPLETED, CANCELLED)
- `testGenesysStockBadge` - Badge de estoque (alto, baixo, esgotado)

#### Buttons (4 componentes)
- `testGenesysLoadingButton` - Botão primário com loading e ícone
- `testGenesysIconButton` - Botão de ícone (Search, Add, Edit, Delete)
- `testGenesysTextButton` - Botão de texto
- `testGenesysFab` - Floating Action Button

#### Cards (2 componentes)
- `testGenesysCard` - Card padrão, clicável e colorido
- `testGenesysStatsCard` - Card de estatísticas (label, valor, cor)

#### Feedback (3 componentes)
- `testGenesysEmptyState` - Estado vazio com ícone, título e descrição
- `testGenesysLoadingIndicator` - Indicador de loading circular
- `testGenesysLoadingOverlay` - Overlay de loading com conteúdo atrás

#### Image (2 componentes)
- `testGenesysAvatar` - Avatar com ícone
- `testGenesysColorCircle` - Círculo colorido para temas

#### Input (4 componentes)
- `testGenesysTextField` - Campo de texto com label, placeholder, erro
- `testGenesysQuantitySelector` - Seletor de quantidade (+/-)
- `testGenesysSearchBar` - Barra de busca com placeholder
- `testGenesysFilterChip` - Chips de filtro selecionáveis

#### Layout (4 componentes)
- `testGenesysColumn` - Layout em coluna
- `testGenesysRow` - Layout em linha
- `testGenesysDivider` - Divisor horizontal
- `testGenesysSpacer` - Espaçador padronizado (Small, Medium, Large)

#### Navigation (2 componentes)
- `testGenesysTabRow` - Abas de navegação com badge
- `testGenesysPagerIndicator` - Indicador de páginas (dots)

#### Text (2 componentes)
- `testGenesysTextStyles` - Estilos de texto (Headline, Title, Body, Label, Error)
- `testGenesysSectionHeader` - Cabeçalho de seção com título, subtítulo e ação

#### Galerias e Temas
- `testAllComponentsGallery` - Galeria com todos os componentes juntos
- `testAllComponentsDarkMode` - Todos os componentes no tema escuro
- `testComponentsInMultipleThemes` - Componentes em 4 temas diferentes (Royal, Ocean, Forest, Sunset)

**Total: 30 testes de componentes UI**

---

### WhiteLabelComponentsScreenshotTest ⭐⭐
Testes completos para **todos os componentes** da WhiteLabelScreen que podem ser adicionados/removidos:

| Componente | Testes | Descrição |
|------------|--------|-----------|
| **Typography** | 2 testes | Texto normal e em destaque (bold, centralizado) |
| **Header** | 1 teste | Cabeçalho com título em destaque |
| **Media** | 3 testes | Full width, side-by-side, e circular |
| **Highlight** | 1 teste | Botão de destaque |
| **StepProcess** | 1 teste | Etapas numeradas do processo |
| **Testimonial** | 1 teste | Depoimento com citação e autor |
| **SocialLinks** | 1 teste | Links sociais (Instagram, WhatsApp, Email) |
| **ProfileHeader** | 1 teste | Foto de perfil com nome e bio |
| **Search** | 1 teste | Barra de busca |
| **CategoryFilter** | 1 teste | Filtro de categorias com produtos |
| **ProductList** | 2 testes | Lista horizontal e vertical de produtos |
| **Image** | 2 testes | Imagem full-width e circular |
| **Composições** | 2 testes | Múltiplos componentes juntos (ROYAL, DARK_MODE) |

**Total: 19 testes de componentes WhiteLabel**

---

### Testes em Múltiplas Resoluções 📱💻

Para testar componentes em diferentes tamanhos de tela, você pode criar múltiplas `@Rule` do Paparazzi:

```kotlin
class MultiDeviceTest {
    // Phone pequeno
    @get:Rule
    val paparazziSmall = Paparazzi(deviceConfig = DeviceConfig.PIXEL_4A)
    
    // Phone padrão
    @get:Rule  
    val paparazziPhone = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)
    
    // Phone grande
    @get:Rule
    val paparazziLarge = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6_PRO)
    
    // Tablet
    @get:Rule
    val paparazziTablet = Paparazzi(deviceConfig = DeviceConfig.NEXUS_10)
    
    @Test
    fun testOnAllDevices() {
        val content = @Composable { /* seu componente */ }
        
        paparazziSmall.snapshot(name = "small_phone") { content() }
        paparazziPhone.snapshot(name = "phone") { content() }
        paparazziLarge.snapshot(name = "large_phone") { content() }
        paparazziTablet.snapshot(name = "tablet") { content() }
    }
}
```

**Dispositivos disponíveis:**
- `DeviceConfig.PIXEL_4A` - 5.8" (1080x2340)
- `DeviceConfig.PIXEL_5` - 6.0" (1080x2340) - Padrão
- `DeviceConfig.PIXEL_6_PRO` - 6.7" (1440x3120)
- `DeviceConfig.NEXUS_7` - 7.0" (1200x1920)
- `DeviceConfig.NEXUS_10` - 10.1" (2560x1600)

| Componente | Testes | Descrição |
|------------|--------|-----------|
| **Typography** | 2 testes | Texto normal e em destaque (bold, centralizado) |
| **Header** | 1 teste | Cabeçalho com título em destaque |
| **Media** | 3 testes | Full width, side-by-side, e circular |
| **Highlight** | 1 teste | Botão de destaque |
| **StepProcess** | 1 teste | Etapas numeradas do processo |
| **Testimonial** | 1 teste | Depoimento com citação e autor |
| **SocialLinks** | 1 teste | Links sociais (Instagram, WhatsApp, Email) |
| **ProfileHeader** | 1 teste | Foto de perfil com nome e bio |
| **Search** | 1 teste | Barra de busca |
| **CategoryFilter** | 1 teste | Filtro de categorias com produtos |
| **ProductList** | 2 testes | Lista horizontal e vertical de produtos |
| **Image** | 2 testes | Imagem full-width e circular |
| **Composições** | 2 testes | Múltiplos componentes juntos (ROYAL, DARK_MODE) |

**Total: 19 testes de componentes da WhiteLabelScreen**

## Configuração

Os testes usam a seguinte configuração:
- **Dispositivo**: Pixel 5
- **Tema**: Material3 Light
- **Rendering**: SHRINK (ajusta ao conteúdo)
- **System UI**: Desabilitada

## CI/CD

Para uso em CI, execute:
```bash
./gradlew :screenshot-tests:verifyPaparazziDebug --info
```

Se houver falhas, os diffs serão gerados em:
```
screenshot-tests/out/failures/
```

## Atualizando Referências

Quando fizer mudanças visuais intencionais:
```bash
./gradlew :screenshot-tests:recordPaparazziDebug
```

Commit as novas imagens em:
```
screenshot-tests/src/test/snapshots/
```

## Boas Práticas

1. **Nomes descritivos**: Use nomes claros para os testes
2. **Isolamento**: Cada teste deve focar em um componente
3. **Estabilidade**: Evite elementos aleatórios (timestamps, etc)
4. **Temas**: Teste em múltiplos temas quando aplicável
5. **Tamanhos**: Use `renderingMode = SHRINK` para componentes individuais

## Troubleshooting

### Erro `UninitializedPropertyAccessException` ou `NoSuchElementException`
Este erro pode ocorrer devido a incompatibilidades entre o Paparazzi 1.3.5 e versões recentes do Android SDK (compileSdk 36+).

**Soluções possíveis:**

1. **Downgrade do compileSdk**: Use compileSdk 34 ou 35 no módulo screenshot-tests
2. **Atualizar Paparazzi**: Quando disponível, use versão 1.3.6+ ou 2.0.0+
3. **Verificar ambiente**: Certifique-se de que o Android SDK está corretamente configurado

**Configuração alternativa no build.gradle.kts:**
```kotlin
android {
    compileSdk = 34  // Versão mais compatível
    // ... resto da configuração
}
```

### Erro de fontes
Se encontrar erros relacionados a fontes, adicione ao `build.gradle.kts`:
```kotlin
paparazzi {
    renderingMode = SessionParams.RenderingMode.SHRINK
}
```

### Diferenças de pixel
Pequenas diferenças podem ocorrer entre ambientes. Use tolerância:
```kotlin
@get:Rule
val paparazzi = Paparazzi(
    maxPercentDifference = 1.0  // 1% de tolerância
)
```

## Links Úteis

- [Documentação Paparazzi](https://cashapp.github.io/paparazzi/)
- [Repositório GitHub](https://github.com/cashapp/paparazzi)
- [Samples](https://github.com/cashapp/paparazzi/tree/master/sample)
