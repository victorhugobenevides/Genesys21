---
name: handoff
description: Compressão de estado e protocolo de transição estruturada entre sessões de IA ou repasse para desenvolvedor no Genesys21.
---

# Handoff de Sessão no Genesys21

Esta skill orienta a geração de um relatório compacto e estruturado de transição (*handoff*) para finalizar uma sessão de IA ou transferir o contexto para outro agente/desenvolvedor sem perda de histórico.

---

## 1. Estrutura Padrão do Handoff

Ao concluir uma tarefa significativa ou ao final da sessão, a IA deve resumir o trabalho seguindo este modelo:

```markdown
# Relatório de Handoff - Genesys21

## 1. Resumo da Tarefa
- **Objetivo**: [Descreva brevemente o que foi solicitado]
- **Status**: [Concluído / Em Progresso / Bloqueado]

## 2. Alterações Realizadas
- **Módulos Modificados**: [ex: :shared, :server, :screenshot-tests]
- **Principais Arquivos**:
  - `path/to/File1.kt`: [Breve explicação da mudança]
  - `path/to/File2.kt`: [Breve explicação da mudança]

## 3. Estado dos Testes e Qualidade
- [x] Testes de Unidade (`./gradlew :shared:allTests`): [Passaram / Não executados]
- [x] Testes de Servidor (`./gradlew :server:test`): [Passaram / Não executados]
- [x] Screenshots Paparazzi (`./gradlew :screenshot-tests:verifyPaparazziDebug`): [Passaram / Não executados]
- [x] Análise Estática (`./gradlew ktlintCheck detekt`): [Sem avisos]

## 4. Próximos Passos & Decisões Pendentes
- [ ] [Próxima tarefa a ser executada]
- [ ] [Decisão técnica que requer aprovação do desenvolvedor]
```

---

## 2. Diretrizes de Compressão de Contexto
- Mantenha o handoff objetivo e conciso (máximo de 1 página).
- Destaque quaisquer mudanças em dados do banco ou scripts de migração Flyway/Exposed.
- Caso existam decisões arquiteturais tomadas (ADRs), faça referência ao arquivo correspondente.
