# Plano de Correção: Sincronização de Variáveis e Fluxo de Deploy (CircleCI)

Com base na lista de variáveis de ambiente fornecida, identifiquei que o job de `deploy` no CircleCI está tentando realizar conexões SSH sem configurar a chave privada e utilizando nomes de variáveis inconsistentes com o que está cadastrado.

## Problemas Identificados
1. **Configuração de SSH ausente:** O job `deploy` tenta rodar `ssh` sem carregar a chave `OCI_SSH_KEY` ou `oracle_key`.
2. **Nomes de Variáveis:** O script usa `$SERVER_IP`, mas a variável correta é `OCI_HOST`. O usuário SSH está como `ubuntu` fixo, mas temos `OCI_USERNAME`.
3. **Escapes de Variáveis:** No comando SSH, algumas variáveis precisam ser expandidas localmente (no CircleCI) e outras remotamente (no servidor).

## Mudanças Propostas

### CircleCI Configuration
#### [MODIFY] [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- **Job `deploy`**:
    - Adicionar um passo para configurar a chave SSH a partir da variável `OCI_SSH_KEY`.
    - Atualizar o comando SSH para usar `${OCI_USERNAME}@${OCI_HOST}`.
    - Utilizar a chave configurada com `-i ~/.ssh/id_rsa`.
    - Garantir que as variáveis do OCIR sejam passadas corretamente para o ambiente remoto.

## Plano de Execução
1. Modificar o job `deploy` no `.circleci/config.yml`.
2. Adicionar a lógica de setup da chave SSH (similar ao que existia em versões anteriores).
3. Fazer commit e push.

## Verificação
- Acompanhar a pipeline no CircleCI. O job `deploy` deve agora conseguir se conectar ao servidor e realizar o pull das imagens.
