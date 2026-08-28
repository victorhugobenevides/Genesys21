# Plano de Teste de Invasão (Pentest) - Genesys21

Este plano detalha a estratégia de segurança para validar a resiliência do motor White-Label **Genesys21** contra ataques cibernéticos, cobrindo desde a autenticação até a infraestrutura de pagamentos.

## Objetivos do Pentest
- Validar a eficácia da autenticação Firebase e controle de cargos (RBAC).
- Testar a robustez das APIs REST contra vulnerabilidades OWASP Top 10.
- Verificar o isolamento de dados entre lojistas (Multitenancy).
- Avaliar a segurança da integração com Stripe e processamento de pagamentos.
- Garantir conformidade com a LGPD no tratamento de dados sensíveis.

---

## 🛡️ Escopo Técnica

### 1. Autenticação e Autorização (IAM)
- **Firebase Token Bypass**: Tentar forjar ou reutilizar tokens expirados.
- **Privilege Escalation**: Tentar acessar rotas de `/api/admin` ou `/api/superadmin` com um token de `CUSTOMER`.
- **Session Fixation**: Avaliar como o sistema lida com a troca de contexto de usuário.
- **Dogma Admin Check**: Validar se a regra de promoção automática para `victorkoto@gmail.com` pode ser explorada por outros domínios ou via injeção de e-mail.

### 2. Segurança da API (OWASP Top 10)
- **IDOR (Insecure Direct Object Reference)**: Tentar acessar pedidos (`/api/public/orders/{id}`) ou perfis de outros usuários alterando o UUID na URL.
- **Injeção de SQL**: Testar filtros de busca e campos de cadastro contra injeções no SQLite (Exposed ORM).
- **Mass Assignment**: Tentar atualizar campos protegidos (ex: `role` ou `stripe_secret_key`) enviando metadados extras no POST de perfil.
- **Rate Limiting**: Validar se os limites de 100/min (global) e 5/min (sensível) podem ser contornados via troca de IP ou cabeçalhos `X-Forwarded-For`.

### 3. Integração Financeira (Stripe)
- **Webhook Spoofing**: Tentar enviar payloads falsos para `/api/public/orders/webhook` sem assinatura válida ou com assinaturas de contas teste.
- **Price Manipulation**: Tentar alterar o valor do pedido no front-end (Wasm) e verificar se o backend valida o `total` contra o banco de dados antes de criar o `PaymentIntent`.
- **Key Exposure**: Verificar se chaves `sk_test` ou `pk_test` estão vazando em logs, metadados de imagem ou erros de serialização.

### 4. Segurança de Front-end (Wasm/JS)
- **XSS (Cross-Site Scripting)**: Injetar scripts em nomes de produtos, bio de perfis ou metadados de templates.
- **CSP Validation**: Tentar carregar scripts externos não autorizados (violando a política de `script-src`).
- **Sensitive Data in Bridge**: Avaliar se informações sensíveis do Firebase/Stripe ficam expostas no objeto `window` ou na ponte JavaScript.

### 5. Privacidade e LGPD
- **PII Leakage**: Validar se rotas públicas `/public/users/profile/{id}` realmente retornam apenas o DTO mascarado.
- **Right to Erasure**: Confirmar se a deleção de conta anonimiza corretamente os logs de auditoria e remove arquivos de `/uploads`.

---

## 🛠️ Ferramentas Sugeridas
- **Burp Suite**: Para interceptação e manipulação de tráfego.
- **Postman/Newman**: Para testes automatizados de segurança em endpoints.
- **OWASP ZAP**: Scanner automatizado de vulnerabilidades web.
- **Snyk/Checkmarx**: Para análise de vulnerabilidades em dependências KMP.

---

## 📅 Cronograma de Execução
1. **Fase de Reconhecimento**: Mapeamento de endpoints e superfícies de ataque.
2. **Testes de Autenticação**: Foco total no Firebase e RBAC.
3. **Exploração de APIs**: Testes de IDOR, Injeção e Rate Limit.
4. **Validação de Pagamentos**: Testes de Webhook e integridade de preços.
5. **Relatório de Vulnerabilidades**: Documentação de achados e planos de remediação.

> [!CAUTION]
> Este pentest deve ser executado apenas em ambiente de **Staging** ou **Local**. Nunca execute ataques de negação de serviço ou manipulação de dados em produção sem backup e janela de manutenção aprovada.

**O plano atende às suas necessidades ou deseja aprofundar em algum módulo específico?**
