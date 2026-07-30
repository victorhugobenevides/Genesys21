# Especificação: Lista de Presentes (Casamentos e Aniversários)

Este plano descreve a implementação de uma nova funcionalidade de "Lista de Presentes" no ecossistema Genesys21. A funcionalidade permitirá que usuários criem listas de presentes para eventos e que convidados visualizem e adquiram esses itens.

## User Review Required

> [!IMPORTANT]
> A integração com pagamentos (Stripe) será necessária para a compra real dos presentes. Este plano foca na estrutura de dados, UI e navegação inicial.

> [!NOTE]
> Precisamos decidir se o "presente" é um produto físico que será enviado ou uma contribuição em dinheiro (Cotas de Lua de Mel, etc). Inicialmente, suportaremos ambos.

## Proposto Changes

### Shared Module (:shared)

#### [NEW] [Gift.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/model/Gift.kt)
Define o modelo de dados para um presente individual.

#### [NEW] [GiftList.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/model/GiftList.kt)
Define o modelo para a lista de presentes vinculada a um evento.

#### [NEW] [GiftRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/repository/GiftRepository.kt)
Interface para operações de CRUD de listas e presentes.

#### [NEW] [KtorGiftRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/data/repository/KtorGiftRepository.kt)
Implementação Ktor para o repositório de presentes.

---

### Compose App Module (:composeApp)

#### [MODIFY] [Route.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/navigation/Route.kt)
Adicionar novas rotas: `GiftListPublic` e `GiftListAdmin`.

#### [MODIFY] [Router.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/navigation/Router.kt)
Atualizar o router para lidar com as novas rotas e sincronização de URL.

#### [NEW] [GiftListViewModel.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/GiftListViewModel.kt)
ViewModel para gerenciar o estado da lista de presentes.

#### [NEW] [GiftListScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/ui/GiftListScreen.kt)
Tela principal que exibe a lista de presentes (adaptável para público/admin).

## Plano de Verificação

### Testes Automatizados
- Unit tests para `GiftListViewModel`.
- Testes de mapeamento JSON no `KtorGiftRepository`.

### Verificação Manual
- Navegar para `/gift-list/{id}` e verificar a renderização.
- Testar a adição de itens à lista no modo admin.
- Simular a reserva de um presente no modo público.
