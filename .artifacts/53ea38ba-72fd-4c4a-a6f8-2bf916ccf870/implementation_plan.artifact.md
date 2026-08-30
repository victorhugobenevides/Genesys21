# Plano de Implementação - Correção de Compilação (Smart Cast) e Validação Local

Este plano visa corrigir o erro de compilação no módulo `server` e estabelecer um processo de validação local rigoroso para evitar falhas na Pipeline.

## 🔍 Causa Raiz
O compilador Kotlin não permite o *Smart Cast* de propriedades de classes declaradas em outros módulos (como o módulo `shared`) porque elas são consideradas mutáveis ou podem ser alteradas por outras threads. No arquivo `SqliteOrderRepository.kt`, estamos acessando `item.product.id` e `item.service.id` dentro de blocos `when` sem capturá-los em variáveis locais.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [SqliteOrderRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteOrderRepository.kt)
- Refatorar o bloco `createOrder` para capturar `item.product` e `item.service` em variáveis locais (`val product = item.product`, `val service = item.service`) antes de realizar a verificação de nulidade e acesso às propriedades.

## 🛡️ Novo Processo de Validação
Para economizar recursos da Pipeline e evitar frustrações, passarei a executar:
1.  **Análise Estática Local**: `analyze_file` para detectar erros de sintaxe e semântica imediatamente.
2.  **Compilação Atômica**: `gradle_build(":server:compileKotlin")` para garantir que o código realmente compila no ambiente de build antes do push.

## 📅 Plano de Verificação
- Rodar `./gradlew :server:compileKotlin` e confirmar o status "SUCCESSFUL".
- Rodar os testes de segurança novamente após a compilação passar.
