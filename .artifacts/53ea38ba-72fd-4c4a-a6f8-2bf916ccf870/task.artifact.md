# Tasks: Fix User Management and SuperAdmin Access 🛡️👥

- [x] **Fase 1: Segurança no Servidor (Repository)**
    - [x] Implementar "GOD MODE" em `SqliteUserRepository.kt` (Proprietário sempre SUPERADMIN)
- [x] **Fase 2: Interface de Gerenciamento (UI)**
    - [x] Atualizar `UserAdminCard` em `AdminUIComponents.kt` para suportar todos os cargos
    - [x] Melhorar feedback visual na troca de cargos
- [/] **Fase 3: Deploy e Validação**
    - [x] Incrementar versão para v6.1.9
    - [ ] Validar acesso às rotas `/api/admin/users`
