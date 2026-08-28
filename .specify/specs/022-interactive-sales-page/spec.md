# Spec 022: Interactive Product Tour (The Genesys Experience)

## 1. Visão Geral
A "Genesys Experience" será a página de vendas oficial do produto, projetada para converter visitantes em lojistas. Em vez de apenas ler sobre o produto, o usuário poderá **interagir** com as principais tecnologias do Genesys21 (White-Label, Temas, Pagamentos e Responsividade) em tempo real, sem necessidade de login.

## 2. Objetivos
- Demonstrar visualmente o poder do motor White-Label.
- Reduzir a fricção de entrada através de uma demonstração interativa ("Try before you buy").
- Mostrar a flexibilidade do Design System em múltiplos dispositivos.
- Validar a facilidade de uso do editor de páginas.

## 3. Experiência do Usuário (Interactive Journey)

### 3.1 Seção Hero: "Seu Negócio em Qualquer Lugar"
- Título impactante e animação de entrada.
- Botão "Iniciar Tour Interativo".

### 3.2 O "Magic Theme Switcher" (Interativo)
- Um controle deslizante ou botões de seleção de tema (Elegance, Mono, Glass, Royal).
- Conforme o usuário troca, a página inteira (ou um preview central) altera instantaneamente as cores, tipografia e estilos.

### 3.3 Vitrine Dinâmica (Drag & Preview)
- O usuário pode arrastar componentes (ou clicar em toggles) para "montar" uma loja fictícia.
- Componentes disponíveis:
    - **Header**: Trocar logo e frase de impacto.
    - **Product List**: Alternar entre visualização de Grade ou Lista.
    - **Booking**: Simular a escolha de um horário de serviço.

### 3.4 Simulador de Checkout Seguro (Stripe Demo)
- Uma simulação visual do **Stripe Payment Element**.
- O usuário pode clicar em "Comprar" e ver como o checkout integrado (sem redirecionamentos) funciona no Genesys21.

### 3.5 Device Sandbox (Responsividade)
- Botões para alternar o preview entre: **Mobile (Pixel 5)**, **Tablet (iPad)** e **Desktop (MacBook)**.
- Demonstra como o layout se adapta automaticamente usando o `ProvideWindowSizeClass`.

## 4. Requisitos Funcionais

### 4.1 Navegação
- Acesso público via `/about` ou uma nova rota `/experience`.
- Integrada ao roteador KMP (`Route.Experience`).

### 4.2 Estado Local
- O estado da interação (tema escolhido, componentes ativos) será estritamente local (Compose `rememberState`), não persistindo no banco até que o usuário crie uma conta real.

## 5. Arquitetura Técnica

### 5.1 Front-end (Wasm/KMP)
- Reutilização massiva dos componentes existentes em `composeApp/commonMain/kotlin/com/itbenevides/genesys21/ui/components/`.
- Uso de `BoxWithConstraints` para o simulador de dispositivos.

### 5.2 Mocking
- Criação de um `MockPageViewModel` simplificado ou passagem de estados manuais para os componentes de UI, evitando chamadas de rede reais durante o tour.

## 6. Call to Action (Conversão)
- Ao final do tour, oferecer um botão de "Criar minha loja agora" que redireciona para o Sign Up com as configurações escolhidas no tour já pré-preenchidas (opcional).
