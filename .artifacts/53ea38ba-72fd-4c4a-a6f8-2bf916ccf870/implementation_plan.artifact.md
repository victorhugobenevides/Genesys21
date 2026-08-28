# Plano de Implementação - Console Adm Centralizado (Enterprise UI)

Este plano visa transformar a área administrativa do **Genesys21** de uma lista simples de abas para um **Console Centralizado** profissional, com menu categorizado e controle rigoroso de hierarquia de acesso (RBAC).

## 1. Visão Geral
A UI atual do `PageListScreen` cresceu organicamente e as abas estão misturando funcionalidades de operação, financeiro e configuração. Vamos reorganizar tudo em um menu lateral (Sidebar) para Desktop/Tablet e um Drawer para Mobile, agrupando por domínio de negócio.

## 2. Nova Estrutura de Menu (Hierarquia)

### 📊 DASHBOARD
- **Painel Principal**: Métricas de faturamento e vendas da loja atual.
- **Insights da Rede** (B2B): Visão macro da plataforma (Exclusivo SuperAdmin).

### 🛍️ OPERAÇÕES
- **Vitrines**: Gerenciamento de páginas e landing pages.
- **Pedidos**: Fluxo de atendimento e chat com clientes.
- **Agenda**: Calendário de agendamentos.
- **Serviços**: Cadastro e preços de serviços.

### 💰 FINANCEIRO
- **Notas Fiscais**: Histórico de recibos e impostos.
- **Pagamentos**: Configurações do Stripe Connect e gateway.

### ⚙️ CONFIGURAÇÕES
- **Minha Loja**: Dados de remetente, entrega e temas.
- **Meu Perfil**: Dados do usuário e segurança.
- **Controle Global** (SuperAdmin): Gestão de usuários, cargos e domínios (Exclusivo SuperAdmin).

## 3. Mudanças Propostas

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [NEW] [AdminMenu.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/list/components/AdminMenu.kt)
- Centralizar a definição dos itens de menu, ícones, categorias e permissões necessárias.

#### [MODIFY] [PageListScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/list/PageListScreen.kt)
- Refatorar para usar o novo `NavigationWrapper` com categorias.
- Mover as sub-UIs (PagesTabUI, ServicesTabUI, etc.) para arquivos próprios no pacote `screens/list/tabs/` para reduzir o tamanho do arquivo principal.

#### [MODIFY] [NavigationSuiteScaffold](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/components/templates/pages/GenesysPage.kt)
- Ajustar para suportar agrupamento visual ou separadores entre categorias de menu.

## 4. Hierarquia de Acesso (RBAC)
- **CUSTOMER**: Acesso negado ao Console Adm (redireciona para Login/Home).
- **MERCHANT**: Acesso a Dashboard, Operações, Financeiro e Configurações (da sua loja).
- **ADMIN**: Mesmas permissões do Merchant + Gestão de outros Merchants (opcional).
- **SUPERADMIN**: Acesso total (incluindo B2B Insights e Controle Global).

## 5. Plano de Verificação

### Testes de UI (Paparazzi)
- Criar snapshots do novo layout de menu em Phone, Tablet e Desktop.

### Verificação Manual
- Validar se um usuário `MERCHANT` **não** visualiza as opções "Insights Rede" e "Controle Global".
- Confirmar se a navegação entre categorias está fluida e mantém o estado da tela anterior.
