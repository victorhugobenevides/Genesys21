# Plano de Implementação - Ativação do Google One Tap Login (Final)

Este plano visa habilitar o Google One Tap Login na versão Web (WASM) do Genesys21, utilizando o ID de Cliente real fornecido pelo usuário.

## Mudanças Propostas

### 1. Configuração do Pipeline (CI/CD)

#### [MODIFY] [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- **Atualizar `index.html`**: Incluir a tag `<script src="https://accounts.google.com/gsi/client" async defer></script>`.
- **Atualizar `firebase-bridge.js`**:
    - Implementar a função `window.firebaseInitializeOneTap` com o Client ID real: `674755208954-6ofmvlcn9birat7ako2banqc9ph1t74s.apps.googleusercontent.com`.
    - Integrar com o Google Identity Services (GSI).
    - Criar o fluxo: `Prompt One Tap -> Credential Response -> Firebase Auth`.

### 2. Recursos Locais (Desenvolvimento)

#### [MODIFY] [index.html](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/webMain/resources/index.html)
- Substituir o Client ID de exemplo pelo real para permitir testes em `localhost`.

### 3. Melhoria na Robustez (WASM)

#### [MODIFY] [WasmAuthRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/wasmJsMain/kotlin/com/itbenevides/genesys21/data/repository/WasmAuthRepository.kt)
- Adicionar logs extras para monitorar a inicialização do One Tap no console do navegador.

## Plano de Verificação

### Verificação Manual
1.  Acessar o site em uma aba anônima.
2.  Verificar se o balão de login do Google aparece no canto superior direito automaticamente.
3.  Confirmar no console do navegador (`F12`) se aparecem logs `BRIDGE: Iniciando One Tap...`.
