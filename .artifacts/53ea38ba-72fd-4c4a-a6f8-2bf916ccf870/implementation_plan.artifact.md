# Plano de Implementação - Correção Definitiva: Checkout Stripe e Acesso SuperAdmin

O usuário reportou que os problemas de checkout (Stripe) e de visibilidade do menu Admin (SuperAdmin) persistem.

## Análise

### 1. Checkout Stripe (Erro de Chave Inválida)
- **Causa**: O banco de dados está viciado com a chave "dummy" (`sk_test_genesys_default`). O `Seeder.kt` só atualiza a chave se ela for nula ou tiver menos de 50 caracteres. Chaves Stripe reais e a chave dummy podem passar nessa validação, impedindo a atualização quando novas variáveis de ambiente são configuradas.
- **Solução**: Forçar a atualização das chaves da loja padrão se as variáveis de ambiente `STRIPE_PUBLIC_KEY` e `STRIPE_SECRET_KEY` estiverem presentes e forem diferentes do que está no banco, ou simplesmente simplificar a lógica para sempre preferir a env var se disponível.

### 2. Acesso SuperAdmin (Menu sem opções)
- **Causa**:
    1. O "Dogma" (regra que força `SUPERADMIN` para `victorkoto@gmail.com`) está apenas no `saveUserProfile`. Se o usuário já existe no banco como `CUSTOMER`, o `getUserProfile` (usado no login) retorna `CUSTOMER`.
    2. O `SqliteUserRepository.toUserProfile` não aplica a regra de promoção automática.
- **Solução**: Mover a lógica do "Dogma" para o método `toUserProfile` no repositório. Isso garante que, independente de como o dado foi salvo, o sistema sempre reconhecerá este e-mail específico como `SUPERADMIN` em tempo de execução.

## Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Mover a verificação de e-mail `victorkoto@gmail.com` para dentro de `toUserProfile`.
- Garantir que o cargo `SUPERADMIN` e todas as permissões sejam atribuídos dinamicamente se o e-mail coincidir.

#### [MODIFY] [Seeder.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/database/Seeder.kt)
- Alterar a lógica de atualização da loja padrão para sempre atualizar as chaves Stripe se as variáveis de ambiente não forem as "default".

## Plano de Verificação

### Verificação Manual
1. Solicitar ao usuário que reinicie o servidor (para disparar o Seeder).
2. Solicitar Logout/Login no front-end.
3. Verificar se o menu "SuperAdmin" aparece.
4. Tentar realizar um checkout e verificar se o erro de chave persiste.
