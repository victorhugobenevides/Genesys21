# Plano de Implementação - Correção de Produção e Auditoria de Role

Este plano visa corrigir os erros de infraestrutura no servidor e investigar/corrigir o motivo de o usuário administrador estar sendo carregado como `CUSTOMER`.

## 🎯 Objetivos
- Resolver bloqueio de **CORS** duplicado (`*, *`).
- Corrigir falha de **Stripe** por chaves padrão (sk_test_genesys_default).
- Eliminar o **Erro 500** no Carrinho.
- Garantir que o e-mail `victorkoto@gmail.com` seja forçado como `SUPERADMIN`.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- **CORS**: Substituir `anyHost()` por uma lista restrita (`victorbenevides.dev`, `radarani.site`, `localhost`). Isso evita que o Ktor envie o wildcard `*` se o proxy (Nginx) já o fizer.
- **Stripe Fallback**: Inserir lógica no `OrderRoutes` ou `StripeService` para detectar a chave padrão e tentar ler `System.getenv("STRIPE_SECRET_KEY")`.

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Investigar falha na comparação de e-mail. Adicionar `.lowercase().trim()` na verificação do "Dogma Admin".
- Reforçar a injeção do cargo em todos os pontos de retorno (`getUserProfile` e `getAllUsers`).

#### [MODIFY] [SqliteCartRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteCartRepository.kt)
- Adicionar blocos `try-catch` individuais no carregamento de itens do carrinho. Se um produto for deletado, o item deve ser ignorado ou retornado como "Indisponível", em vez de causar erro 500 na requisição inteira.

## 📅 Plano de Verificação
1.  Monitorar o console do navegador após o deploy.
2.  Logar como `victorkoto@gmail.com` e verificar se a aba "B2B" e "Admin" aparecem no Sidebar.
3.  Simular checkout e validar que o `PaymentIntent` é gerado sem erro de "Invalid API Key".
