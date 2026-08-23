# Walkthrough: Restauração de Sintaxe CI

Restaurei o arquivo `.circleci/config.yml` para o padrão de sintaxe que funcionava anteriormente, garantindo que as variáveis de ambiente sejam expandidas corretamente pelo shell durante a execução.

## Alterações Realizadas

### CircleCI Configuration
- **Arquivo**: [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- **Restauração de Escapes**: Voltei a usar `\$` para variáveis como `\$OCI_NAMESPACE` e `\$OCI_USERNAME`.
- **Gerenciamento de Memória**: Removi o bloco `environment:` do job (que poderia estar causando conflitos) e passei a definir o `GRADLE_OPTS` via `export` diretamente no início dos comandos de build.
- **Robustez no Deploy**: Apliquei a mesma restauração de sintaxe no job de deploy remoto via SSH, mantendo a correção do Heredoc (`\<<`).

## Resultados da Validação

- [x] Sintaxe `\$` restaurada (padrão que funcionava).
- [x] Configurações de memória isoladas no escopo do script.
- [x] Push realizado para o branch `main` (`b1b8aa3`).

> [!NOTE]
> No CircleCI, quando uma variável é definida no painel de controle do projeto e usada num comando `run:`, o caractere `\` antes do `$` avisa ao CircleCI para não tentar trocar o nome pelo valor agora. Isso deixa para o **Bash** da máquina fazer a troca no momento da execução, o que é mais confiável para segredos e variáveis globais.
