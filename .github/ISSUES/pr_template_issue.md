# Issue: Criar template de Pull Request

**Descrição**
Implementar o template de Pull Request conforme especificado na spec `GitFlow` (arquivo `.specify/specs/005-gitflow/spec.md`). O template deve estar em `.github/pull_request_template.md` e incluir:
- Checklist de revisão de código, segurança e performance.
- Links para tickets e especificações relacionadas.
- Campos para aprovação de revisores seniores.

**Critérios de Aceite**
- Arquivo `.github/pull_request_template.md` criado com o conteúdo adequado.
- Quando um PR for aberto, o template é exibido automaticamente no GitHub.
- Checklist marcado como concluído permite a mesclagem.

**Impacto**
- **Código**: Novo arquivo markdown de template.
- **Processo**: Padroniza a revisão e garante qualidade antes da mesclagem.
- **Documentação**: Atualizar `README.md` com link para o template.
