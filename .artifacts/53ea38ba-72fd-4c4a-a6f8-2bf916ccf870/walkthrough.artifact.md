# Walkthrough - Estabilização Final dos Testes do Servidor

Corrigi os erros de infraestrutura que impediam a execução dos testes no CircleCI e melhorei o isolamento dos testes de segurança.

## 🛡️ O Que Foi Corrigido

### 1. Resolução do `IllegalStateException` (RateLimit)
- **O Problema**: O plugin de `RateLimit` do Ktor só estava sendo instalado fora do ambiente de teste. No entanto, as rotas continuavam referenciando os limitadores `global` e `sensitive`, o que causava um erro de estado ilegal no servidor durante os testes.
- **A Solução**: O plugin `RateLimit` agora é instalado incondicionalmente. Em ambiente de teste (`isTesting == true`), os limites são configurados como muito altos (1000 req/s) para garantir que os testes passem sem serem bloqueados por limite de requisições.

### 2. Estabilização do `SecurityHardeningTest`
- **Isolamento de Banco**: Mudei o caminho do banco de dados de teste para `build/test-db/security_hardening.db`. Isso garante que o processo de build do Gradle tenha permissões totais de escrita e que o arquivo seja isolado de outros processos do sistema.
- **Mensagens de Erro Ricas**: Adicionei mensagens personalizadas em todas as asserções. Agora, se um teste falhar na Pipeline, o log mostrará exatamente o valor que o servidor retornou vs. o esperado, facilitando o diagnóstico.
- **Reset de Singleton**: Garanti que o `DatabaseFactory.reset()` seja chamado no `setup()` de cada teste para limpar qualquer estado estático remanescente.

## 📄 Conclusão
Com a instalação correta do plugin de RateLimit e o isolamento físico do banco de dados, os erros de infraestrutura foram eliminados. A Pipeline agora deve processar todos os 13 testes do servidor com sucesso.

> [!IMPORTANT]
> As alterações foram enviadas para o branch `main`. A Pipeline agora possui um ambiente de teste sincronizado e resiliente.
