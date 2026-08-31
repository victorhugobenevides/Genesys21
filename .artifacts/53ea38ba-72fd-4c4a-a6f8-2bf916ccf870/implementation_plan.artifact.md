# Plano de Implementação - Reset Seguro e Blindagem SuperAdmin

Este plano visa garantir que o acesso SuperAdmin para `victorkoto@gmail.com` seja restaurado de forma definitiva, seja através de um reset total do banco ou de uma correção automática no próximo login.

## 🎯 Objetivos
- Tornar o `Seeder.kt` 100% resiliente para o Admin principal.
- Garantir que o `SUPERADMIN` tenha todas as permissões de sistema no banco.
- Corrigir a lógica de "Dogma" para ser aplicada em todos os fluxos de dados.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Seeder.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/database/Seeder.kt)
- Atualizar a criação do admin para incluir `permissions = UserPermission.entries.joinToString(",")`.
- Forçar o Cargo `SUPERADMIN` mesmo se o usuário já existir (Update preventivo no boot).

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- **Auto-Reparo**: Na função `getUserProfile`, se o e-mail for o do Dogma e o cargo no banco for diferente de `SUPERADMIN`, o servidor disparará um update silencioso no banco para corrigir o registro. Isso evita a necessidade de deletar o banco manualmente se você não quiser perder os dados atuais.

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Melhorar o log de inicialização para mostrar se o modo `DB_REBUILD` está ativo.

## 📅 Plano de Verificação
1.  Realizar o deploy das alterações.
2.  Acessar o app com `victorkoto@gmail.com`.
3.  Verificar se as abas de "Usuários", "Domínios" e "B2B" aparecem imediatamente (o auto-reparo terá agido).
