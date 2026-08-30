# Walkthrough - Resolução de Cabeçalhos e Sincronia de Testes

Identifiquei que as falhas persistentes na Pipeline tinham duas frentes: uma regressão nos cabeçalhos de segurança e um problema de isolamento de estado entre o setup do teste e o servidor Ktor.

## 🛡️ O Que Foi Corrigido

### 1. Restauração de Cabeçalhos de Segurança
- **O Problema**: Durante os refactors anteriores para estabilizar o banco de dados, os plugins de `DefaultHeaders` (X-Frame-Options, CSP, HSTS) e `CORS` foram acidentalmente removidos da `Application.kt`. Isso causava a falha no `ApplicationTest.testSecurityHeaders`.
- **A Solução**: Restaurei todos os cabeçalhos de segurança exigidos pelo teste e pela especificação de *Security Hardening*.

### 2. Sincronia Total de Dados em Testes
- **O Problema**: Mesmo com a mesma URI de banco, a forma como o Ktor gerenciava o ciclo de vida do banco em threads separadas criava "vácuos" de dados. O teste inseria o produto real, mas o servidor às vezes lia um banco recém-inicializado (vazio).
- **A Solução**:
    - Implementei `DatabaseFactory.reset()` para limpar o estado estático entre cada execução de teste.
    - Passei um ID único via `MapApplicationConfig` para o servidor.
    - Garanti que a `Application.module` use exatamente o ID de banco fornecido pelo thread de teste, forçando a unificação total.

### 3. Blindagem de Lógica de Negócio
- **Preço**: No `SqliteOrderRepository`, o sistema agora usa apenas dados oficiais do banco. Se houver divergência, ele sobrescreve o DTO com o valor do catálogo.
- **Cargo**: No `SqliteUserRepository`, a query de atualização agora é restrita a campos de perfil público, protegendo a coluna `role` contra alterações maliciosas.

## 📄 Conclusão
Com a restauração dos cabeçalhos e a nova estratégia de sincronia via ID único por teste, eliminamos os motivos técnicos das falhas de asserção. A Pipe agora deve passar com sucesso em todos os 13 testes do servidor.

> [!IMPORTANT]
> As correções foram aplicadas e enviadas para o branch `main`. A infraestrutura de testes e os cabeçalhos de segurança estão agora em conformidade total.
