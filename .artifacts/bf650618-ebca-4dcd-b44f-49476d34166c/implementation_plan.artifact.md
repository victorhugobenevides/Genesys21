# Plano de Implementação - Melhoria da UX: Login e Feedback do Carrinho

Este plano visa resolver frustrações no fluxo de login (solicitação repetitiva e falha no fechamento automático) e a falta de feedback visual ao adicionar itens ao carrinho.

## Problemas Identificados
1.  **Login Repetitivo**: O carrinho solicita login mesmo se o usuário já estiver autenticado no Firebase, pois ele depende do carregamento do perfil completo do banco de dados.
2.  **BottomSheet Persistente**: O diálogo/bottomsheet de login não fecha automaticamente após o sucesso em alguns fluxos.
3.  **Falta de Feedback**: Não há confirmação visual (como um Snackbar) quando um produto, serviço ou doação é adicionado ao carrinho.

## Mudanças Propostas

### 1. Reatividade do Estado de Login
- **PageViewModel**: Alterar `isLoggedIn` para observar diretamente o `authState` (UID do Firebase) em vez de esperar o `UserProfile`. Isso garante que o app reconheça o usuário logado instantaneamente após o retorno do Firebase.

### 2. Feedback Visual com Snackbars
- **PageViewModel**: Implementar um canal de eventos (`SharedFlow`) para mensagens de interface (ex: "Item adicionado ao carrinho!").
- **App.kt**: Adicionar um observador global para este canal de mensagens e exibir Snackbars.

### 3. Ajuste nos Diálogos de Login
- **CartScreen**: Garantir que `showLoginDialog` seja resetado para `false` no callback de sucesso do login social.
- **ServiceBookingScreen**: Aplicar a mesma lógica para o fluxo de agendamento.

## Mudanças Propostas por Arquivo

### Comportamento Global
#### [MODIFY] [PageViewModel.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/PageViewModel.kt)
- Redefinir `isLoggedIn` baseado em `authRepository.authState`.
- Adicionar `_uiMessages` e disparar mensagens em `addToCart`, `addServiceToCart`, etc.

#### [MODIFY] [App.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/App.kt)
- Observar `viewModel.uiMessages` e mostrar no `SnackbarHost`.

### Telas
#### [MODIFY] [CartScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/viewer/CartScreen.kt)
- Fechar diálogo de login no sucesso.

#### [MODIFY] [ServiceBookingScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/viewer/ServiceBookingScreen.kt)
- Fechar diálogo de login no sucesso.

## Plano de Verificação

### Verificação Manual
1.  **Feedback**: Adicionar um produto ao carrinho e verificar se o Snackbar aparece.
2.  **Login**: Estar logado, entrar no carrinho e verificar se ele NÃO pede login novamente ao clicar em finalizar.
3.  **Fechamento**: Fazer login pelo carrinho e verificar se o modal some imediatamente.
