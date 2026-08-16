# Plan: CI Secrets Restoration & Pipeline Fix

Este plano visa restaurar a lógica de injeção de segredos (Firebase) no pipeline do CircleCI, permitindo que o build e os testes utilizem as credenciais reais configuradas no ambiente, ao mesmo tempo que mantém um fallback resiliente para ambientes de desenvolvimento ou forks.

## User Review Required

> [!IMPORTANT]
> Descobri que o pipeline possuía uma lógica de decodificação de variáveis de ambiente (`GOOGLE_SERVICES_JSON_ANDROID`) que foi removida no refactor anterior. Vou restaurá-la para garantir que o build de produção/staging utilize os dados reais.

## Proposed Changes

### [CI/CD - Infrastructure]
#### [MODIFY] [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- Restaurar o passo `Decode Secrets` nos jobs `test-and-validate` e `build-and-push`.
- A lógica tentará decodificar a Base64 das variáveis de ambiente. Se falhar ou estiverem vazias, criará o arquivo dummy com o `package_name` correto (`com.itbenevides.genesys21`).
- Garantir que o `firebase-adminsdk.json` também seja restaurado a partir da variável `FIREBASE_ADMIN_JSON`.

## Verification Plan

### Automated Tests
- O sucesso será confirmado pela conclusão verde do job `test-and-validate` no CircleCI após o push.

### Manual Verification
- Acompanhar os logs do job "Prepare CI Environment" para ver se ele detectou e usou a variável de ambiente real.
