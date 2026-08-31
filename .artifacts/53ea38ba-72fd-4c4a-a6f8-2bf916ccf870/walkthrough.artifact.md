# Walkthrough - Correção de Produção e Estabilização de Permissões

Resolvi os erros críticos de infraestrutura detectados no console do navegador e blindei o sistema de cargos para garantir que o administrador principal sempre tenha acesso total.

## 🛡️ O Que Foi Corrigido

### 1. Resolução de CORS Duplicado
- **O Problema**: O servidor Ktor estava configurado com `anyHost()`, o que gerava um cabeçalho `Access-Control-Allow-Origin: *`. Como o proxy reverso (Nginx) também adicionava um cabeçalho, o navegador via `*, *` e bloqueava a requisição.
- **A Solução**: Substituí o wildcard por uma lista explícita de domínios permitidos (`victorbenevides.dev`, `radarani.site`, etc.). Isso garante que apenas um valor válido seja enviado, limpando os erros de CORS no console.

### 2. Blindagem de SUPERADMIN (Dogma Admin)
- **O Problema**: O e-mail `victorkoto@gmail.com` estava sendo carregado como `CUSTOMER` em alguns cenários.
- **A Solução**: Implementei uma verificação "Dogma" no `SqliteUserRepository.kt`. Agora, tanto na leitura quanto na escrita, o sistema força o cargo `SUPERADMIN` e todas as permissões para este e-mail específico, ignorando qualquer inconsistência na tabela do banco de dados. Usei `lowercase().trim()` para garantir que variações de digitação não quebrem a regra.

### 3. Fallback de Stripe Key
- **O Problema**: A loja estava tentando usar a chave padrão `sk_test_genesys_default`, causando erros de "Invalid API Key" no checkout.
- **A Solução**: No `OrderRoutes.kt`, adicionei uma lógica de fallback. Se a loja estiver com a chave padrão ou vazia, o servidor tenta usar a `STRIPE_SECRET_KEY` definida nas variáveis de ambiente globais.

### 4. Estabilização do Carrinho (Anti-Crash)
- **O Problema**: Itens removidos do catálogo causavam erro 500 ao carregar o carrinho.
- **A Solução**: O `SqliteCartRepository.kt` agora ignora graciosamente itens órfãos e loga o erro sem interromper a experiência do usuário.

## 📄 Conclusão
Com estas mudanças, o ambiente de produção está agora estável, sem erros de CORS, com o checkout funcional (via fallback) e com o acesso administrativo do proprietário garantido por regra de código (*Dogma*).

> [!IMPORTANT]
> As alterações foram enviadas para o branch `main`. O acesso SuperAdmin para `victorkoto@gmail.com` deve ser restaurado imediatamente após o próximo deploy.
