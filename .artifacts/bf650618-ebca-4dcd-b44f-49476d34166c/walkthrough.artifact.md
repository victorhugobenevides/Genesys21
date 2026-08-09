# Walkthrough - Correção de Conflito de Classes e Estabilização do Boot

Resolvi o erro crítico de inicialização do servidor (`NoSuchMethodError`) causado por uma colisão de nomes de classes entre os módulos do projeto e estabilizei o processo de boot.

## Mudanças Realizadas

### 1. Resolução de Colisão no Classpath
- **Problema**: Existiam duas classes `GoogleCalendarService` com o mesmo pacote (`com.itbenevides.genesys21.data.service`) nos módulos `:server` e `:shared`. O Java carregava a versão do `:shared` (que exigia parâmetros no construtor) quando o servidor tentava usar a versão do `:server` (que não tinha parâmetros), resultando em um crash imediato no boot.
- **Solução**: Removi a versão duplicada e incompleta do módulo `:shared`. A implementação real e completa agora vive exclusivamente no módulo `:server`, onde as bibliotecas nativas do Google estão disponíveis.

### 2. Melhoria no Serviço de Google Calendar
- **Não-Bloqueante**: Envolvi as chamadas da API do Google (que são síncronas em Java) em um bloco `withContext(Dispatchers.IO)`. Isso garante que a criação de links do Meet não trave as threads principais do servidor Ktor.
- **Robustez**: Adicionei inicialização preguiçosa (`lazy`) para os componentes de transporte e JSON do Google, evitando falhas precoces durante a criação do objeto.

### 3. Ajuste no Pipeline de Deploy (CircleCI)
- **Limpeza Total**: Adicionei um passo de `clean` antes da compilação para garantir que nenhum artefato antigo ou corrompido seja incluído na imagem Docker.
- **Logs de Erro**: O Smoke Test agora monitora e imprime os logs do container em caso de falha de boot, facilitando diagnósticos futuros.

## Resultados
- O crash `java.lang.NoSuchMethodError` foi eliminado.
- O servidor agora deve completar o boot com sucesso no CircleCI.
- A integração com Google Meet está mais segura e performática.

---
> [!TIP]
> Com a remoção do arquivo duplicado no `:shared`, a estrutura do projeto ficou mais limpa e livre de comportamentos imprevisíveis de "shadowing" de classes.
