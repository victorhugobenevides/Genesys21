# Plano de Implementação - Correção de UI Admin (Scroll e Mobile Menu)

Este plano visa corrigir as falhas de usabilidade no portal administrativo, garantindo que todas as telas sejam roláveis e que o menu de navegação seja otimizado para dispositivos móveis.

## 🎯 Objetivos
- Adicionar rolagem vertical em todas as abas do portal administrativo.
- Otimizar o menu de navegação em telas pequenas (mobile), limitando os itens na barra inferior e utilizando um Drawer para as demais opções.
- Garantir que os componentes não fiquem amontoados em resoluções menores.

## 🛠️ Mudanças Propostas

### [composeApp] Design System & Layout

#### [MODIFY] [GenesysPage.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/components/templates/pages/GenesysPage.kt)
- Adicionar suporte a `DrawerState` para abrir o menu lateral programaticamente em mobile.
- Injetar um botão de menu na `topBar` automaticamente se estiver em modo mobile e houver conteúdo no drawer.

#### [MODIFY] [GenesysTopAppBar.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/components/organisms/navigation/GenesysTopAppBar.kt)
- Adicionar parâmetro `onMenuClick: (() -> Unit)?` para exibir o ícone de hambúrguer.

---

### [composeApp] Admin Tabs (Scroll Fix)

#### [MODIFY] Todas as abas em `presentation/screens/list/tabs/`
- Adicionar `Modifier.verticalScroll(rememberScrollState())` aos containers principais para garantir que o conteúdo nunca fique inacessível ou amontoado.
    - `MainDashboardTab.kt`
    - `PagesTab.kt`
    - `AgendaTab.kt`
    - `ServicesTab.kt`
    - `B2BInsightsTab.kt`
    - `StoreSettingsTab.kt`
    - `GlobalUsersTab.kt`
    - `GlobalDomainsTab.kt`
    - `AuditLogsTab.kt`

---

### [composeApp] Navegação Inteligente

#### [MODIFY] [PageListScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/list/PageListScreen.kt)
- Refatorar a lógica de `navigationSuiteItems`:
    - No Mobile: Mostrar apenas as 4 abas principais (Dashboard, Pedidos, Vitrines, Agenda) + Botão "Mais" (que abre o Drawer).
    - No Desktop: Manter comportamento atual (Rail ou Sidebar).

---

## 📅 Plano de Verificação
1.  **Mobile**: Abrir o portal administrativo em um navegador mobile (ou simulador compact). Verificar se a barra inferior tem apenas 5 itens e se o botão "Mais" ou o ícone no TopBar abre o menu completo.
2.  **Scroll**: Acessar o Dashboard com muitos dados e a lista de usuários global. Validar se a rolagem funciona suavemente.
3.  **Visual**: Garantir que o rodapé "desenvolvido por..." não sobreponha o conteúdo das abas.

> [!IMPORTANT]
> A falta de scroll era causada pelo uso de `Column` sem modificadores de rolagem dentro de um container com peso (`weight(1f)`), o que forçava os componentes a tentarem caber em um espaço fixo.

**Posso prosseguir com as correções de UI e Navegação?**
