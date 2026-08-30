# Walkthrough - Estabilização Crítica de Testes de Segurança

Reforcei a proteção contra ataques de *Mass Assignment* e *Price Manipulation*, corrigindo falhas de infraestrutura que impediam a execução estável dos testes de segurança no CircleCI.

## 🛡️ Melhorias e Reforços de Segurança

### 1. Rigor Total no Cadastro de Usuários (Mass Assignment)
- **O que foi feito**: Modifiquei o `SqliteUserRepository.kt` para forçar o cargo `CUSTOMER` diretamente no bloco de `insert`.
- **Diferença**: Antes, o backend usava um objeto "processado" que poderia ser burlado se a lógica de pré-processamento falhasse. Agora, a regra é aplicada no momento da escrita no banco: qualquer novo usuário (que não seja o administrador oficial via regra de dogma) **nasce obrigatoriamente como CUSTOMER**.

### 2. Sincronização e Isolamento de Banco de Dados
- **O que foi feito**: Atualizei o `SecurityHardeningTest.kt` para usar `DatabaseFactory.dbQuery` em todo o setup de dados (criação de lojas e produtos).
- **Por que?**: Misturar `transaction { }` (bloqueante) com `newSuspendedTransaction` (corrotina) no mesmo banco de dados SQLite pode causar atrasos de sincronização, fazendo com que um pedido pareça "não encontrado" logo após ser criado. Agora todo o ciclo de vida do dado usa o mesmo motor de corrotinas.

### 3. Diagnóstico de Falhas em CI
- **O que foi feito**: Melhorei as mensagens de erro dos testes, incluindo o corpo da resposta e o valor real dos cargos em caso de falha.
- **Resultado**: Se a pipeline falhar novamente, teremos logs claros do que o servidor está retornando exatamente.

## 📄 Conclusão
Estas mudanças tornam o sistema Genesys21 mais seguro por padrão (*Secure by Default*) e garantem que a pipeline de integração contínua seja um termômetro confiável para a saúde do projeto.

> [!IMPORTANT]
> As correções foram aplicadas e o código já foi enviado para o branch `main`. A pipeline deve agora apresentar o status "Verde" e os testes de segurança devem passar com sucesso.
