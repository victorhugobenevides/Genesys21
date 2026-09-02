# Plano de Implementação - Nova Vitrine de Estética e Estabilização de Componentes

Este plano visa criar uma nova página de vitrine para o tema "Salão de Beleza/Clínica de Estética", atendendo aos requisitos de distribuição em redes sociais, exibição de procedimentos sem preços, galeria do espaço físico e horários de funcionamento.

## 🎯 Objetivos
- Adicionar suporte para ocultar preços em listas de produtos e serviços.
- Criar o componente `BusinessHours` para exibição de horários.
- Adicionar um novo template "Salão & Estética" ao `PageTemplateRegistry`.
- Criar uma página de demonstração via `Seeder`.

## 🛠️ Mudanças Propostas

### [shared](file:///Users/victorben/AndroidStudioProjects/genesys21/shared)

#### [MODIFY] [Page.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/model/Page.kt)
- Adicionar campo `showPrice: Boolean = true` aos componentes `ProductList`, `ProductGrid` e `ServiceList`.
- Adicionar o componente `BusinessHours` com suporte a lista de dias e horários.

#### [MODIFY] [PageTemplate.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/domain/model/PageTemplate.kt)
- Adicionar o template `beautySalon` (Salão & Estética) com:
    - `ProfileHeader` para Bio (Instagram/FB).
    - `SocialLinks` para contato rápido.
    - `ProductList` (com `showPrice = false`) para os Procedimentos.
    - `Grid` com `Image` para a Galeria do Espaço.
    - `BusinessHours` para o funcionamento.
    - `Button` de destaque para o WhatsApp.

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [MODIFY] [PageComponentRenderer.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/viewer/PageComponentRenderer.kt)
- Implementar a renderização do novo componente `BusinessHours`.
- Respeitar a flag `showPrice` na renderização de `ProductList` e `ServiceList`.

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Seeder.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/database/Seeder.kt)
- Adicionar a criação de uma página de exemplo "Espaço de Beleza - Demonstração" utilizando o novo template.

## 📅 Plano de Verificação
- Validar se os preços estão ocultos na página de estética.
- Verificar se o componente de horários está legível e elegante.
- Confirmar se o botão de WhatsApp redireciona corretamente.
