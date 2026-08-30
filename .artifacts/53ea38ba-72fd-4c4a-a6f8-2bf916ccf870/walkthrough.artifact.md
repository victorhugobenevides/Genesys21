# Walkthrough - Unificação de Banco de Dados de Teste

Resolvi a falha persistente nos testes de segurança unificando a conexão do banco de dados SQLite entre o thread de teste e o servidor Ktor.

## 🛡️ O Que Foi Corrigido (A Causa Raiz Real)

### 1. Conexões Divergentes
- **O Problema**: Tanto o `SecurityHardeningTest.kt` quanto a `Application.module` estavam gerando nomes de banco de dados aleatórios usando `System.nanoTime()`.
- **A Consequência**: Mesmo usando `cache=shared`, o SQLite só compartilha o estado se o nome do banco for **idêntico**. Como os nomes eram diferentes, o setup do teste escrevia em um banco e o servidor Ktor lia de outro (vazio), causando erros de "Pedido não encontrado" e falhas de validação.

### 2. URI de Teste Constante
- **A Solução**: Defini uma URI constante para o ambiente de teste em ambos os lados: `jdbc:sqlite:file:genesys_test_db?mode=memory&cache=shared`.
- **Resultado**: Agora, o servidor Ktor detecta se o banco já foi inicializado pelo setup do teste e reutiliza a **mesma conexão exata**, garantindo que o catálogo de preços oficial e os usuários criados no teste estejam visíveis para as rotas da API.

### 3. Proteção contra Re-inicialização
- **Melhoria**: Blindei a `Application.module` para pular a inicialização do banco de dados se o `DatabaseFactory` já possuir uma conexão ativa. Isso impede que o Ktor "limpe" os dados inseridos pelo teste antes de executar as requisições.

## 📄 Conclusão
A infraestrutura de testes de segurança agora está perfeitamente sincronizada. O ambiente é, de fato, o mesmo para o teste e para o servidor, eliminando as condições de corrida e garantindo que as regras de recálculo de preço e bloqueio de cargo sejam validadas corretamente.

> [!IMPORTANT]
> As correções foram enviadas para o branch `main`. A Pipeline deve agora processar os testes de segurança com sucesso.
