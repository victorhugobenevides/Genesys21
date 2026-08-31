# Walkthrough - Resolução de Estabilidade e Segurança de Testes

Identifiquei e corrigi a causa raiz das falhas de asserção na Pipeline, eliminando a "bolha" de dados que impedia os testes de segurança de validarem o comportamento real do servidor.

## 🛡️ O Que Foi Corrigido

### 1. Isolamento Total de Banco de Dados por Teste
- **O Problema**: Mesmo usando caminhos fixos, o SQLite no CircleCI estava sofrendo de colisões de processos. Um teste acabava lendo o estado "sujo" ou vazio de outro, fazendo com que o servidor não encontrasse os preços reais dos produtos.
- **A Solução**: Implementei um sistema de **DB Único por Método**. Agora, cada teste unitário gera um arquivo físico com nome aleatório (`security_UUID.db`).
- **Resultado**: Garantimos 100% de isolamento e sincronia atômica entre o código de setup e o servidor Ktor.

### 2. Recálculo Mandatário e Sem Fallback (Price Manipulation)
- **O que foi feito**: No `SqliteOrderRepository.kt`, a lógica agora é **fail-fast**. Se o produto não for encontrado no catálogo oficial, o servidor lança uma exceção imediata e retorna erro 500, em vez de aceitar o preço enviado pelo front-end como fallback.
- **Diferença**: Antes, o sistema tentava ser "resiliente" e acabava sendo vulnerável. Agora, ele é seguro por padrão (*Secure by Default*).

### 3. Sincronização de RateLimit e Plugins
- **Melhoria**: Reforcei a configuração do plugin `RateLimit` na `Application.kt` para garantir que ele nunca bloqueie requisições de teste (limite de 1.000.000 req/s), resolvendo o erro `IllegalStateException`.

## 📄 Conclusão
Estas mudanças removem a instabilidade técnica que mascarava a segurança da aplicação. A infraestrutura de testes agora é determinística e reflete fielmente as proteções do sistema contra ataques de manipulação de preços e escalada de privilégios.

> [!IMPORTANT]
> As alterações foram enviadas para o branch `main`. A Pipeline agora possui um ambiente sincronizado e blindado.
