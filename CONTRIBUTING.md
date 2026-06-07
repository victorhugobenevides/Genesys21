# Contributing Guidelines

Thank you for contributing to **Genesys21**! This project follows the **GitFlow** workflow.

## Branch Strategy
- **main** – production‑ready code.
- **develop** – integration branch for features.
- **feature/** – new feature branches, e.g. `feature/123-add-login`.
- **release/** – release preparation branches, e.g. `release/1.2.0`.
- **hotfix/** – urgent fixes to production, e.g. `hotfix/456-crash-fix`.

## How to Contribute
1. **Fork** the repository.
2. Clone your fork locally.
3. Create a new branch from `develop` using the naming convention:
   ```bash
   git checkout -b feature/<ticket-id>-short-description develop
   ```
4. Make your changes, write tests, and ensure the CI passes.
5. Commit with a clear message.
6. Push the branch to your fork and open a **Pull Request** against `develop`.
   - The PR will automatically use the template at `.github/pull_request_template.md`.
7. After approval, the PR will be merged and the changes flow to `main` via a release branch.

## Issue Reporting
- Use the **Feature Request** template for new ideas.
- Use the **Bug Report** template for bugs.
- All templates are located in the `.github/ISSUE_TEMPLATE` directory.

## Code Style & Testing
- Follow the existing code style (Kotlin idioms, Compose conventions).
- Run the test suite locally before submitting a PR:
  ```bash
  ./gradlew test
  ```

## Continuous Integration
- CI runs on every push to `main`, `develop`, `release/*`, and `hotfix/*`.
- Status checks must pass before merging.

---

For more details about the GitFlow automation, see the script `scripts/gitflow_init.sh`.
