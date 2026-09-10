# Walkthrough: Utilidade Elite & Estabilização CI (v5.6.0)

Nesta versão, focamos em produtividade para o lojista e na integridade da nossa Pipeline de deploy.

## Mudanças Principais

### 1. Seleção de Texto (Spec 019) 📋
Ativamos a capacidade de selecionar e copiar textos críticos em todo o Portal Admin. Agora, o lojista pode copiar facilmente:
- **IDs de Pedidos (UUIDs)** e **Nomes de Clientes** na aba de Pedidos.
- **IDs de Vitrines** na listagem de páginas.
- **Dados de Endereço** nas configurações da loja.
- **Nomes e Telefones** na agenda.

> [!TIP]
> Isso elimina a necessidade de digitar IDs complexos manualmente, reduzindo erros de operação.

### 2. Estabilização da Pipeline (Snapshot Tests) 📸
Após as grandes mudanças de layout para corrigir o scroll, nossos testes visuais (Paparazzi) ficaram desatualizados.
- Removemos os `@Ignore` que estavam silenciando os erros.
- A Pipeline agora está configurada para falhar se houver regressão visual, garantindo que o novo padrão "Elite Admin" seja preservado.

## Como Validar

1. **Seleção de Texto**:
   - Acesse o portal e vá na aba de **Pedidos**.
   - Tente selecionar o ID de um pedido com o mouse ou toque longo no celular. Você verá que os handles de seleção nativos aparecem agora.
2. **Deploy Automático**:
   - Acompanhe o job `visual-verification` na CircleCI. Ele servirá de base para atualizarmos os baselines de imagem.

render_diffs(file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/list/components/AdminUIComponents.kt)
render_diffs(file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/list/tabs/AgendaTab.kt)
