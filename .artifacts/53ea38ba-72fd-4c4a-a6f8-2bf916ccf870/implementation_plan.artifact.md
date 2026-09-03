# Plano de Implementação - Diagnóstico e Restauração de Vitrines

Este plano visa investigar por que as páginas pararam de aparecer (especificamente a nova vitrine de estética) e garantir que o sistema de gerenciamento de vitrines esteja estável após o reset do banco de dados.

## 🔍 Análise de Causa Raiz

### 1. Desaparecimento de Páginas
- **Causa provável**: O reset do banco de dados (`DB_REBUILD=true`) deletou todas as páginas criadas anteriormente. Isso é o comportamento esperado de um reset total.
- **Questão**: Por que a vitrine de estética (`estetica-demo`) não apareceu?
  - Possível falha no `Seeder.kt` ao tentar indexar produtos com IDs genéricos ou falha na transação.
  - Possível erro de dessincronização entre o `adminId` fixo e o UID real do usuário no Firebase.

### 2. Erro 500 no Carrinho
- **Causa**: O console indica erro 500 ao tentar acessar `/api/cart`. Isso sugere uma falha de integridade ou um bug na leitura dos itens.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Seeder.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/database/Seeder.kt)
- **Rigor de UID**: Garantir que o seeder busque o usuário pelo e-mail configurado e use o seu ID real para criar as vitrines.
- **IDs Únicos**: Mudar os IDs dos produtos no template de estética de `p1`, `p2` para `beauty_salon_p1`, etc., para evitar colisões.
- **Logs de Sucesso**: Adicionar logs explícitos para cada página criada: `"[SEEDER] Página [ID] criada para o dono [UID]"`.

#### [MODIFY] [SqlitePageRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqlitePageRepository.kt)
- **Logging de Consulta**: Adicionar log na função `getPages` para imprimir quantas páginas foram encontradas no banco para o UID logado. Isso nos ajudará a saber se o dado existe mas o filtro está bloqueando.

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- **Erro Detalhado**: Melhorar o log de exceções no `StatusPages` para imprimir o stack trace completo no console do servidor (visível via `docker logs`).

## 📅 Plano de Verificação
1.  Realizar o deploy.
2.  Acessar a administração.
3.  Verificar se agora aparecem duas vitrines: "Currículo" e "Espaço Aurora".
4.  Se não aparecerem, os logs do servidor nos dirão o número exato de linhas encontradas na tabela `pages`.
