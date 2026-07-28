## Recommended Connect integration

### A. Account configuration
Accounts API: `/v2/core/accounts`
Legacy account `type`: not used
Dashboard: full
Fee collection: Stripe
Negative balance liability: Stripe

Esta configuração é a ideal para o seu modelo SaaS (Software as a Service). Como os lojistas operam em domínios próprios e você deseja que eles assumam total responsabilidade pelas taxas e riscos financeiros (reembolsos/estornos), eles atuarão como mercadores independentes na rede Stripe.

Cada conta conectada precisa da configuração de mercador (`configuration.merchant`) para processar cobranças diretas.

### B. Charge pattern: direct
O padrão de cobranças diretas (Direct Charges) é o que melhor se adapta ao seu negócio. A transação ocorre diretamente na conta do lojista, facilitando a gestão de impostos, chargebacks e taxas de processamento, que são deduzidas automaticamente do saldo dele, mantendo sua comissão em zero conforme planejado.

### C. Lojista (Vendedor) onboarding flow
Onboarding method: embedded
Utilizaremos componentes incorporados (Account Sessions) para que o lojista realize o cadastro sem sair do seu portal administrativo. Isso mantém a experiência de marca (White-label) enquanto a Stripe cuida da coleta segura de documentos e verificação de identidade.

Fluxo: O lojista clica na "engrenagem" -> Você cria uma conta via API -> Gera uma Account Session -> Exibe o componente de onboarding -> Stripe verifica os dados -> Você libera as vendas assim que as capacidades estiverem ativas.

### D. Payments dashboard access para Lojistas
Como utilizamos `dashboard: full`, os lojistas poderão fazer login diretamente em `dashboard.stripe.com` para uma gestão profunda e relatórios avançados. No entanto, para o dia a dia, eles utilizarão os componentes incorporados que você disponibilizará dentro do portal Genesys21.

### E. Embedded components
Componentes recomendados para o seu portal:
- `account_onboarding`: Para o cadastro inicial.
- `notification_banner`: Obrigatório; avisa o lojista se houver pendências de documentos para evitar bloqueios de saques.
- `account_management`: Para ele editar dados da loja e domínios.
- `payments`: Para ele ver a lista de vendas realizadas.
- `payouts`: Para ele gerenciar os saques para a conta bancária.

### F. Webhook integration
Utilize webhooks para confirmação de pagamentos em tempo real, especialmente para métodos assíncronos como o Pix. Sempre valide as assinaturas dos eventos recebidos para garantir a segurança da comunicação entre a Stripe e o seu servidor Ktor.

### G. Onboarding status gating
Verifique o status das capacidades antes de permitir transações:
- Cobranças: `configuration.merchant.capabilities.card_payments.status === 'active'`
- Saques: `configuration.merchant.capabilities.stripe_balance.payouts.status === 'active'`

### H. Fee structure
- Platform fee model: zero (gratuito por transação)
- `application_fee_amount` strategy: platform fee only (R$ 0,00)
- O cliente paga o valor total diretamente ao lojista. A Stripe deduz as taxas de processamento do lojista e deposita o valor líquido na conta dele.

Consulte as taxas atuais em [stripe.com/pricing](https://stripe.com/pricing).

### I. Implementation plan
1. Habilitar o Connect no Dashboard da Stripe.
2. Criar a rota POST `/api/admin/connect/accounts` para geração de contas vinculadas ao `owner_id`.
3. Integrar o cabeçalho `Stripe-Account` no `StripeService.kt` para direcionar cobranças.
4. Implementar a interface de "Configurações de Pagamento" no Compose com os componentes da Stripe.
5. Configurar o Webhook de produção para ouvir eventos de contas conectadas.

### J. Risk and liability
- Dono da responsabilidade por saldo negativo: Lojista (Conta Conectada)
- Dono dos controles de risco (Radar): Lojista (Conta Conectada)

### K. Why this fits your business
- Alinhamento total com o modelo de responsabilidade financeira do lojista.
- Suporte nativo para múltiplos domínios e identidades de fatura.
- Custo operacional zero para a sua plataforma no processamento das vendas.
