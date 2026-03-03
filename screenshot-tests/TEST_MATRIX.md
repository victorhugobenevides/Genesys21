# Matriz de Testes - Screenshot Tests

## Visão Geral

Esta matriz documenta todos os testes de screenshot implementados e suas combinações.

## Categorias de Testes

### 1. Acessibilidade (♿)

| Componente | Font Scales | Temas | Total |
|------------|-------------|-------|-------|
| Buttons | 4 | 1 | 4 |
| TextField | 4 | 1 | 4 |
| Cards | 4 | 1 | 4 |
| List Items | 4 | 1 | 4 |
| Navigation | 4 | 1 | 4 |
| Dialogs | 4 | 1 | 4 |
| Chips | 4 | 1 | 4 |
| **Subtotal** | | | **28** |

**Font Scales:** Small (0.85x), Default (1.0x), Large (1.15x), XLarge (1.3x)

### 2. Estados (🔄)

| Componente | Estados Testados | Total |
|------------|-----------------|-------|
| Button | enabled, disabled, loading | 3 |
| TextField | empty, filled, error, disabled | 4 |
| Checkbox | unchecked, checked, disabled_unchecked, disabled_checked | 4 |
| Switch | off, on, disabled_off, disabled_on | 4 |
| Card | default, outlined, elevated | 3 |
| Chip | unselected, selected, disabled_unselected, disabled_selected | 4 |
| Progress | indeterminate, determinate_50, determinate_100 | 3 |
| **Subtotal** | | **25** |

### 3. Multi-Resolução (📱💻)

| Layout/Tela | Dispositivos | Total |
|-------------|--------------|-------|
| Product List | Small Phone, Large Phone, Tablet | 3 |
| Product Grid | Small Phone, Large Phone, Tablet | 3 |
| Navigation | Small Phone, Large Phone, Tablet | 3 |
| Form Layout | Small Phone, Large Phone, Tablet | 3 |
| Detail Screen | Small Phone, Large Phone, Tablet | 3 |
| Adaptive Columns | Phone, Tablet | 2 |
| Adaptive Navigation | Phone, Tablet | 2 |
| Adaptive Side Panel | Phone, Tablet | 2 |
| **Subtotal** | | **21** |

**Dispositivos:**
- Small Phone: Pixel 4A (5.8", 1080x2340)
- Standard Phone: Pixel 5 (6.0", 1080x2340)
- Large Phone: Pixel 6 Pro (6.7", 1440x3120)
- Tablet: Nexus 10 (10.1", 2560x1600)

### 4. Edge Cases (⚠️)

| Caso | Descrição | Total |
|------|-------------|-------|
| Long Text | Overflow, wrapping | 1 |
| Empty States | Listas vazias, no content | 1 |
| Very Large Lists | 50+ items | 1 |
| Single Character | Inputs mínimos | 1 |
| Special Characters | Unicode, emojis, acentos | 1 |
| Zero Values | Valores zerados | 1 |
| Max Values | Valores máximos, 999+ | 1 |
| **Subtotal** | | **7** |

### 5. Componentes do Design System (🎨)

*Já documentado no README.md principal*
- 30 testes de componentes UI
- 19 testes de componentes WhiteLabel
- 8 testes de temas

**Subtotal:** **57 testes**

## Total Geral

| Categoria | Testes |
|-----------|--------|
| Acessibilidade | 28 |
| Estados | 25 |
| Multi-Resolução | 21 |
| Edge Cases | 7 |
| Componentes + Temas | 57 |
| **TOTAL** | **138+** |

## Cobertura por Dimensão

### Font Scales
- ✓ 0.85x (Small)
- ✓ 1.0x (Default)
- ✓ 1.15x (Large)
- ✓ 1.3x (XLarge)

### Temas
- ✓ Royal
- ✓ Ocean
- ✓ Forest
- ✓ Sunset
- ✓ Dark Mode
- ✓ Todos os 23 temas (via AllUIComponentsScreenshotTest)

### Dispositivos
- ✓ Small Phone (Pixel 4A)
- ✓ Standard Phone (Pixel 5)
- ✓ Large Phone (Pixel 6 Pro)
- ✓ Small Tablet (Nexus 7)
- ✓ Large Tablet (Nexus 10)

### Estados de Componentes
- ✓ Enabled
- ✓ Disabled
- ✓ Loading
- ✓ Error
- ✓ Empty
- ✓ Filled
- ✓ Selected/Unselected
- ✓ Checked/Unchecked

## Próximas Expansões

### Sugeridas
1. **Animações**: Capturar estados de animação
2. **Orientação**: Landscape vs Portrait
3. **RTL**: Suporte a idiomas da direita para esquerda
4. **Densidade**: LDPI, MDPI, HDPI, XHDPI, etc.
5. **Modo Noturno**: Forçar sistema em dark mode vs light mode

### Em Consideração
- Testes de performance visual (tempo de renderização)
- Comparação entre versões (diff entre releases)
- Integração com ferramentas de design (Figma)

## Como Adicionar Novos Testes

1. Identifique a categoria apropriada
2. Crie o teste seguindo o padrão existente
3. Use base classes (`EnhancedPaparazziTestBase`) quando possível
4. Documente na matriz acima
5. Execute `./scripts/generate_report.sh` para atualizar relatório

## Manutenção

- **Frequência**: Atualizar snapshots a cada mudança visual intencional
- **Review**: Sempre revisar diffs antes de atualizar referências
- **CI/CD**: Executar em todas as PRs
- **Documentação**: Manter esta matriz atualizada
