---
name: code-review
description: Protocolo de Code Review em dois eixos (Eixo 1: Dogma & Segurança; Eixo 2: Requisitos & Testes) para o Genesys21.
---

# Code Review em Dois Eixos no Genesys21

Esta skill orienta a revisão de código (seja em Pull Requests ou antes de commits) avaliando as alterações sob dois eixos fundamentais.

---

## Eixo 1: Dogma & Segurança (Compliance Estrito)

| Item | Ponto de Verificação | Status |
| :--- | :--- | :---: |
| **Bypass de Identidade** | As checagens do E-mail de Ouro (`victorkoto@gmail.com`) e UID de Ouro no `SqliteUserRepository.kt` continuam intactas e protegidas? | [ ] |
| **Price Tampering** | Em rotas de criação/atualização de pedidos (`/api/public/orders`), os preços e totais estão sendo recalculados no servidor a partir do banco de dados? | [ ] |
| **IDOR Check** | As rotas de escrita em `PageRoutes.kt`, `StoreRoutes.kt` e similares validam se o `ownerId` corresponde ao `principal.name` do token do Firebase? | [ ] |
| **Privilégios de Perfil** | Na rota de atualização de usuário, os campos `role` e `permissions` são ignorados no `UPDATE` para usuários comuns? | [ ] |
| **Segurança de Logs** | Nenhuma chave secreta (Stripe, Firebase Admin SDK, senhas) é emitida em texto puro nos logs do Ktor/Logcat? | [ ] |

---

## Eixo 2: Requisitos, Qualidade & Testes

| Item | Ponto de Verificação | Status |
| :--- | :--- | :---: |
| **Cobertura TDD** | Existem testes automatizados cobrindo os caminhos felizes e casos de borda em `:shared` ou `:server`? | [ ] |
| **Screenshot Tests** | Se houve alteração de UI em Compose ou `PageComponent`, a suíte Paparazzi foi executada (`:screenshot-tests:verifyPaparazziDebug`)? | [ ] |
| **Formatadores & Linters** | O código passa sem avisos ou erros no `ktlintCheck` e no `detekt`? | [ ] |
| **WasmJs Safety** | Nenhuma API experimental ou não suportada em JS/WasmJs foi adicionada sem o devido fallback no `bridge.js`? | [ ] |
| **Imutabilidade MVI** | O estado do ViewModel é exposto de forma imutável via `StateFlow`? | [ ] |

---

## Comandos de Validação Rápida

```bash
# 1. Executar todos os testes de unidade e servidor
./gradlew :shared:allTests :server:test

# 2. Verificar regressão visual de componentes UI
./gradlew :screenshot-tests:verifyPaparazziDebug

# 3. Executar verificações estáticas de estilo e qualidade
./gradlew ktlintCheck detekt
```
