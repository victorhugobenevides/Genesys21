# Plano de Implementação - Restauração de Admin e Fix de Checkout Stripe

Este plano resolve a causa raiz do problema de permissões (onde o Admin aparecia como Customer) e corrige a falha de checkout na Stripe ignorando chaves padrão.

## 🔍 Análise de Causa Raiz

### 1. Problema de SuperAdmin (Role: CUSTOMER)
- **Causa**: O App (Wasm/Android) chama a rota `/api/public/users/profile/{id}`. Por segurança (LGPD), essa rota pública converte o perfil para `PublicUserProfile`, que **não contém o campo `role`**.
- **Efeito**: Ao receber o JSON sem o campo `role`, o cliente Kotlin deserializa o objeto usando o valor padrão do enum: `UserRole.CUSTOMER`.
- **Solução**: Criar uma rota autenticada `/api/users/profile/me` que retorna o `UserProfile` completo para o próprio usuário.

### 2. Erro de Stripe (Invalid API Key)
- **Causa**: A loja "Dogma" está salva no banco com a chave `sk_test_genesys_default`. O fallback atual só entrava se a chave estivesse em branco.
- **Solução**: Modificar a lógica de fallback para ignorar explicitamente a chave `sk_test_genesys_default` e usar a variável de ambiente do servidor.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [UserRoutes.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/routes/UserRoutes.kt)
- Adicionar rota `GET /api/users/profile/me` dentro do bloco `authenticate`.
- Esta rota retornará o objeto `UserProfile` integral (com Role e Permissions).

#### [MODIFY] [OrderRoutes.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/routes/OrderRoutes.kt)
- Ajustar a detecção de chaves Stripe para tratar `sk_test_genesys_default` como valor inválido/vazio, forçando o uso da variável de ambiente `STRIPE_SECRET_KEY`.

### [shared](file:///Users/victorben/AndroidStudioProjects/genesys21/shared)

#### [MODIFY] [KtorUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/data/repository/KtorUserRepository.kt)
- Alterar o método `getUserProfile(id)` para detectar se o ID solicitado é o do usuário logado.
- Se for o próprio usuário, chamar a nova rota `/api/users/profile/me` para obter as permissões administrativas.

## 📅 Plano de Verificação
1.  Realizar o deploy.
2.  Logar com `victorkoto@gmail.com`.
3.  Confirmar que o objeto no console do navegador agora contém `"role": "SUPERADMIN"`.
4.  Tentar um checkout e validar se a chave da Stripe usada é a correta.
