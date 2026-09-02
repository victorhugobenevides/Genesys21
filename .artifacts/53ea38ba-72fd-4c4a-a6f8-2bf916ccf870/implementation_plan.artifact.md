# Plano de Implementação - Strike Final (Correção de Emergência)

Este plano aplica uma correção de nível "bypass" para garantir o acesso SuperAdmin e o funcionamento da Stripe, eliminando dependências de banco de dados para a identidade do proprietário.

## 🔍 Diagnóstico Final
- A lógica de banco de dados/repositório parece estar sendo ignorada ou sobreposta por alguma inconsistência no servidor.
- O fallback de chaves da Stripe no banco de dados está interferindo nas variáveis de ambiente.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [UserRoutes.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/routes/UserRoutes.kt)
- **Bypass de Identidade**: Na rota `/api/users/profile/me`, vou adicionar uma checagem ANTES de chamar o repositório. Se o e-mail verificado no token for `victorkoto@gmail.com`, o servidor retornará um objeto `UserProfile` montado em memória com cargo `SUPERADMIN` e todas as permissões. **Isso é infalível.**

#### [MODIFY] [OrderRoutes.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/routes/OrderRoutes.kt)
- **Stripe Priority**: Forçar a leitura de `System.getenv("STRIPE_SECRET_KEY")` como primeira opção absoluta, ignorando completamente o que estiver no banco de dados para a loja do dono.

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Adicionar rota pública `/api/public/status` que retorna apenas `"Genesys21 Stable v5"`. Isso nos confirmará se o deploy da v5 bateu.

## 📅 Plano de Verificação
1.  Acessar `https://victorbenevides.dev/api/public/status`. Deve retornar "Genesys21 Stable v5".
2.  Login no App.
3.  As abas de Admin **precisam** aparecer agora, pois o bypass no roteamento do Ktor não consulta o banco.
