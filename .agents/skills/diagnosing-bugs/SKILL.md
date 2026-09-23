---
name: diagnosing-bugs
description: Protocolo disciplinado de depuração gotejada (gated debugging) em 6 passos no Genesys21. Investigação baseada em evidências antes de aplicar correções.
---

# Diagnóstico e Depuração Gotejada no Genesys21

Esta skill define o protocolo rigoroso em 6 passos para diagnosticar, isolar e resolver bugs no Genesys21 sem "adivinhação" ou alterações aleatórias de código.

---

## Protocolo em 6 Passos (Gated Debugging)

### Passo 1: Reprodução via Teste Automatizado (Red Test)
Antes de alterar qualquer arquivo de produção, crie ou isole um teste que reproduza **exatamente** o defeito relatado:
- Para bugs de API/Servidor: Crie um teste em `:server` usando `testApplication`.
- Para bugs de lógica compartilhada: Crie um teste em `:shared` (`commonTest`).
- Para bugs de UI/Layout: Crie um teste Paparazzi em `:screenshot-tests`.
- **Validação**: Execute o teste e confirme a falha com a mensagem de erro esperada.

---

### Passo 2: Isolamento da Fronteira do Bug
Determine a camada exata onde a falha se origina:
1. **Camada de Apresentação (Compose/MVI)**: O estado do ViewModel (`uiState`) está incorreto ou a intenção não foi disparada?
2. **Camada de Rede/Ktor**: O payload JSON está divergindo da serialização `@Serializable`?
3. **Camada do Servidor/Ktor Route**: O interceptor de auth (`authenticate("firebase")`) ou a autorização por papel (`SUPERADMIN`/`MERCHANT`) está barrando a requisição?
4. **Camada de Persistência (Exposed ORM / SQLite)**: A transação WAL mode falhou ou a query Exposed gerou sintaxe incompatível?
5. **Interop WasmJs (`bridge.js`)**: O polyfill JS falhou em ambiente não-HTTPS ou no browser?

---

### Passo 3: Formulação de Hipótese Falseável
Defina uma única hipótese clara em formato de causa-efeito:
> *"O bug ocorre porque `SqliteOrderRepository` confia no valor `total` enviado pelo cliente em vez de buscar o preço unitário atualizado no banco."*

---

### Passo 4: Instrumentação e Coleta de Evidências
Adicione logs focados ou asserções temporárias sem alterar a lógica de negócio:
- Use logs de teste ou Ktor `CallLogging`.
- Verifique valores no banco SQLite de desenvolvimento/teste.
- **Não aplique a correção nesta etapa!** Apenas colete evidências que confirmem ou refutem a hipótese do Passo 3.

---

### Passo 5: Aplicação da Correção Mínima
Assim que a causa raiz for confirmada pelas evidências:
- Aplique o menor conjunto de alterações necessário para corrigir a causa raiz.
- Execute o teste reprodutor criado no Passo 1 e garanta que ele agora **passa (Green)**.

---

### Passo 6: Regressão e Verificação Estática
Garantir que a correção não introduziu efeitos colaterais em outras partes do sistema:
```bash
# 1. Testes de Unidade e Servidor
./gradlew :shared:allTests :server:test

# 2. Testes de Screenshot UI
./gradlew :screenshot-tests:verifyPaparazziDebug

# 3. Linter e Análise Estática
./gradlew ktlintCheck detekt
```
