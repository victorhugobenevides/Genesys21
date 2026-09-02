# Plano de Implementação - Operação Nuclear v5 (Estabilização Absoluta)

Este plano aplica a correção definitiva para o acesso SuperAdmin e para a Stripe, tratando a causa raiz na dessincronização entre as camadas Shared e Server.

## 🔍 Diagnóstico Final
- **Shared Repository**: O `KtorUserRepository` estava caindo na rota pública por falha de tempo na bridge JS. Isso causava a deserialização do perfil com o valor padrão `CUSTOMER`.
- **UID Mismatch**: O seeder às vezes criava o admin com um UID aleatório, conflitando com o UID real do Firebase do usuário.
- **Stripe ENV**: As chaves não estavam sendo propagadas corretamente para o runtime do Ktor.

## 🛠️ Mudanças Propostas

### [shared](file:///Users/victorben/AndroidStudioProjects/genesys21/shared)

#### [MODIFY] [DogmaConstants.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/model/DogmaConstants.kt)
- Registrar o UID mestre: `mKQ9MZqG6bYhy3JqvngGpv49ZZs1`.
- Criar a regra `UserProfile.isGodMode()` que valida UID ou E-mail.

#### [MODIFY] [KtorUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/data/repository/KtorUserRepository.kt)
- Forçar o uso da rota `/api/users/profile/me` sempre que um token JWT estiver presente.
- Se o perfil retornado for do dono, garantir que o cargo SuperAdmin seja injetado localmente como proteção secundária.

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Injetar o cargo `SUPERADMIN` no topo da função `toUserProfile` usando o UID e o E-mail.

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Adicionar verificador de integridade de ambiente: O servidor não iniciará se `STRIPE_SECRET_KEY` estiver ausente em modo de produção.

## 📅 Plano de Verificação
1.  Deploy imediato da v5.
2.  Login com `victorkoto@gmail.com`.
3.  O badge `SUPERADMIN` e todas as abas administrativas devem aparecer no primeiro frame após o login.
