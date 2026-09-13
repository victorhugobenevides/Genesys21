# Tasks: Finalização e Melhoria do Editor White Label (v2.0) 🎨🛠️

- [ ] **Fase 1: Documentação e Conexões**
    - [ ] Atualizar especificação em `.specify/specs/002-page-editor/spec.md`
    - [ ] Ativar editores de `ProfileHeader` e `SocialLinks` em `WhiteLabelContent.kt`

- [ ] **Fase 2: Novos Editores de Componentes**
    - [ ] Criar `HeroComponentEditor.kt`
    - [ ] Criar `BenefitsComponentEditor.kt`
    - [ ] Criar `TestimonialComponentEditor.kt`
    - [ ] Criar `ValuedActionComponentEditor.kt`
    - [ ] Criar `SpacerComponentEditor.kt`
    - [ ] Criar `DividerComponentEditor.kt`

- [ ] **Fase 3: Inteligência Artificial (Magic Edit)**
    - [ ] Adicionar `refineComponentContent` em `PageAIGeneratorService.kt`
    - [ ] Criar `AiRefinementDialog.kt`
    - [ ] Integrar botão de "Mágica" nos editores de texto (`Header`, `Text`, `Hero`)

- [ ] **Fase 4: Validação Final**
    - [ ] Testar fluxo completo de edição no Android e Web (Wasm)
    - [ ] Verificar persistência dos drafts com os novos componentes
