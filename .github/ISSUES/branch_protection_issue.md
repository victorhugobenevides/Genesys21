# Issue: Definir regras de proteção de branch

**Descrição**
Implementar as regras de proteção de branch conforme especificado na spec `GitFlow` (arquivo `.specify/specs/005-gitflow/spec.md`). As regras devem incluir:
- Proibição de push direto em `main` e `release/*`.
- Revisões obrigatórias (mínimo 2 aprovadores, um senior).
- Checks de status obrigatórios (lint, testes, CodeQL, Dependabot).
- Requer assinatura GPG para merges.

**Critérios de Aceite**
- Arquivo `branch_protection.json` já existente está configurado corretamente.
- Proteções aplicadas no repositório GitHub via UI ou API.
- Documentação atualizada no `README.md` com link para as regras.

**Impacto**
- **Código**: nenhuma mudança de código, apenas configuração de branch.
- **Documentação**: atualização do README.
- **Processo**: aumenta a segurança e auditabilidade do fluxo de trabalho.
