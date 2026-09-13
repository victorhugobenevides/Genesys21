# Finalização e Melhoria do Editor White Label (v2.0) 🎨🛠️

Este plano visa completar a especificação do Editor de Páginas, garantindo que todos os componentes do sistema sejam editáveis e integrando inteligência artificial para auxiliar na criação de conteúdo.

## User Review Required

> [!IMPORTANT]
> Atualmente, diversos componentes (como `ProfileHeader`, `Hero`, `SocialLinks`) aparecem como "não editáveis" na interface. Este plano irá ativar os editores existentes e criar os novos.

## Mudanças Propostas

### 1. Atualização da Especificação [.specify/specs/002-page-editor/spec.md]
- Expandir a lista de componentes suportados para incluir o catálogo moderno completo.
- Adicionar a seção de **IA Assistida (Magic Edit)**.

### 2. Integração de Editores Existentes
#### [MODIFY] [WhiteLabelContent.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/viewer/WhiteLabelContent.kt)
- Adicionar os casos `is PageComponent.ProfileHeader` e `is PageComponent.SocialLinks` no `when` do `ComponentEditorUI`.

### 3. Criação de Novos Editores Avançados
Criar os seguintes arquivos em `composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/editor/`:
- **[NEW] HeroComponentEditor.kt**: Banner principal com suporte a upload de imagem.
- **[NEW] BenefitsComponentEditor.kt**: Lista de diferenciais com ícones selecionáveis.
- **[NEW] TestimonialComponentEditor.kt**: Editor de depoimentos e avaliações.
- **[NEW] ValuedActionComponentEditor.kt**: Editor de componentes de doação/contribuição.
- **[NEW] SpacerComponentEditor.kt**: Controle de altura de espaçamentos.
- **[NEW] DividerComponentEditor.kt**: Controle de estilo de linhas divisórias.

### 4. IA Assistida (Magic Edit) 🪄
#### [MODIFY] [PageAIGeneratorService.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/service/PageAIGeneratorService.kt)
- Adicionar método `refineComponentContent(component, prompt)` para gerar textos específicos.
- Utilizar a chave Gemini fornecida pelo usuário.

#### [NEW] AiRefinementDialog.kt
- Modal para o usuário descrever o que quer (ex: "Crie uma bio para arquiteto") e ver a sugestão da IA antes de aplicar ao componente.

## Verification Plan

### Teste de Interface
- Selecionar um componente de Perfil e verificar se o painel de edição abre.
- Selecionar um Banner Hero e verificar se é possível trocar título e imagem.

### Teste de IA
- Clicar no ícone de "Mágica" em um campo de texto e verificar se a sugestão é gerada e aplicada.
