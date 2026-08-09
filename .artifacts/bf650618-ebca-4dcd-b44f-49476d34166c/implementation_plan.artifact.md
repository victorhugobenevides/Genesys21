# Plano de Implementação - Correção de Login e Infraestrutura Web

Este plano visa resolver a falha no login do Google, ativar o One Tap e corrigir problemas de carregamento e CORS no ambiente Web (WASM).

## Problemas Identificados
1.  **Race Condition no Carregamento**: O script `firebase-bridge.js` está como `type="module"`, o que o torna assíncrono. O app (`composeApp.js`) tenta chamar as funções do Firebase antes delas serem carregadas, fazendo com que o One Tap e o Login Google falhem.
2.  **Erro de Interpolação**: No Kotlin/Wasm, uma string de log estava com escape incorreto (`\${e.message}`), impedindo a visualização da causa real do erro no console.
3.  **CORS e COOP**: O navegador bloqueia popups de login se os cabeçalhos de segurança não estiverem perfeitamente alinhados entre o Nginx e a aplicação.
4.  **Feedback de Erro no Login**: Erros de autenticação não estão sendo repassados para o Snackbar global.

## Mudanças Propostas

### 1. Infraestrutura Web (CircleCI)
#### [MODIFY] [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- **`index.html`**: Remover `type="module"` do `firebase-bridge.js`. Adicionar `defer` para garantir ordem de execução.
- **`nginx.conf`**:
    - Ativar o uso do `$cors_origin` mapeado para evitar cabeçalhos duplicados.
    - Garantir `Cross-Origin-Opener-Policy: same-origin-allow-popups` em todos os blocos.
    - Adicionar `Cross-Origin-Embedder-Policy: unsafe-none` para facilitar carregamento de recursos externos como o `ui-avatars`.

### 2. Correções de Código (WASM)
#### [MODIFY] [GoogleSignInButton.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/wasmJsMain/kotlin/com/itbenevides/genesys21/presentation/components/auth/GoogleSignInButton.kt)
- Corrigir `println` para mostrar a mensagem de erro real.

#### [MODIFY] [PageViewModel.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/PageViewModel.kt)
- Garantir que o `onError` do `signInWithToken` chame o `handleError`, disparando o Snackbar.

## Plano de Verificação

### Verificação Manual
1.  Acessar `https://victorbenevides.dev` em aba anônima.
2.  Verificar se o log `BRIDGE: Funções carregadas...` aparece no console ANTES do app iniciar.
3.  Testar o botão "Entrar com Google" e validar se o popup abre e fecha com sucesso.
4.  Confirmar que o balão do One Tap aparece.
