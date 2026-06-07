# WebView Order Flow – Spec (Appium)

## 📖 Visão geral
Este documento descreve os critérios de aceitação e os casos de teste automatizados para validar o fluxo de **pedido** dentro da WebView da aplicação **Genesys21** (Android). Os testes serão executados usando **Appium** em modo Web (ChromeDriver) e focarão em:
- Navegação até a tela que contém a WebView.
- Interação com elementos HTML (campo de busca, botão, exibição de status).
- Verificação de persistência de dados e mensagens de erro.

## 🛠️ Ambiente de teste
| Item | Valor |
|------|-------|
| **Dispositivo** | Android Emulator (Pixel 4, API 34) ou dispositivo físico com Android ≥ 5.0 |
| **Browser** | Google Chrome (mesma versão do emulador) |
| **Appium Server** | `http://localhost:4723/wd/hub` (versão ≥ 2.0) |
| **Driver** | `UiAutomator2` + `ChromeDriver` (auto‑download pelo Appium) |
| **Linguagem** | Java + TestNG (ou Python + pytest) |
| **Build da app** | `./gradlew assembleDebug` (debuggable=true) |
| **WebView debugging** | `WebView.setWebContentsDebuggingEnabled(true)` habilitado no código da app (modo debug) |

## 🎯 Objetivo dos testes
1. Garantir que a WebView seja carregada corretamente dentro da aplicação.
2. Validar que o usuário pode buscar um pedido via número e obter o status esperado.
3. Assegurar que mensagens de erro/validação apareçam para entradas inválidas.
4. Capturar screenshots para evidência visual em falhas.

## 📋 Casos de teste
| ID | Descrição | Pré‑condição | Passos | Resultado esperado |
|----|-----------|--------------|-------|-------------------|
| **WT-01** | Carregar WebView | App instalada e lançada | 1. Abrir app → tela principal.<br>2. Pressionar **"Abrir Loja"** (ou botão que abre a WebView). | Contexto muda para `WEBVIEW_*`. Página de pedidos é exibida (URL contém `/order`). |
| **WT-02** | Buscar pedido válido | WebView carregada | 1. No campo `input[name='orderId']` digitar `12345`.<br>2. Click no botão `button.search`. | Texto **"Status: Em preparação"** aparece. Screenshot salva. |
| **WT-03** | Buscar pedido inexistente | WebView carregada | 1. Digitar `00000`.<br>2. Click no botão buscar. | Mensagem **"Pedido não encontrado"** exibida. |
| **WT-04** | Validação de campo vazio | WebView carregada | 1. Deixar campo vazio.<br>2. Click buscar. | Mensagem **"Informe o número do pedido"** mostrada; nenhum request de rede é disparado. |
| **WT-05** | Navegação de volta | WebView carregada | 1. Pressionar botão Android **Back**. | Contexto retorna a `NATIVE_APP` e tela principal do app é exibida. |
| **WT-06** | Performance de carregamento | WebView carregada | Medir tempo entre troca de contexto e presença do elemento `input[name='orderId']`. | Tempo ≤ 3 s (benchmark). |

## 📦 Dados de teste
```json
[
  {"orderId": "12345", "expectedStatus": "Em preparação"},
  {"orderId": "00000", "expectedError": "Pedido não encontrado"}
]
```

## ✅ Critérios de aceitação
- Todos os casos acima devem passar **100 %** nas execuções CI.
- Falhas devem gerar screenshots em `target/screenshots/` e logs detalhados.
- O tempo total da suite (5‑6 testes) deve ser < 2 min em executor padrão.
- O código de teste deve ser **lint‑free** (`checkstyle` ou `flake8`).

## 📂 Estrutura de arquivos esperada
```
appium-tests/
├─ pom.xml               # ou requirements.txt + pytest.ini
├─ src/test/java/.../OrderWebFlowTest.java
└─ resources/
    └─ chromedriver/    # (opcional) versão fixa
```

## 📈 Métricas de qualidade
- **Pass rate** ≥ 99 % nas últimas 20 runs.
- **Flakiness** < 2 % (retries < 2). 
- **Coverage**: 100 % dos fluxos listados acima.

---
*Esta spec está pronta para revisão. Por favor, confirme se está adequada ou adicione/ajuste casos antes de iniciarmos a implementação dos testes.*
