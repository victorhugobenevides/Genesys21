# Plan: Cart Management Consolidation (Spec 003)

Este plano visa finalizar a gestão de carrinho multi-plataforma, garantindo persistência local no Android, sincronização reativa e um fluxo de merge impecável entre visitantes (guest) e usuários logados.

## User Review Required

> [!NOTE]
> A sincronização com o servidor exige que o backend suporte o header `X-Cart-Session-Id` para usuários não logados. Já verifiquei que o `KtorCartRepository` e o `cartRoutes` no servidor tratam isso.

## Proposed Changes

### [Shared - Data Layer]
#### [MODIFY] [AndroidCartRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/androidMain/kotlin/com/itbenevides/genesys21/data/repository/AndroidCartRepository.kt)
- Verificar se a implementação do DataStore está correta e se o `loadInitialCart` do `BaseCartRepository` está sendo chamado.

#### [MODIFY] [BaseCartRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/data/repository/BaseCartRepository.kt)
- Melhorar a lógica de `mergeWithServer` para garantir que duplicatas de produtos sejam somadas e serviços sejam preservados com seus agendamentos.

### [UI Layer - Presentation]
#### [MODIFY] [PageViewModel.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/PageViewModel.kt)
- Garantir que `loadInitialCart()` seja disparado no `init`.
- Chamar `mergeWithServer()` imediatamente após o sucesso do login.

#### [MODIFY] [CartScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/viewer/CartScreen.kt)
- Integrar o `GenesysQuantitySelector` de forma definitiva nas linhas de produtos.
- Adicionar feedback visual (Snackbar) ao falhar na sincronização.

### [Technical Specs]
#### [MODIFY] [tasks.md](file:///Users/victorben/AndroidStudioProjects/genesys21/.specify/specs/003-cart-management/tasks.md)
- Marcar as tarefas concluídas.

## Verification Plan

### Automated Tests
- Executar `:shared:test` focando no `CartRepositoryTest`.
- Criar um novo teste unitário para o cenário de merge: Local (Product A x 2) + Server (Product A x 1) = Local (Product A x 3).

### Manual Verification
- **Web (Wasm)**: Adicionar item como visitante, atualizar a página e verificar se o carrinho persiste.
- **Login Flow**: Adicionar item como visitante, fazer login com Google e verificar se o item antigo foi "trazido" para a conta.
- **Android**: Validar a persistência via DataStore entre reinicializações do app.
