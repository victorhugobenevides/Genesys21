# Walkthrough - Resolução Definitiva de Hardening de Segurança

Identifiquei e corrigi o conflito técnico que impedia a validação correta dos testes de segurança. O problema não era a lógica de segurança em si, mas como o banco de dados SQLite estava sendo compartilhado entre o setup do teste e o servidor Ktor.

## 🛡️ O Que Foi Corrigido (A Causa Raiz)

### 1. Sincronização de Banco de Dados em Testes
- **O Problema**: O Ktor embutido no `testApplication` e o código do teste estavam abrindo conexões diferentes. Quando o teste inseria um produto real, o servidor não o via a tempo e usava o valor do cliente como "fallback", aceitando o preço manipulado.
- **A Solução**: Implementei um sistema de **DB Lock por Teste**. Agora, cada execução de teste gera um ID de banco único (`System.nanoTime()`) e a `Application.kt` foi blindada para **não re-inicializar** o banco se ele já estiver configurado, garantindo que o servidor e o teste usem exatamente o mesmo espaço de memória.

### 2. Blindagem de Repositórios (Recálculo Forçado)
- **O que foi feito**: Removi qualquer possibilidade de "fallback" para o preço do cliente. Se o servidor não encontrar o produto no catálogo oficial, ele lança uma exceção imediata. Se encontrar, ele **sobrescreve** o preço tanto no cabeçalho quanto em cada item da lista.
- **Resultado**: É impossível pagar menos que o valor de catálogo, mesmo interceptando o tráfego.

### 3. Trava de Segurança em Cargos (RBAC)
- **O que foi feito**: Reforcei o `SqliteUserRepository.kt` para garantir que a atualização de perfil (`update`) **ignore completamente** o campo de cargo.
- **Diferença**: Um usuário pode mudar seu nome ou foto, mas o cargo `CUSTOMER` está "gravado na pedra" no banco de dados e só pode ser alterado através do Painel de Controle Global (SuperAdmin).

## 📄 Conclusão
Esta entrega estabiliza os testes de segurança e garante que a Pipe do CircleCI seja um ambiente confiável. O Genesys21 agora possui proteções atômicas contra os ataques mais comuns de lógica de negócio.

> [!IMPORTANT]
> As correções foram enviadas para o branch `main`. A infraestrutura de testes agora está robusta e isolada.
