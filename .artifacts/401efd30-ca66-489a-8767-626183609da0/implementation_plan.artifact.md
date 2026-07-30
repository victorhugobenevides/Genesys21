# Plano de Implementação: Componente de Valor Dinâmico (Doações/Cotas)

Este plano descreve a criação de um novo componente de página que permite aos usuários escolherem um valor (doação, cota, contribuição) e seguirem diretamente para o fluxo de pagamento.

## User Review Required

> [!IMPORTANT]
> O componente permitirá valores pré-definidos (sugestões) e um campo para valor personalizado.
> Os itens serão adicionados ao carrinho com um nome customizado (ex: "Doação para o Canal") e o preço selecionado.

## Proposed Changes

### 1. Módulo Shared (:shared)

#### [MODIFY] [CartItem.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/model/CartItem.kt)
*   Adicionar campos `customName: String?` e `customPrice: Double?`.
*   Atualizar os getters `name` e `price` para priorizar esses campos.

#### [MODIFY] [Page.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/model/Page.kt)
*   Adicionar `PageComponent.ValuedAction` ao sealed class com os campos:
    *   `title: String`
    *   `description: String?`
    *   `suggestedValues: List<Double>`
    *   `allowCustomValue: Boolean`
    *   `buttonText: String` (ex: "Contribuir", "Doar")

---

### 2. Módulo Server (:server)

#### [MODIFY] [SqliteOrderRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteOrderRepository.kt)
*   Atualizar o método `fetchOrderItems` para carregar `customName` e `customPrice` das colunas `product_name` e `product_price` da tabela `order_items`, garantindo que itens dinâmicos sejam restaurados corretamente no histórico de pedidos.

---

### 3. Módulo Compose App (:composeApp)

#### [MODIFY] [PageViewModel.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/PageViewModel.kt)
*   Adicionar função `addValuedActionToCart(name: String, price: Double)` para facilitar a inclusão desses itens.

#### [NEW] [ValuedActionComponent.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/components/ValuedActionComponent.kt)
*   Implementar o componente visual que exibe:
    *   Título e descrição.
    *   Chips/Botões para valores sugeridos.
    *   Campo de texto para valor customizado (se habilitado).
    *   Botão de ação que adiciona ao carrinho e redireciona.

#### [MODIFY] [PublicViewer.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/viewer/PublicViewer.kt) (ou equivalente)
*   Registrar o novo componente no renderizador de páginas.

---

## Verification Plan

### Automated Tests
*   **Unit Test:** Validar que um `CartItem` com `customPrice` calcula o total corretamente.
*   **Integration Test:** Criar um pedido com um item de valor dinâmico e verificar se o valor persiste no banco de dados.

### Manual Verification
1.  No Editor de Página, adicionar o componente "Ação com Valor".
2.  Configurar valores sugeridos (ex: 10, 20, 50).
3.  No Visualizador Público, selecionar um valor e clicar no botão.
4.  Verificar se o item aparece no carrinho com o valor correto.
5.  Finalizar a compra (simulado ou Stripe test mode) e verificar o pedido gerado.
