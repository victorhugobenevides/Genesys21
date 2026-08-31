# Walkthrough - Resolução de Sincronia Física de Banco de Dados

Esta entrega resolve a falha persistente nos testes de segurança no CircleCI, mudando a estratégia de banco de dados de "volátil em memória" para "persistente em arquivo" durante a execução dos testes.

## 🛡️ O Que Foi Corrigido (A Solução Atômica)

### 1. Isolamento Físico de Testes
- **O Problema**: O SQLite em memória (`:memory:`) tem limitações graves de compartilhamento de dados entre threads/processos diferentes, mesmo com `cache=shared`. No ambiente de CI, isso impedia o servidor Ktor de ver os produtos reais inseridos pelo teste.
- **A Solução**: Implementei a criação de um **arquivo físico de banco de dados único** (`.db`) para cada teste unitário.
- **Resultado**: Agora o Teste e o Servidor lêem e escrevem nos **mesmos bytes no disco**, garantindo 100% de visibilidade dos dados de catálogo e usuários.

### 2. Sincronização via Configuração Ktor
- **O que foi feito**: O teste passa o caminho exato do arquivo criado para o servidor através da propriedade `ktor.test.db_path`.
- **Diferença**: Removemos qualquer suposição ou geração de nomes aleatórios dentro da `Application.kt`. A fonte da verdade sobre onde o banco está agora é única e controlada pelo runner do teste.

### 3. Limpeza Automática
- **O que foi feito**: Adicionei lógica no `@AfterTest` para deletar o arquivo temporário de banco de dados após a execução, mantendo o ambiente de CI limpo e livre de artefatos residuais.

## 📄 Conclusão
Com o uso de arquivos reais, eliminamos as falhas de "Asserção" que eram causadas por tabelas vazias no servidor. O Genesys21 agora tem uma infraestrutura de testes de segurança robusta, refletindo cenários reais de produção onde os dados persistem entre transações.

> [!IMPORTANT]
> As correções finais foram enviadas para o branch `main`. Esta é a resolução definitiva para a instabilidade do banco de dados no CircleCI.
