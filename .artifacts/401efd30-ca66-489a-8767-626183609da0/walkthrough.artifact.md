# Walkthrough: Correção do Erro 500 na Criação de Pedidos

Este walkthrough descreve as alterações feitas para resolver o erro de integridade referencial que impedia novos usuários (via Google/Social) de realizar pedidos.

## Alterações Realizadas

### 1. Sincronização Automática de Perfil
O `PageViewModel` agora garante que um perfil de usuário exista no servidor SQLite antes de tentar realizar qualquer operação que dependa do `customerId`.

*   **Arquivo:** [PageViewModel.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/PageViewModel.kt)
*   **Lógica:** Ao carregar o perfil (`loadUserProfile`), se o servidor retornar "Não Encontrado", o app agora coleta o e-mail e nome do Firebase e cria o registro automaticamente no servidor.

### 2. Extensão do Repositório de Autenticação
Adicionamos métodos para obter metadados do usuário logado (E-mail e Nome) em todas as plataformas (Android, iOS, WASM).

*   **Interface:** [AuthRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/repository/AuthRepository.kt)
*   **Implementações:** Atualizadas em `AndroidAuthRepository`, `IosAuthRepository`, `WasmAuthRepository` e mocks.

### 3. Melhoria na Diagnóstico de Erros
O repositório Ktor agora captura o corpo da mensagem de erro do servidor para facilitar a depuração no lado do cliente.

*   **Arquivo:** [KtorOrderRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/data/repository/KtorOrderRepository.kt)

## Validação

> [!TIP]
> Para testar, limpe os dados do banco de dados local ou utilize um novo usuário do Google.
> Ao logar, verifique nos logs do servidor se a ação `UPDATE_PROFILE` ou `INSERT INTO users` foi disparada antes da finalização do pedido.

> [!IMPORTANT]
> Esta correção elimina a causa do Erro 500 relacionado ao `customerId`, permitindo que o fluxo de checkout da Stripe prossiga normalmente.
