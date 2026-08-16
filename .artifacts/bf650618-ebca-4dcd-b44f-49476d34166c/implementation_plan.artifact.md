# Plan: CI Pipeline Hardening & Secret Management

Este plano visa corrigir as falhas no pipeline de CI (CircleCI) causadas pela ausência de arquivos de configuração do Firebase (`google-services.json`) que estão no `.gitignore`. Implementaremos a injeção de arquivos dummy para satisfazer os plugins do Gradle durante os testes e build.

## User Review Required

> [!IMPORTANT]
> Os arquivos gerados no CI serão **dummy** (placeholders). Eles permitem que o build complete e os testes unitários rodem, mas não permitem conexões reais com o Firebase. Para o deploy de produção, as chaves reais devem ser injetadas via Secrets do CircleCI (conforme já previsto no plano de deploy anterior).

## Proposed Changes

### [CI/CD - Infrastructure]
#### [MODIFY] [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- Adicionar um passo `Prepare Environment` antes dos testes.
- Gerar um `google-services.json` mínimo em `composeApp/`.
- Gerar um `firebase-adminsdk.json` mínimo em `server/`.
- Criar o diretório `server/data/` para o SQLite.

### [Backend - Server]
#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Melhorar a resiliência da inicialização do Firebase Admin para não crashar o servidor se o arquivo JSON for inválido/dummy durante os testes.

## Verification Plan

### Automated Tests
- O sucesso será confirmado quando o próximo push para a branch `main` completar o job `test-and-validate` no CircleCI.

### Manual Verification
- Verificar os logs do CircleCI para garantir que os arquivos dummy foram criados nos caminhos corretos.
