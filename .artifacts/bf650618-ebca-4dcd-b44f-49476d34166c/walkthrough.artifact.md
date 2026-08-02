# Walkthrough - Correção de Build e Ativação de Staging

Corrigi a falha de compilação no CircleCI e finalizei a estrutura para suportar múltiplos ambientes (Produção e Staging) no mesmo servidor.

## Mudanças Realizadas

### 1. Restauração de Segredos (CircleCI)
- **Problema**: O build falhou por falta do arquivo `google-services.json`, que havia sido removido na última simplificação do pipeline.
- **Solução**: Restaurei as etapas de decodificação (`Base64 -> File`) para `google-services.json` e `firebase-adminsdk.json` em todos os jobs do pipeline. Sem esses arquivos, as tarefas do Gradle relacionadas ao Firebase falham.

### 2. Infraestrutura Multi-Ambiente (Nginx)
- Configurei o Nginx para gerenciar dois ambientes simultaneamente:
    - **Produção**: `victorbenevides.dev` -> Porta 8080.
    - **Staging**: `staging.victorbenevides.dev` -> Porta 8081.
- O staging agora possui seu próprio volume de dados (`data-staging/`), garantindo que testes não afetem a produção.

### 3. Feedback Visual de Erros
- Implementei o `SnackbarHost` no `App.kt`. Agora, erros que ocorrem no ViewModel (como conflitos de agendamento) são exibidos visualmente para o usuário.

## Resultados
- O pipeline foi disparado com o commit `1a0f6c5`.
- A compilação deve ocorrer com sucesso agora que os arquivos de segredo foram restaurados.
- O ambiente de staging estará pronto para receber a branch `develop`.

---
> [!IMPORTANT]
> O Nginx central agora gerencia ambos os domínios. Certifique-se de que o certificado SSL cobre `staging.victorbenevides.dev`. Se houver erro de HTTPS no staging, precisaremos rodar o Certbot uma vez para incluir o novo subdomínio.
