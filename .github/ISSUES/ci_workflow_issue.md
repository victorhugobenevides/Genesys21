# Issue: Implement CI workflow for GitFlow

**Descrição**
Implementar o fluxo de CI/CD conforme especificado no spec `GitFlow`. O workflow deve incluir lint, testes unitários, testes de integração, CodeQL e OWASP Dependency‑Check para cada Pull Request.

**Critérios de Aceite**
- Arquivo de workflow GitHub Actions em `.github/workflows/ci.yml` criado.
- Jobs configurados para rodar em `push` e `pull_request` nos branches `develop`, `release/*` e `main`.
- Cada job inclui etapas de lint, testes, análise estática e geração de artefatos.
- Pipeline concluído em menos de 15 minutos.

**Impacto**
- **Código**: novos arquivos YAML de workflow.
- **Testes**: integração automática nas PRs.
- **Documentação**: atualizar README com badge de status CI.
