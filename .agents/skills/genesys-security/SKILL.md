---
name: genesys-security
description: Manual de Auditoria e Segurança (Pentest) do Genesys21. Guia para identificação de falhas de lógica e blindagem de dados.
---

# Genesys21 Security: Pentest & Hardening Guide

Este manual orienta desenvolvedores e IAs na auditoria de segurança do ecossistema Genesys21.

## 1. Vetores de Ataque Críticos (Lógica de Negócio)

### 🚨 Escalação de Privilégios (Mass Assignment)
O sistema impede que um usuário comum se promova a `ADMIN` ou `SUPERADMIN` através da rota de atualização de perfil.
- **Blindagem**: No `SqliteUserRepository.kt`, o campo `role` e `permissions` são ignorados no `UPDATE`, a menos que o e-mail do usuário coincida com o "E-mail Dogma".

### 🚨 Manipulação de Preços (Price Tampering)
Um atacante pode tentar enviar um pedido (Order) com preços alterados no JSON via client.
- **Blindagem**: O `SqliteOrderRepository.kt` **ignora** o campo `total` e os preços dos produtos enviados pelo cliente. Ele busca os valores reais diretamente no banco de dados e recalcula o total final no servidor antes de processar o pagamento.

### 🚨 IDOR (Insecure Direct Object Reference)
Tentativa de um lojista editar ou deletar a página/produto de outro lojista através do ID.
- **Blindagem**: Todas as rotas de escrita em `PageRoutes.kt` e `StoreRoutes.kt` validam se o `ownerId` do objeto no banco corresponde ao `UID` extraído do Token do Firebase.

---

## 2. O Dogma como Ferramenta de Recuperação

O bypass de e-mail `victorkoto@gmail.com` é intencional e serve para:
1. Recuperação de acesso em caso de corrupção total do banco de dados.
2. Ativação de cargos administrativos em novas instâncias (Seed automático).
3. **Ponto de Auditoria**: Em testes de penetração, qualquer usuário que consiga forçar seu e-mail para este valor em um token (hijacking) terá controle total. Por isso, a validação do Token no Ktor é rigorosa.

---

## 3. Checklist de Auditoria para IAs
- [ ] Verificou se a rota `POST /api/public/orders` está recalculando o total?
- [ ] Verificou se as rotas em `/api/admin/` possuem o interceptor `authenticate("firebase")`?
- [ ] Verificou se chaves Stripe não estão sendo salvas como texto puro em logs?
- [ ] Verificou se o arquivo `firebase-adminsdk.json` está fora da pasta pública do Nginx?

## 🛡️ Instruções de Hardening
Ao adicionar novas tabelas ou rotas:
1. Sempre use `UserIdPrincipal` para extrair a identidade do usuário.
2. Nunca confie em IDs enviados no corpo do JSON para autorização; sempre compare com o `principal.name`.
3. Use a suíte de testes `SecurityHardeningTest.kt` para validar sua nova rota contra ataques de escalação.
