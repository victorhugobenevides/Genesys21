# Plano de Implementação - Paralelismo de Pipeline

Este plano visa otimizar o tempo de CI/CD do projeto Genesys21, permitindo que validações lógicas, visuais e o build de produção ocorram simultaneamente.

## 🎯 Objetivos
- Reduzir o tempo total do workflow de ~20 minutos para ~12 minutos.
- Manter a segurança de deploy (apenas código testado vai para produção).

## 🛠️ Mudanças Propostas

### Infraestrutura (CircleCI)

#### [MODIFY] [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- Alterar a seção `workflows` para remover as dependências lineares.
- **Workflow Antigo**: Testes -> Visual -> Build -> Deploy.
- **Workflow Novo**:
    - `test-and-validate`, `visual-verification` e `build-and-push` iniciam juntos.
    - `deploy` requer que todos os três acima finalizem com sucesso.

## 📅 Plano de Verificação
1.  Realizar um pequeno push de teste.
2.  Observar o painel do CircleCI.
3.  **Critério de Aceite**: Três barras de progresso devem estar rodando ao mesmo tempo.
4.  O deploy deve aguardar as três ficarem verdes antes de iniciar.

> [!TIP]
> Essa mudança é segura porque o `deploy` continuará sendo a última barreira de proteção. Se qualquer teste falhar, o servidor da Oracle não será tocado.
