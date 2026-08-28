# Execução do Plano de Pentest & Hardening

Organização das tarefas para validar e reforçar a segurança do Genesys21.

## 🛠️ Autenticação e Autorização (RBAC)
- [ ] Criar teste de integração para validar bloqueio de rotas `/api/admin` para usuários sem permissão.
- [ ] Validar se o "Dogma Admin" (`victorkoto@gmail.com`) está protegido contra manipulação de headers.

## 🛡️ Proteção de APIs (OWASP)
- [ ] **Fix**: Implementar validação de preço no servidor em `OrderRoutes.kt` (recalculando o total com base no banco antes de criar o pedido).
- [ ] Auditar `SqliteOrderRepository` para garantir que o `orderId` não permite enumeração simples.
- [ ] Revisar `saveUserProfile` para prevenir Mass Assignment de campos sensíveis como `role`.

## 💳 Segurança Stripe
- [ ] Criar script de teste para simular Webhook Spoofing e validar falha de assinatura.
- [ ] Verificar se chaves secretas nunca aparecem em logs ou respostas de erro do Ktor.

## 🌐 Segurança de Front-end & Infra
- [ ] Refinar a Content-Security-Policy (CSP) para ser mais restritiva em produção.
- [ ] Validar isolamento de dados entre lojistas (Multitenancy check).

## 📄 Relatório e Documentação
- [ ] Atualizar o `SECURITY.md` com as descobertas e correções aplicadas.
