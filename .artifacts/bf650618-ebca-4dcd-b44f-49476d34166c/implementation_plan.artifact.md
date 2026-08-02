# Plano de Implementação - Correção de Build e Finalização de Staging

Este plano visa corrigir a falha no pipeline do CircleCI causada pela remoção acidental das etapas de decodificação de segredos e finalizar a infraestrutura de Staging.

## 1. Correção do Pipeline (CircleCI)

### Problema
O build falhou com `File google-services.json is missing`. Isso ocorreu porque as etapas que transformam as variáveis de ambiente em arquivos físicos foram removidas na última simplificação do `config.yml`.

### Solução
- Restaurar o passo `Decode Google Services JSON (Android)` no job `test-and-validate` e no job `deploy`.
- Restaurar o passo `Decode Firebase Admin JSON (Server)` no job `test-and-validate` e no job `deploy`.
- Garantir que os arquivos sejam colocados nos caminhos esperados pelo Gradle (`composeApp/google-services.json` e raiz do projeto para o servidor).

## 2. Refinamento de Staging

### Nginx e Rede
- O Nginx agora está configurado para gerenciar ambos os domínios (`victorbenevides.dev` e `staging.victorbenevides.dev`).
- O Staging usará um banco de dados separado `data-staging/` para isolamento total.

## Mudanças Propostas por Arquivo

### Pipeline
#### [MODIFY] [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- Adicionar os passos de decodificação de segredos (`google-services.json` e `firebase-adminsdk.json`).
- Adicionar `store_test_results` para melhor visibilidade de falhas no dashboard do CircleCI.

## Plano de Verificação

### Testes Automatizados
- O CircleCI deve passar por todas as etapas: `Run Unit Tests`, `Build Distribution` e `Smoke Test`.

### Verificação Manual
- Validar se `https://staging.victorbenevides.dev` carrega a versão correta.
- Validar se o site oficial continua operando normalmente.
