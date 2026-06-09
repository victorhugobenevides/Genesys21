# Gitflow & Development Workflow

This document defines the branching and development standards for the Genesys21 project.

## Branches

- **main**: Production-ready code. Only merged from `develop` via Release/Hotfix.
- **develop**: Integration branch for new features and bug fixes.
- **feature/* or bugfix/*: Working branches. Must be created from `develop` and merged back to `develop` via Pull Request.

## Workflow Rules (Barriers)

1. **No Direct Commits to Main/Develop**: All changes must go through a feature/bugfix branch.
2. **Issue Tracking**: Every change must be linked to a GitHub Issue.
3. **Naming Convention**: `bugfix/issue-[number]-[description]` or `feature/issue-[number]-[description]`.
4. **Pull Requests**:
   - Must include a description of the changes.
   - Should be reviewed (if possible).
   - **MUST PASS CI BUILD**: Direct merge to `develop` or `main` without passing CI is strictly prohibited.
5. **Static Analysis**: All PRs must have no "ERROR" level warnings in Android Studio/IntelliJ inspections before being marked as ready for review.
6. **Automated Testing**: 
   - Feature additions must include Unit Tests.
   - Bug fixes must include a regression test when applicable (e.g., verifying State transitions).

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` for new features.
- `fix:` for bug fixes.
- `refactor:` for code changes that neither fix a bug nor add a feature.
- `docs:` for documentation.
