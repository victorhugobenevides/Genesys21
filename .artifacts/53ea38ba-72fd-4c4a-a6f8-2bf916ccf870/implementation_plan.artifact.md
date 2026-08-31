# Plano de Implementação - Estabilização Crítica (Final)

Este plano aplica uma "opção nuclear" para restaurar o acesso SuperAdmin e corrigir os erros de Stripe e CORS no ambiente de produção.

## 🎯 Objetivos
- Forçar o cargo `SUPERADMIN` no front-end para o e-mail do proprietário (God Mode).
- Eliminar duplicidade de cabeçalhos CORS (`*, *`).
- Resolver falha de checkout Stripe por chaves padrão.
- Blindar o carregamento do perfil no servidor.

## 🛠️ Mudanças Propostas

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [MODIFY] [PageViewModel.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/PageViewModel.kt)
- **God Mode**: Na função `loadUserProfile`, se o perfil retornado for do e-mail `victorkoto@gmail.com`, forçar o cargo para `SUPERADMIN` localmente no estado da ViewModel. Isso garante acesso imediato mesmo que haja lag de sincronia no banco.

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- **CORS**: Remover `anyHost()` e utilizar apenas a lista explícita de domínios.
- **Logs**: Adicionar log do e-mail do usuário no login para auditoria.

#### [MODIFY] [OrderRoutes.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/routes/OrderRoutes.kt)
- **Stripe Debug**: Logar a chave da Stripe sendo usada (mascarada) no console do servidor para identificar por que o fallback está falhando.

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Reforçar o auto-reparo para ser executado em toda chamada de perfil do Admin.

## 📅 Plano de Verificação
1.  Deploy imediato.
2.  Login com `victorkoto@gmail.com`.
3.  Verificar no console do navegador se o objeto `userProfile` agora reflete `SUPERADMIN`.
4.  Realizar um checkout de teste.
